package extension.webservertest

import extension.InjectInstance
import extension.SharedStore
import io.helidon.config.Config
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

/**
 * WebserverTestExtension runs once per test class.
 * It doesn't care if the test launch is per class or per methods, it behaves the same.
 * Junit Extensions also try to abstract that out, so changing the launch does not affect extensions.
 * Via [WebserverTest.CreateEndpoint] and [WebserverTest.DestroyEndpoint] this allows test classes to manage instances.
 * This allows running in parallel as it ties the instances lifecycle with the run, rather than static.
 */
class WebserverTestExtension : BeforeAllCallback, TestInstancePostProcessor, AfterAllCallback {

    companion object {
        private val loadedConfig = Config.create()

        const val SERVER_INSTANCE = "server.instance"

        private const val INITIALIZERS_CLASS_NAME = "dev.sku20.helidon.endpoint.generated.InitializersKt"

        private const val USER_INSTANCES = "server.user.instances"
        private const val ENDPOINT_CLAZZ = "server.endpoint.clazz"
    }

    override fun beforeAll(context: ExtensionContext) {
        val testClass = context.requiredTestClass
        val store = SharedStore.getStoreScopedToTestClass(context)
        createEndpoint(testClass, store)
        startServer(testClass, store)
    }

    override fun postProcessTestInstance(
        testInstance: Any,
        context: ExtensionContext
    ) {
        val injectableFields = AnnotationSupport.findAnnotatedFields(
            context.requiredTestClass,
            InjectInstance::class.java
        )
        val store = SharedStore.getStoreScopedToTestClass(context)
        for (field in injectableFields) {
            when (field.type) {
                WebServer::class.java -> field.set(testInstance, store.get(SERVER_INSTANCE))
                else -> field.set(
                    testInstance,
                    (store.get(USER_INSTANCES) as Map<*, *>)[field.type]
                )
            }
        }
    }

    override fun afterAll(context: ExtensionContext) {
        val testClass = context.requiredTestClass
        val store = SharedStore.getStoreScopedToTestClass(context)
        stopServer(store)
        destroyEndpoint(testClass, store)
    }

    private fun createEndpoint(
        testClass: Class<*>,
        store: ExtensionContext.Store
    ) {
        val instances = mutableMapOf<Class<*>, Any>()
        val endpoint = findStaticMethod(
            testClass,
            WebserverTest.CreateEndpoint::class.java
        )?.invoke(null, instances)
            ?: throw RuntimeException("Cannot find createEndpoint method in test class ${testClass.simpleName}")
        instances[endpoint::class.java] = endpoint
        store.put(USER_INSTANCES, instances)
        store.put(ENDPOINT_CLAZZ, endpoint::class.java)
    }

    @Suppress("UNCHECKED_CAST")
    private fun destroyEndpoint(
        testClass: Class<*>,
        store: ExtensionContext.Store
    ) {
        val endpointClazz = store.get(ENDPOINT_CLAZZ) as Class<*>
        val instances = store.get(USER_INSTANCES) as Map<Class<*>, Any>
        val endpoint = instances[endpointClazz]
        findStaticMethod(
            testClass,
            WebserverTest.DestroyEndpoint::class.java
        )?.invoke(null, endpoint, instances)
    }

    @Suppress("UNCHECKED_CAST")
    private fun startServer(
        testClass: Class<*>,
        store: ExtensionContext.Store
    ): WebServer {
        val serverBuilder = WebServer.builder()
            .config(loadedConfig.get("server"))
            .protocolsDiscoverServices(false)
            .port(0)
            .host("localhost")

        val endpointClazz = store.get(ENDPOINT_CLAZZ) as Class<*>
        val instances = store.get(USER_INSTANCES) as Map<Class<*>, Any>
        val endpoint = instances[endpointClazz]

        val clazz = Class.forName(INITIALIZERS_CLASS_NAME)
        val routes = HttpRouting.builder()
        val method = clazz.getDeclaredMethod(
            "registerRoutesFor",
            endpointClazz,
            HttpRouting.Builder::class.java
        )
        method.invoke(null, endpoint, routes)
        serverBuilder.routing(routes)

        findStaticMethod(
            testClass,
            WebserverTest.ConfigServer::class.java
        )?.invokeStaticMethodWithArgs(serverBuilder, store)
        val server = serverBuilder
            .build()
            .start()
        store.put(SERVER_INSTANCE, server)
        return server
    }

    private fun stopServer(store: ExtensionContext.Store) {
        val server = store.get(SERVER_INSTANCE) as WebServer
        server.stop()
    }

    @Suppress("UNCHECKED_CAST")
    private fun Method?.invokeStaticMethodWithArgs(
        firstArg: Any,
        store: ExtensionContext.Store
    ) {
        if (this == null) return
        val instances = store.get(USER_INSTANCES) as Map<Class<*>, Any>
        val argsCount = this.parameterCount
        if (argsCount == 1) {
            this.invoke(null, firstArg); return
        }
        val args = Array(argsCount) { firstArg }
        for (i in 1 until argsCount) {
            val type = this.parameters[i].type
            args[i] = instances[type]!!
        }
        this.invoke(
            null,
            *args
        )
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