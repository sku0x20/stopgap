package extension

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.platform.engine.support.store.Namespace

object SharedStore {

    private const val NAME = "GLOBAL_STORE_NAMESPACE"
    val GLOBAL_NAMESPACE = Namespace.create(NAME)
    val GLOBAL_EXTENSION_NAMESPACE = ExtensionContext.Namespace.create(NAME)

}