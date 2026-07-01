package dev.sku20.stopgap.helidon.test.integration

import dev.sku20.stopgap.helidon.test.InjectInstance
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
import org.junit.platform.commons.support.ReflectionSupport
import java.lang.reflect.Method

class WebserverExtension : BeforeAllCallback, TestInstancePostProcessor, AfterAllCallback {

    companion object {
        private val loadedConfig = Config.create()
        private const val INITIALIZERS_CLASS_NAME = "dev.sku20.stopgap.helidon.endpoint.generated.InitializersKt"
    }

    private fun storeFor(context: ExtensionContext): ExtensionContext.Store {
        return context.getStore(
            ExtensionContext.Namespace.create(WebserverExtension::class.java, context.requiredTestClass)
        )
    }

    override fun beforeAll(context: ExtensionContext) {
        val testClass = context.requiredTestClass
        val store = storeFor(context)
        setup(testClass, store)
        startServer(testClass, store)
    }

    override fun postProcessTestInstance(testInstance: Any, context: ExtensionContext) {
        val injectableFields = AnnotationSupport.findAnnotatedFields(
            context.requiredTestClass,
            InjectInstance::class.java
        )
        val store = storeFor(context)
        for (field in injectableFields) {
            when (field.type) {
                WebServer::class.java -> field.set(testInstance, store.get(StoreKeys.SERVER))
                else -> field.set(
                    testInstance,
                    (store.get(StoreKeys.SETUP) as SetupCapture).instances[field.type]
                )
            }
        }
    }

    override fun afterAll(context: ExtensionContext) {
        val testClass = context.requiredTestClass
        val store = storeFor(context)
        stopServer(store)
        cleanup(testClass, store)
    }

    private fun setup(testClass: Class<*>, store: ExtensionContext.Store) {
        val setup = findStaticMethod(testClass, WebserverTest.Setup::class.java)
            ?.invoke(null) as? SetupCapture
            ?: throw RuntimeException("Cannot find setup method in test class ${testClass.simpleName}")
        store.put(StoreKeys.SETUP, setup)
    }

    private fun cleanup(testClass: Class<*>, store: ExtensionContext.Store) {
        val setup = store.get(StoreKeys.SETUP) as SetupCapture
        findStaticMethod(testClass, WebserverTest.Cleanup::class.java)?.invoke(null, setup.instances)
    }

    private fun startServer(testClass: Class<*>, store: ExtensionContext.Store): WebServer {
        val serverBuilder = WebServer.builder()
            .config(loadedConfig.get("server"))
            .protocolsDiscoverServices(false)
            .port(0)
            .host("localhost")

        val setup = store.get(StoreKeys.SETUP) as SetupCapture
        val clazz = Class.forName(INITIALIZERS_CLASS_NAME)
        val method = findMethodWith(clazz, "registerRoutesFor", setup.endpoint::class.java, HttpRouting.Builder::class.java)
        val routes = HttpRouting.builder()
        method.invoke(null, setup.endpoint, routes, *setup.registerParams)
        serverBuilder.routing(routes)

        findStaticMethod(testClass, WebserverTest.ConfigServer::class.java)
            ?.invoke(null, serverBuilder, setup.instances)

        val server = serverBuilder.build().start()
        store.put(StoreKeys.SERVER, server)
        return server
    }

    private fun stopServer(store: ExtensionContext.Store) {
        (store.get(StoreKeys.SERVER) as WebServer).stop()
    }

    private fun findStaticMethod(testClass: Class<*>, annotation: Class<out Annotation>): Method? {
        val methods = AnnotationSupport.findAnnotatedMethods(testClass, annotation, HierarchyTraversalMode.TOP_DOWN)
        if (methods.size > 1) throw IllegalStateException("Only one method can be annotated with ${annotation.name}")
        if (methods.isEmpty()) return null
        val member = methods[0]
        if (ModifierSupport.isNotStatic(member)) throw IllegalStateException("${annotation.name} method must be static")
        return member
    }

    @Suppress("SameParameterValue")
    private fun findMethodWith(clazz: Class<*>, methodName: String, vararg params: Class<*>): Method {
        val methods = ReflectionSupport.findMethods(clazz, {
            if (it.name != methodName) return@findMethods false
            val parameters = it.parameters
            if (params.size > parameters.size) return@findMethods false
            for (i in params.indices) {
                if (parameters[i].type != params[i]) return@findMethods false
            }
            return@findMethods true
        }, HierarchyTraversalMode.BOTTOM_UP)
        if (methods.isEmpty()) throw IllegalStateException("No method found with name $methodName and parameters ${params.joinToString { it.name }}")
        return methods[0]
    }
}
