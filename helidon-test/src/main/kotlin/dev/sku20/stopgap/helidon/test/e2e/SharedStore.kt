package dev.sku20.stopgap.helidon.test.e2e

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.platform.engine.support.store.Namespace

object SharedStore {
    val NAMESPACE: Namespace = Namespace.create("e2e")
    val EXTENSION_NAMESPACE: ExtensionContext.Namespace = ExtensionContext.Namespace.create("e2e")
}
