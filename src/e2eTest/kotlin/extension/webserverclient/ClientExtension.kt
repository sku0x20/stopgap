package extension.webserverclient

import extension.InjectInstance
import extension.SharedStore
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestInstancePostProcessor
import org.junit.platform.commons.support.AnnotationSupport

class ClientExtension : TestInstancePostProcessor {

    override fun postProcessTestInstance(
        testInstance: Any,
        context: ExtensionContext
    ) {
        val injectableFields = AnnotationSupport.findAnnotatedFields(
            context.requiredTestClass,
            InjectInstance::class.java
        )
        val store = context.getStore(SharedStore.GLOBAL_EXTENSION_NAMESPACE)
        val clients = store.get(ClientsManager.CLIENTS_ID) as Clients
        for (field in injectableFields) {
            val client = clients.findClient(field.type)
            if (client != null) {
                field.set(testInstance, client)
            }
        }
    }

}