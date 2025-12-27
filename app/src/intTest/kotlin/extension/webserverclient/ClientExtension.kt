package extension.webserverclient

import extension.InjectInstance
import extension.SharedStore
import extension.webservertest.WebserverTestExtension.Companion.SERVER_INSTANCE
import io.helidon.webserver.WebServer
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestInstancePostProcessor
import org.junit.platform.commons.support.AnnotationSupport

class ClientExtension : BeforeAllCallback, TestInstancePostProcessor, AfterAllCallback {

    companion object {
        private val CLIENTS = "${::javaClass}::clients-key"
    }

    override fun beforeAll(context: ExtensionContext) {
        val store = SharedStore.getStoreScopedToTestClass(context)
        val server = store.get(SERVER_INSTANCE) as WebServer
        setupClients(server, store)
    }

    override fun postProcessTestInstance(
        testInstance: Any,
        context: ExtensionContext
    ) {
        val injectableFields = AnnotationSupport.findAnnotatedFields(
            context.requiredTestClass,
            InjectInstance::class.java
        )
        val store = SharedStore.getStoreScopedToTestClass(context)
        val clients = store.get(CLIENTS) as Clients
        for (field in injectableFields) {
            val client = clients.findClient(field.type)
            if (client != null) {
                field.set(testInstance, client)
            }
        }
    }

    override fun afterAll(context: ExtensionContext) {
        val store = SharedStore.getStoreScopedToTestClass(context)
        closeClient(store)
    }

    private fun setupClients(
        server: WebServer,
        store: ExtensionContext.Store
    ) {
        val host = server.prototype().host()
        val port = server.port()
        val clients = Clients()
        clients.setup(host, port)
        store.put(CLIENTS, clients)
    }

    private fun closeClient(store: ExtensionContext.Store) {
        val clients = store.get(CLIENTS) as Clients
        clients.closeClients()
    }

}