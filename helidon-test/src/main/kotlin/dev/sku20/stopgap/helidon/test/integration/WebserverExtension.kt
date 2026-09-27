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
        private const val ENDPOINT_ROUTES_CLASS_NAME = "dev.sku20.stopgap.helidon.endpoint.generated.EndpointRoutesKt"
        private const val AUTH_INITIALIZER_CLASS_NAME = "dev.sku20.stopgap.helidon.authentication.generated.AuthenticationInitializerKt"
        private const val AUTH_RESOLVER_INTERFACE_NAME = "dev.sku20.stopgap.helidon.authentication.AuthenticationResolver"
    }

    override fun beforeAll(context: ExtensionContext) {
        val testClass = context.requiredTestClass
        val store = ItStore.storeFor(context)
        setup(testClass, store)
        startServer(testClass, store)
    }

    override fun postProcessTestInstance(testInstance: Any, context: ExtensionContext) {
        val injectableFields = AnnotationSupport.findAnnotatedFields(
            context.requiredTestClass,
            InjectInstance::class.java
        )
        val store = ItStore.storeFor(context)
        val setup = store.get(ItStoreKeys.SETUP) as SetupCapture
        for (field in injectableFields) {
            when (field.type) {
                WebServer::class.java -> field.set(testInstance, store.get(ItStoreKeys.SERVER))
                else -> {
                    val value = setup.instances[field.type]
                        ?: setup.authResolver?.takeIf { field.type.isInstance(it) }
                    field.set(testInstance, value)
                }
            }
        }
    }

    override fun afterAll(context: ExtensionContext) {
        val testClass = context.requiredTestClass
        val store = ItStore.storeFor(context)
        stopServer(store)
        cleanup(testClass, store)
    }

    private fun setup(testClass: Class<*>, store: ExtensionContext.Store) {
        val setup = findStaticMethod(testClass, WebserverTest.Setup::class.java)
            ?.invoke(null) as? SetupCapture
            ?: throw RuntimeException("Cannot find setup method in test class ${testClass.simpleName}")
        store.put(ItStoreKeys.SETUP, setup)
    }

    private fun cleanup(testClass: Class<*>, store: ExtensionContext.Store) {
        val setup = store.get(ItStoreKeys.SETUP) as SetupCapture
        findStaticMethod(testClass, WebserverTest.Cleanup::class.java)?.invoke(null, setup.instances)
    }

    private fun startServer(testClass: Class<*>, store: ExtensionContext.Store): WebServer {
        val serverBuilder = WebServer.builder()
            .config(loadedConfig.get("server"))
            .protocolsDiscoverServices(false)
            .port(0)
            .host("localhost")

        val setup = store.get(ItStoreKeys.SETUP) as SetupCapture
        val routes = HttpRouting.builder()
        initAuthentication(setup, routes)

        val clazz = Class.forName(ENDPOINT_ROUTES_CLASS_NAME)
        val method = findMethodWith(clazz, "registerRoutesFor", setup.endpoint::class.java, HttpRouting.Builder::class.java)
        method.invoke(null, setup.endpoint, routes, *setup.registerParams)
        serverBuilder.routing(routes)

        findStaticMethod(testClass, WebserverTest.ConfigServer::class.java)
            ?.invoke(null, serverBuilder, setup.instances)

        val server = serverBuilder.build().start()
        store.put(ItStoreKeys.SERVER, server)
        return server
    }

    private fun initAuthentication(setup: SetupCapture, routes: HttpRouting.Builder) {
        val resolver = setup.authResolver
            ?: setup.instances.entries.firstOrNull { entry ->
                entry.key.name == AUTH_RESOLVER_INTERFACE_NAME ||
                    try {
                        val authResolverClass = Class.forName(AUTH_RESOLVER_INTERFACE_NAME)
                        authResolverClass.isInstance(entry.value)
                    } catch (_: ClassNotFoundException) {
                        false
                    }
            }?.value
            ?: return

        try {
            val authInitClass = Class.forName(AUTH_INITIALIZER_CLASS_NAME)
            val method = authInitClass.methods.firstOrNull {
                it.name == "initAuthentication" && it.parameterCount == 2
            } ?: throw IllegalStateException("Cannot find initAuthentication method in $AUTH_INITIALIZER_CLASS_NAME")
            method.invoke(null, resolver, routes)
        } catch (_: ClassNotFoundException) {
            // Authentication codegen not applied; skip
        }
    }

    private fun stopServer(store: ExtensionContext.Store) {
        (store.get(ItStoreKeys.SERVER) as WebServer).stop()
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
