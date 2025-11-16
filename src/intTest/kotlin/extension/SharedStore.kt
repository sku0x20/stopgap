package extension

import org.junit.jupiter.api.extension.ExtensionContext

object SharedStore {

    fun getStoreScopedToTestClass(context: ExtensionContext): ExtensionContext.Store {
        val nameSpace = ExtensionContext.Namespace.create(
            this::class.java,
            context.requiredTestClass,
        )
        return context.getStore(nameSpace)
    }

}