package dev.sku20.stopgap.helidon.test.e2e

import dev.sku20.stopgap.helidon.test.InjectInstance
import dev.sku20.stopgap.helidon.test.client.ManagedClients
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestInstancePostProcessor
import org.junit.platform.commons.support.AnnotationSupport

class ClientExtension : TestInstancePostProcessor {

    override fun postProcessTestInstance(testInstance: Any, context: ExtensionContext) {
        val injectableFields = AnnotationSupport.findAnnotatedFields(
            context.requiredTestClass,
            InjectInstance::class.java
        )
        val clients = context.getStore(E2eStore.EXTENSION_NAMESPACE)
            .get(E2eStoreKeys.CLIENTS) as ManagedClients
        for (field in injectableFields) {
            val client = clients.findClient(field.type)
            if (client != null) field.set(testInstance, client)
        }
    }
}
