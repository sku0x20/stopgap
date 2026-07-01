package dev.sku20.stopgap.helidon.test.e2e

import org.junit.platform.engine.support.store.Namespace
import org.junit.platform.engine.support.store.NamespacedHierarchicalStore
import org.junit.platform.launcher.LauncherSession
import org.junit.platform.launcher.LauncherSessionListener
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network

class DockerImageRunner : LauncherSessionListener {

    private lateinit var container: GenericContainer<*>

    override fun launcherSessionOpened(session: LauncherSession) {
        val store = session.store
        val image = store.get(E2eStore.NAMESPACE, E2eStoreKeys.DOCKER_IMAGE) as String
        val serviceName = store.get(E2eStore.NAMESPACE, E2eStoreKeys.SERVICE_NAME) as String
        container = GenericContainer(image)
            .withExposedPorts(8080)
            .withLogConsumer { System.err.print(it.utf8String) }
        setupContainerNetwork(store, serviceName)
        setupEnv(store)
        container.start()
        store.put(E2eStore.NAMESPACE, E2eStoreKeys.PORT, container.getMappedPort(8080))
    }

    override fun launcherSessionClosed(session: LauncherSession) {
        container.stop()
    }

    private fun setupEnv(store: NamespacedHierarchicalStore<Namespace>) {
        val envFile = store.get(E2eStore.NAMESPACE, E2eStoreKeys.ENV_FILE) as? String ?: return
        val resource = this::class.java.classLoader.getResourceAsStream(envFile)!!
        for (line in resource.bufferedReader().lines()) {
            val split = line.split("=")
            container.withEnv(split[0], split[1])
        }
    }

    private fun setupContainerNetwork(store: NamespacedHierarchicalStore<Namespace>, serviceName: String) {
        val network = store.get(E2eStore.NAMESPACE, E2eStoreKeys.NETWORK) as Network
        container.withNetwork(network).withNetworkAliases(serviceName)
    }
}
