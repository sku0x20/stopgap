package dev.sku20.stopgap.helidon.test.e2e

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.platform.engine.support.store.Namespace

object E2eStore {
    val NAMESPACE: Namespace = Namespace.create("e2e")
    val EXTENSION_NAMESPACE: ExtensionContext.Namespace = ExtensionContext.Namespace.create("e2e")
}

object E2eStoreKeys {
    const val NETWORK      = "e2e.network"
    const val PORT         = "e2e.port"
    const val CLIENTS      = "e2e.clients"
    const val DOCKER_IMAGE = "e2e.docker.image"
    const val SERVICE_NAME = "e2e.docker.service-name"
    const val ENV_FILE     = "e2e.docker.env-file"
}
