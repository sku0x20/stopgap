package dev.sku20.stopgap.helidon.test.e2e

import org.junit.platform.launcher.LauncherSession
import org.junit.platform.launcher.LauncherSessionListener
import org.testcontainers.containers.Network

class DockerNetworkManager : LauncherSessionListener {

    private lateinit var network: Network

    override fun launcherSessionOpened(session: LauncherSession) {
        network = Network.newNetwork()
        session.store.put(E2eStore.NAMESPACE, E2eStoreKeys.NETWORK, network)
    }

    override fun launcherSessionClosed(session: LauncherSession) {
        network.close()
    }
}
