package dev.sku20.stopgap.helidon.test.integration

import dev.sku20.stopgap.helidon.test.InjectInstance
import dev.sku20.stopgap.helidon.test.client.ManagedClients
import io.helidon.webserver.WebServer
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestInstancePostProcessor
import org.junit.platform.commons.support.AnnotationSupport

class ClientExtension : BeforeAllCallback, TestInstancePostProcessor, AfterAllCallback {

    private fun storeFor(context: ExtensionContext): ExtensionContext.Store {
        return context.getStore(ItStore.namespace(context.requiredTestClass))
    }

    override fun beforeAll(context: ExtensionContext) {
        val store = storeFor(context)
        val server = store.get(ItStoreKeys.SERVER) as WebServer
        val clients = ManagedClients()
        clients.setup(server.prototype().host(), server.port())
        store.put(ItStoreKeys.CLIENTS, clients)
    }

    override fun postProcessTestInstance(testInstance: Any, context: ExtensionContext) {
        val injectableFields = AnnotationSupport.findAnnotatedFields(
            context.requiredTestClass,
            InjectInstance::class.java
        )
        val clients = storeFor(context).get(ItStoreKeys.CLIENTS) as ManagedClients
        for (field in injectableFields) {
            val client = clients.findClient(field.type)
            if (client != null) field.set(testInstance, client)
        }
    }

    override fun afterAll(context: ExtensionContext) {
        val clients = storeFor(context).get(ItStoreKeys.CLIENTS) as ManagedClients
        clients.closeClients()
    }
}
