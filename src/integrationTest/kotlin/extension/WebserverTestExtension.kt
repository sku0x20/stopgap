package extension

import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

class WebserverTestExtension : BeforeAllCallback, BeforeEachCallback, AfterAllCallback {

    override fun beforeAll(context: ExtensionContext) {
        val store = getStore(context)
        store.put("test", "test2")
        System.err.println("before all extension: ${store.get("test")}")
    }

    override fun beforeEach(context: ExtensionContext) {
        val store = getStore(context)
        System.err.println("before each extension: ${store.get("test")}}")
    }

    override fun afterAll(context: ExtensionContext) {
        val store = getStore(context)
        System.err.println("after each extension ${store.get("test")}}")
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