package extension

import org.junit.platform.engine.support.store.Namespace
import org.junit.platform.engine.support.store.NamespacedHierarchicalStore
import org.junit.platform.launcher.LauncherSession
import org.junit.platform.launcher.LauncherSessionListener
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network


class DockerImageRunner : LauncherSessionListener {

    companion object {
        private const val DOCKER_IMAGE_NAME = "stopgap:e2e"
        const val SERVICE_NAME = "stopgap"
    }

    private val container = GenericContainer(DOCKER_IMAGE_NAME)
        .withExposedPorts(8080)

    override fun launcherSessionOpened(session: LauncherSession) {
        setupContainerNetwork(session.store)
        container.start()

        System.setProperty("container.server.port", container.getMappedPort(8080).toString())
        System.err.println("container started: ${container.containerId}")
    }

    override fun launcherSessionClosed(session: LauncherSession) {
        container.stop()
        System.err.println("container stopped")

    }

    private fun setupContainerNetwork(store: NamespacedHierarchicalStore<Namespace>) {
        val network = store.get(
            SharedStore.GLOBAL_STORE_NAMESPACE,
            DockerNetworkManager.NETWORK_ID
        ) as Network

        container.withNetwork(network)
            .withNetworkAliases(SERVICE_NAME)
    }
}