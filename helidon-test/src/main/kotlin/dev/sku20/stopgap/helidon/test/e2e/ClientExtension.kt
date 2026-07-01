package dev.sku20.stopgap.helidon.test.e2e

import dev.sku20.stopgap.helidon.test.InjectInstance
import dev.sku20.stopgap.helidon.test.client.ManagedClients
import dev.sku20.stopgap.helidon.test.store.SharedStore
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestInstancePostProcessor
import org.junit.platform.commons.support.AnnotationSupport

class ClientExtension : TestInstancePostProcessor {

    override fun postProcessTestInstance(testInstance: Any, context: ExtensionContext) {
        val injectableFields = AnnotationSupport.findAnnotatedFields(
            context.requiredTestClass,
            InjectInstance::class.java
        )
        val store = context.getStore(SharedStore.GLOBAL_EXTENSION_NAMESPACE)
        val clients = store.get(ClientsManager.CLIENTS_ID) as ManagedClients
        for (field in injectableFields) {
            val client = clients.findClient(field.type)
            if (client != null) field.set(testInstance, client)
        }
    }
}
