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
 * Junit Extensions also try to abstract that out, so it does not affect extensions.
 */
class WebserverTestExtension : BeforeAllCallback, TestInstancePostProcessor, AfterAllCallback {

    companion object {
        const val SERVER_INSTANCE = "server.instance"

        private val loadedConfig = Config.create()
        private const val USER_INSTANCES = "server.user.instances"
    }

    override fun beforeAll(context: ExtensionContext) {
        val testClass = context.requiredTestClass
        val store = SharedStore.getStoreScopedToTestClass(context)
        createInstances(testClass, store)
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
        val store = SharedStore.getStoreScopedToTestClass(context)
        stopServer(store)
    }

    private fun createInstances(
        testClass: Class<*>,
        store: ExtensionContext.Store
    ) {
        val instances = mutableMapOf<Class<*>, Any>()
        findStaticMethod(
            testClass,
            WebserverTest.CreateInstances::class.java
        )?.invoke(null, instances)
        store.put(USER_INSTANCES, instances)
    }

    private fun startServer(
        testClass: Class<*>,
        store: ExtensionContext.Store
    ): WebServer {
        val serverBuilder = WebServer.builder()
            .config(loadedConfig.get("server"))
            .protocolsDiscoverServices(false)
            .port(0)
            .host("localhost")

        val routes = HttpRouting.builder()
        serverBuilder.routing(routes)
        findStaticMethod(
            testClass,
            WebserverTest.ConfigRoutes::class.java
        )?.invokeStaticMethodWithArgs(routes, store)

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