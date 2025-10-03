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
import java.lang.reflect.Method
import kotlin.reflect.KClass

class WebserverTestExtension : BeforeAllCallback, TestInstancePostProcessor, AfterAllCallback {

    companion object {
        private const val CONFIG = "config-key"
        private const val INSTANCE_REGISTRY = "instance-registry-key"
        private const val ENDPOINT = "endpoint-key"
        private const val SERVER_INSTANCE = "webserver-instance-key"
    }

    override fun beforeAll(context: ExtensionContext) {
        val store = getStore(context)

        val config = setupConfig(context.requiredTestClass, store)
        val registry = setupInstanceRegistry(context.requiredTestClass, config, store)
        setupServer(context.requiredTestClass, registry, store)
    }

    override fun postProcessTestInstance(
        testInstance: Any,
        context: ExtensionContext
    ) {
        val store = getStore(context)
        val injectableFields = AnnotationSupport.findAnnotatedFields(
            context.requiredTestClass,
            InjectInstance::class.java
        )
        for (field in injectableFields) {
            when (field.type) {
                Config::class.java -> field.set(testInstance, store.get(CONFIG))
                InstanceRegistry::class.java -> field.set(testInstance, store.get(INSTANCE_REGISTRY))
                WebServer::class.java -> field.set(testInstance, store.get(SERVER_INSTANCE))
            }
        }
    }

    override fun afterAll(context: ExtensionContext) {
        val store = getStore(context)
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
    ) {
        val builder = WebServer.builder()
            .port(0)
            .host("localhost")
            .protocolsDiscoverServices(false)

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