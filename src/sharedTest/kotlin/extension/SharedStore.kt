package extension

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.platform.engine.support.store.Namespace

object SharedStore {

    private const val GLOBAL_STORE = "GLOBAL_STORE_NAMESPACE"
    val GLOBAL_NAMESPACE = Namespace.create(GLOBAL_STORE)
    val GLOBAL_EXTENSION_NAMESPACE = ExtensionContext.Namespace.create(GLOBAL_STORE)


    fun getStoreScopedToTestClass(context: ExtensionContext): ExtensionContext.Store {
        val nameSpace = ExtensionContext.Namespace.create(
            this::class.java,
            context.requiredTestClass,
        )
        return context.getStore(nameSpace)
    }

}