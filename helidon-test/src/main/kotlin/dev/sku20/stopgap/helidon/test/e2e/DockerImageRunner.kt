package dev.sku20.stopgap.helidon.test.e2e

import org.junit.platform.engine.support.store.Namespace
import org.junit.platform.engine.support.store.NamespacedHierarchicalStore
import org.junit.platform.launcher.LauncherSession
import org.junit.platform.launcher.LauncherSessionListener
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network

class DockerImageRunner : LauncherSessionListener {

    companion object {
        private const val DOCKER_IMAGE_NAME = "stopgap:e2e"
        private const val ENV_FILE = "application.env"
        const val SERVICE_NAME = "stopgap"
    }

    private val container = GenericContainer(DOCKER_IMAGE_NAME)
        .withExposedPorts(8080)
        .withLogConsumer { System.err.print(it.utf8String) }

    override fun launcherSessionOpened(session: LauncherSession) {
        setupContainerNetwork(session.store)
        setupEnv()
        container.start()
        session.store.put(E2eStore.NAMESPACE, E2eStoreKeys.PORT, container.getMappedPort(8080))
    }

    override fun launcherSessionClosed(session: LauncherSession) {
        container.stop()
    }

    private fun setupEnv() {
        val resource = this::class.java.classLoader.getResourceAsStream(ENV_FILE)!!
        for (line in resource.bufferedReader().lines()) {
            val split = line.split("=")
            container.withEnv(split[0], split[1])
        }
    }

    private fun setupContainerNetwork(store: NamespacedHierarchicalStore<Namespace>) {
        val network = store.get(E2eStore.NAMESPACE, E2eStoreKeys.NETWORK) as Network
        container.withNetwork(network).withNetworkAliases(SERVICE_NAME)
    }
}
