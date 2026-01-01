package extension

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.platform.engine.support.store.Namespace

object SharedStore {

    private const val GLOBAL_STORE_ID = "namespace.global"
    val GLOBAL_NAMESPACE: Namespace = Namespace.create(GLOBAL_STORE_ID)
    val GLOBAL_EXTENSION_NAMESPACE: ExtensionContext.Namespace = ExtensionContext.Namespace.create(GLOBAL_STORE_ID)


    fun getStoreScopedToTestClass(context: ExtensionContext): ExtensionContext.Store {
        val nameSpace = ExtensionContext.Namespace.create(
            this::class.java,
            context.requiredTestClass,
        )
        return context.getStore(nameSpace)
    }

}