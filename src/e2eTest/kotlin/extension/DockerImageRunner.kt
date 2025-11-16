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

        System.setProperty("container.server.port", container.getMappedPort(8080).toString())
    }

    override fun launcherSessionClosed(session: LauncherSession) {
        container.stop()
    }

    private fun setupEnv() {
        val resource = this::class.java.classLoader.getResourceAsStream(ENV_FILE)!!
        for (line in resource.bufferedReader().lines()) {
            val split = line.split("=")
            val key = split[0]
            val value = split[1]
            container.withEnv(key, value)
        }
    }

    private fun setupContainerNetwork(store: NamespacedHierarchicalStore<Namespace>) {
        val network = store.get(
            SharedStore.GLOBAL_NAMESPACE,
            DockerNetworkManager.NETWORK_ID
        ) as Network

        container.withNetwork(network)
            .withNetworkAliases(SERVICE_NAME)
    }
}