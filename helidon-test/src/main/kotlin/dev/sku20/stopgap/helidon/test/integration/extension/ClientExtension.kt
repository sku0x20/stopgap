package dev.sku20.stopgap.helidon.test.integration.extension

import dev.sku20.stopgap.helidon.test.InjectInstance
import dev.sku20.stopgap.helidon.test.client.Clients
import dev.sku20.stopgap.helidon.test.store.SharedStore
import io.helidon.webserver.WebServer
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestInstancePostProcessor
import org.junit.platform.commons.support.AnnotationSupport

class ClientExtension : BeforeAllCallback, TestInstancePostProcessor, AfterAllCallback {

    companion object {
        private const val CLIENTS_ID = "clients"
    }

    override fun beforeAll(context: ExtensionContext) {
        val store = SharedStore.getStoreScopedToTestClass(context)
        val server = store.get(WebserverExtension.SERVER_INSTANCE_ID) as WebServer
        setupClients(server, store)
    }

    override fun postProcessTestInstance(testInstance: Any, context: ExtensionContext) {
        val injectableFields = AnnotationSupport.findAnnotatedFields(
            context.requiredTestClass,
            InjectInstance::class.java
        )
        val store = SharedStore.getStoreScopedToTestClass(context)
        val clients = store.get(CLIENTS_ID) as Clients
        for (field in injectableFields) {
            val client = clients.findClient(field.type)
            if (client != null) field.set(testInstance, client)
        }
    }

    override fun afterAll(context: ExtensionContext) {
        val store = SharedStore.getStoreScopedToTestClass(context)
        val clients = store.get(CLIENTS_ID) as Clients
        clients.closeClients()
    }

    private fun setupClients(server: WebServer, store: ExtensionContext.Store) {
        val clients = Clients()
        clients.setup(server.prototype().host(), server.port())
        store.put(CLIENTS_ID, clients)
    }
}
