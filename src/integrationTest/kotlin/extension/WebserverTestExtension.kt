package extension

import org.junit.jupiter.api.extension.*

class WebserverTestExtension : BeforeAllCallback, BeforeEachCallback, TestInstancePostProcessor, AfterAllCallback {

    companion object {
        private const val SERVER_INSTANCE = "webserver-instance-key"
    }

    override fun beforeAll(context: ExtensionContext) {
        val store = getStore(context)
        store.put(SERVER_INSTANCE, "some-value")
        System.err.println("before all extension: ${store.get(SERVER_INSTANCE)}")
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