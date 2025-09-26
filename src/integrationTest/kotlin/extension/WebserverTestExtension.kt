package extension

import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

class WebserverTestExtension : BeforeAllCallback, BeforeEachCallback, AfterAllCallback {

    override fun beforeAll(context: ExtensionContext) {
        System.err.println("before all extension")
    }

    override fun beforeEach(context: ExtensionContext) {
        System.err.println("before each extension")
    }

    override fun afterAll(context: ExtensionContext) {
        System.err.println("after each extension")
    }

}