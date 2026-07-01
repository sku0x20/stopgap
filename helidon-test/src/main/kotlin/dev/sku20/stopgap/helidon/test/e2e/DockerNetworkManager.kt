package dev.sku20.stopgap.helidon.test.e2e

import dev.sku20.stopgap.helidon.test.StoreKeys
import org.junit.platform.engine.support.store.Namespace
import org.junit.platform.launcher.LauncherSession
import org.junit.platform.launcher.LauncherSessionListener
import org.testcontainers.containers.Network

class DockerNetworkManager : LauncherSessionListener {

    private val namespace = Namespace.create(DockerNetworkManager::class.java)

    private lateinit var network: Network

    override fun launcherSessionOpened(session: LauncherSession) {
        network = Network.newNetwork()
        session.store.put(namespace, StoreKeys.E2e.NETWORK, network)
    }

    override fun launcherSessionClosed(session: LauncherSession) {
        network.close()
    }
}
