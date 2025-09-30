package extension

import com.example.stopgap.instanceregistry.Config
import com.example.stopgap.instanceregistry.InstanceRegistry
import org.junit.jupiter.api.extension.*
import org.junit.platform.commons.support.AnnotationSupport
import org.junit.platform.commons.support.HierarchyTraversalMode
import org.junit.platform.commons.support.ModifierSupport
import org.mockito.kotlin.mock

class WebserverTestExtension : BeforeAllCallback, BeforeEachCallback, TestInstancePostProcessor, AfterAllCallback {

    companion object {
        private const val SERVER_INSTANCE = "webserver-instance-key"
        private const val CONFIG = "config-key"
        private const val IS_CONFIG_MOCKED = "is-config-mocked-key"
        private const val INSTANCE_REGISTRY = "instance-registry-key"
    }

    override fun beforeAll(context: ExtensionContext) {
        val store = getStore(context)

        val config = createConfig(context.requiredTestClass, store)

        val registry = InstanceRegistry(config)
        setupInstanceRegistry(context.requiredTestClass, registry)

        store.put(INSTANCE_REGISTRY, registry)
//        store.put(SERVER_INSTANCE, "some-value")
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
        System.err.println("after each extension ${store.get(SERVER_INSTANCE)}")
    }

    private fun getStore(context: ExtensionContext): ExtensionContext.Store {
        // scope to test class
        val nameSpace = ExtensionContext.Namespace.create(
            this::class.java,
            context.requiredTestClass,
        )
        return context.getStore(nameSpace)
    }

    private fun createConfig(
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
        registry: InstanceRegistry
    ) {
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
        member.invoke(null, registry)
    }

}