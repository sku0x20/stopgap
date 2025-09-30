package extension

import com.example.stopgap.instanceregistry.Config
import com.example.stopgap.instanceregistry.InstanceRegistry
import org.junit.jupiter.api.extension.*
import org.junit.platform.commons.support.AnnotationSupport
import org.junit.platform.commons.support.HierarchyTraversalMode
import org.junit.platform.commons.support.ModifierSupport
import org.mockito.kotlin.mock
import java.lang.reflect.Method

class WebserverTestExtension : BeforeAllCallback, BeforeEachCallback, TestInstancePostProcessor, AfterAllCallback {

    companion object {
        private const val SERVER_INSTANCE = "webserver-instance-key"
        private const val CONFIG = "config-key"
        private const val INSTANCE_REGISTRY = "instance-registry-key"
    }

    override fun beforeAll(context: ExtensionContext) {
        val store = getStore(context)

        val config = mock<Config>()
        val registry = InstanceRegistry(config)

        val setupInstanceRegistry = setupInstanceRegistryFunction(context)
        setupInstanceRegistry.invoke(null, registry)

        store.put(CONFIG, config)
        store.put(INSTANCE_REGISTRY, registry)
//        store.put(SERVER_INSTANCE, "some-value")
    }

    private fun setupInstanceRegistryFunction(context: ExtensionContext): Method {
        val setupInstanceRegistryFunctions = AnnotationSupport.findAnnotatedMethods(
            context.requiredTestClass,
            WebserverTest.SetupInstanceRegistry::class.java,
            HierarchyTraversalMode.TOP_DOWN
        )
        if (setupInstanceRegistryFunctions.size > 1) {
            throw IllegalStateException("Only one method can be annotated with @SetupInstanceRegistry")
        }
        val member = setupInstanceRegistryFunctions[0]
        if (ModifierSupport.isNotStatic(member)) {
            throw IllegalStateException("@SetupInstanceRegistry method must be static")
        }
        return member
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

}