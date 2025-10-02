package extension

import com.example.stopgap.instanceregistry.Config
import com.example.stopgap.instanceregistry.InstanceRegistry
import io.helidon.webserver.WebServer
import org.junit.jupiter.api.extension.*
import org.junit.platform.commons.support.AnnotationSupport
import org.junit.platform.commons.support.HierarchyTraversalMode
import org.junit.platform.commons.support.ModifierSupport
import org.mockito.kotlin.mock

class WebserverTestExtension : BeforeAllCallback, BeforeEachCallback, TestInstancePostProcessor,
    AfterAllCallback {

    companion object {
        private const val IS_CONFIG_MOCKED = "is-config-mocked-key"
        private const val CONFIG = "config-key"
        private const val INSTANCE_REGISTRY = "instance-registry-key"
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
        System.err.println("postProcessTestInstance extension: ${store.get(SERVER_INSTANCE)}")
    }

    override fun beforeEach(context: ExtensionContext) {
        val store = getStore(context)
        System.err.println("before each extension: ${store.get(SERVER_INSTANCE)}")
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
        val methods = AnnotationSupport.findAnnotatedMethods(
            testClass,
            WebserverTest.CreateConfig::class.java,
            HierarchyTraversalMode.TOP_DOWN
        )
        if (methods.size > 1) {
            throw IllegalStateException("Only one method can be annotated with @CreateConfig")
        }
        if (methods.isEmpty()) {
            val mock = mock<Config>()
            store.put(IS_CONFIG_MOCKED, true)
            store.put(CONFIG, mock)
            return mock
        }
        val member = methods[0]
        if (ModifierSupport.isNotStatic(member)) {
            throw IllegalStateException("@CreateConfig method must be static")
        }
        val config = member.invoke(null) as Config
        store.put(IS_CONFIG_MOCKED, false)
        store.put(CONFIG, config)
        return config
    }

    private fun setupInstanceRegistry(
        testClass: Class<*>,
        config: Config,
        store: ExtensionContext.Store
    ): InstanceRegistry {
        val methods = AnnotationSupport.findAnnotatedMethods(
            testClass,
            WebserverTest.SetupInstanceRegistry::class.java,
            HierarchyTraversalMode.TOP_DOWN
        )
        if (methods.size > 1) {
            throw IllegalStateException("Only one method can be annotated with @SetupInstanceRegistry")
        }
        val member = methods[0]
        if (ModifierSupport.isNotStatic(member)) {
            throw IllegalStateException("@SetupInstanceRegistry method must be static")
        }
        val registry = InstanceRegistry(config)
        member.invoke(null, registry)
        store.put(INSTANCE_REGISTRY, registry)
        return registry
    }

    private fun setupServer(
        testClass: Class<*>,
        registry: InstanceRegistry,
        store: ExtensionContext.Store
    ) {
        val server = WebServer.builder()
            .port(0)
            .host("localhost")
            .protocolsDiscoverServices(false)
//            .routing(mainEndpoint.routing(instanceRegistry))
            .build()
        server.start()
        store.put(SERVER_INSTANCE, server)
    }

    private fun stopServer(store: ExtensionContext.Store) {
        val server = store.get(SERVER_INSTANCE) as WebServer
        server.stop()
    }

}