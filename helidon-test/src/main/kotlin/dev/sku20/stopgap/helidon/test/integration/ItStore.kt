package dev.sku20.stopgap.helidon.test.integration

import org.junit.jupiter.api.extension.ExtensionContext

object ItStore {
    fun namespace(testClass: Class<*>): ExtensionContext.Namespace =
        ExtensionContext.Namespace.create("it", testClass)

    fun storeFor(context: ExtensionContext): ExtensionContext.Store =
        context.getStore(namespace(context.requiredTestClass))
}

object ItStoreKeys {
    const val SERVER  = "it.server"
    const val SETUP   = "it.setup"
    const val CLIENTS = "it.clients"
}
