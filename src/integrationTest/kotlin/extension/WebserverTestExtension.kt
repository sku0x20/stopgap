package extension

import com.example.stopgap.Endpoint
import com.example.stopgap.HelidonConfig
import com.example.stopgap.instanceregistry.Config
import com.example.stopgap.instanceregistry.InstanceRegistry
import io.helidon.webserver.WebServer
import io.helidon.webserver.http.HttpRouting
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestInstancePostProcessor
import org.junit.platform.commons.support.AnnotationSupport
import org.junit.platform.commons.support.HierarchyTraversalMode
import org.junit.platform.commons.support.ModifierSupport
import org.junit.platform.commons.support.ReflectionSupport
import java.lang.reflect.Method
import kotlin.reflect.KClass

/**
 * WebserverTestExtension runs once per test class.
 * It doesn't care if the test launch is per class or per methods, it behaves the same.
 * Junit Extensions also try to abstract that out, so it does not affect extensions.
 */
class WebserverTestExtension : BeforeAllCallback, TestInstancePostProcessor, AfterAllCallback {

    companion object {
        private const val CONFIG = "config-key"
        private const val INSTANCE_REGISTRY = "instance-registry-key"
        private const val ENDPOINT = "endpoint-key"
        private const val SERVER_INSTANCE = "webserver-instance-key"
        private const val CLIENTS = "clients-key"
    }

    override fun beforeAll(context: ExtensionContext) {
        val store = getStore(context)

        val testClass = context.requiredTestClass

        val config = setupConfig(testClass, store)
        val registry = setupInstanceRegistry(testClass, config, store)
        val server = setupServer(testClass, registry, store)
        setupClient(testClass, server, store)
    }

    @Suppress("UNCHECKED_CAST")
    override fun postProcessTestInstance(
        testInstance: Any,
        context: ExtensionContext
    ) {
        val store = getStore(context)
        val injectableFields = AnnotationSupport.findAnnotatedFields(
            context.requiredTestClass,
            InjectInstance::class.java
        )
        val clients = store.get(CLIENTS) as Map<Class<*>, Any>
        for (field in injectableFields) {
            when (field.type) {
                Config::class.java -> field.set(testInstance, store.get(CONFIG))
                InstanceRegistry::class.java -> field.set(testInstance, store.get(INSTANCE_REGISTRY))
                WebServer::class.java -> field.set(testInstance, store.get(SERVER_INSTANCE))
                else -> {
                    val client = clients[field.type]
                    if (client != null) {
                        field.set(testInstance, client)
                    }
                }
            }
        }
    }

    override fun afterAll(context: ExtensionContext) {
        val store = getStore(context)
        closeClient(store)
        stopServer(store)
    }

    private fun getStore(context: ExtensionContext): ExtensionContext.Store {
        // scope to test class
        val nameSpace = ExtensionContext.Namespace.create(
            this::class.java,
            context.requiredTestClass,
        )
        return context.getStore(nameSpace)
    }

    private fun setupConfig(
        testClass: Class<*>,
        store: ExtensionContext.Store
    ): Config {
        val method = findStaticMethod(
            testClass,
            WebserverTest.CreateConfig::class.java
        )
        val config = if (method == null) HelidonConfig.loadDefault() else method.invoke(null) as Config
        store.put(CONFIG, config)
        return config
    }

    private fun setupInstanceRegistry(
        testClass: Class<*>,
        config: Config,
        store: ExtensionContext.Store
    ): InstanceRegistry {
        val registry = InstanceRegistry(config)
        val method = findStaticMethod(
            testClass,
            WebserverTest.SetupInstanceRegistry::class.java
        )
        if (method != null) {
            val endpoint = method
                .getAnnotation(WebserverTest.SetupInstanceRegistry::class.java)
                .endpoint
            if (endpoint != Endpoint::class) {
                store.put(ENDPOINT, endpoint)
            }
            method.invoke(null, registry)
        }
        store.put(INSTANCE_REGISTRY, registry)
        return registry
    }

    @Suppress("UNCHECKED_CAST")
    private fun setupServer(
        testClass: Class<*>,
        registry: InstanceRegistry,
        store: ExtensionContext.Store
    ): WebServer {
        val helidonConfig = HelidonConfig.loadDefault()
        val builder = WebServer.builder()
            .config(helidonConfig.getConfig("server"))
            .protocolsDiscoverServices(false)
            .port(0)
            .host("localhost")

        val endpointClazz = store.get(ENDPOINT) as? KClass<out Endpoint>
        if (endpointClazz != null) {
            // todo: make InstanceRegistry take KClass
            val endpoint = registry.getInstanceForQualifier<Endpoint>(endpointClazz.qualifiedName!!)
            val routing = HttpRouting.builder()
                .register("/", endpoint.routes(registry))
            builder.routing(routing)
        }

        findStaticMethod(
            testClass,
            WebserverTest.ConfigServer::class.java
        )?.invoke(null, builder)
        val server = builder
            .build()
            .start()
        store.put(SERVER_INSTANCE, server)
        return server
    }

    private fun setupClient(
        testClass: Class<*>,
        server: WebServer,
        store: ExtensionContext.Store
    ) {
        // todo: do it only once put it in static.
        val webserverAnnotation = testClass.getAnnotation(WebserverTest::class.java)!!
        val clientAnnotation = webserverAnnotation.client
        val roots = this::class.java.classLoader.getResources("")
        val allMethods = mutableListOf<Method>()
        for (root in roots) {
            ReflectionSupport.findAllClassesInClasspathRoot(root.toURI(), { clazz ->
                val methods = AnnotationSupport.findAnnotatedMethods(
                    clazz,
                    clientAnnotation.java,
                    HierarchyTraversalMode.TOP_DOWN
                )
                var added = false
                for (method in methods) {
                    if (ModifierSupport.isStatic(method)) {
                        allMethods.add(method)
                        added = true
                    }
                }
                added
            }, { true })
        }

        val clients = mutableMapOf<Class<*>, Any>()
        for (method in allMethods) {
            val host = server.prototype().host()
            val port = server.port()
            val client = method.invoke(null, host, port)
            clients[method.returnType] = client
        }
        store.put(CLIENTS, clients)
    }

    private fun closeClient(store: ExtensionContext.Store) {
        // todo: not supported
//        val client = store.get(CLIENT_INSTANCE) as WebClient
//        client.closeResource()
    }

    private fun stopServer(store: ExtensionContext.Store) {
        val server = store.get(SERVER_INSTANCE) as WebServer
        server.stop()
    }

    private fun findStaticMethod(
        testClass: Class<*>,
        annotation: Class<out Annotation>
    ): Method? {
        val methods = AnnotationSupport.findAnnotatedMethods(
            testClass,
            annotation,
            HierarchyTraversalMode.TOP_DOWN
        )
        if (methods.size > 1) {
            throw IllegalStateException("Only one method can be annotated with ${annotation.name}")
        }
        if (methods.isEmpty()) {
            return null
        }
        val member = methods[0]
        if (ModifierSupport.isNotStatic(member)) {
            throw IllegalStateException("${annotation.name} method must be static")
        }
        return member
    }

}