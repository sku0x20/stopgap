package dev.sku20.stopgap.helidon.test.integration.extension

import dev.sku20.stopgap.helidon.test.InjectInstance
import dev.sku20.stopgap.helidon.test.StoreKeys
import dev.sku20.stopgap.helidon.test.client.ManagedClients
import io.helidon.webserver.WebServer
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestInstancePostProcessor
import org.junit.platform.commons.support.AnnotationSupport

class ClientExtension : BeforeAllCallback, TestInstancePostProcessor, AfterAllCallback {

    private fun storeFor(context: ExtensionContext): ExtensionContext.Store {
        return context.getStore(
            ExtensionContext.Namespace.create(ClientExtension::class.java, context.requiredTestClass)
        )
    }

    private fun webserverStoreFor(context: ExtensionContext): ExtensionContext.Store {
        return context.getStore(
            ExtensionContext.Namespace.create(WebserverExtension::class.java, context.requiredTestClass)
        )
    }

    override fun beforeAll(context: ExtensionContext) {
        val server = webserverStoreFor(context).get(StoreKeys.It.SERVER) as WebServer
        val clients = ManagedClients()
        clients.setup(server.prototype().host(), server.port())
        storeFor(context).put(StoreKeys.It.CLIENTS, clients)
    }

    override fun postProcessTestInstance(testInstance: Any, context: ExtensionContext) {
        val injectableFields = AnnotationSupport.findAnnotatedFields(
            context.requiredTestClass,
            InjectInstance::class.java
        )
        val clients = storeFor(context).get(StoreKeys.It.CLIENTS) as ManagedClients
        for (field in injectableFields) {
            val client = clients.findClient(field.type)
            if (client != null) field.set(testInstance, client)
        }
    }

    override fun afterAll(context: ExtensionContext) {
        val clients = storeFor(context).get(StoreKeys.It.CLIENTS) as ManagedClients
        clients.closeClients()
    }
}
