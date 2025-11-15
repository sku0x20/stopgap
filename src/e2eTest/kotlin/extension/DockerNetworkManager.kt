package extension

import org.junit.platform.launcher.LauncherSession
import org.junit.platform.launcher.LauncherSessionListener
import org.testcontainers.containers.Network

class DockerNetworkManager : LauncherSessionListener {

    companion object {
        const val NETWORK_ID = "network.name"
    }

    private lateinit var network: Network

    override fun launcherSessionOpened(session: LauncherSession) {
        network = Network.newNetwork()
        session.store.put(
            SharedStore.GLOBAL_STORE_NAMESPACE,
            NETWORK_ID,
            network
        )
    }

    override fun launcherSessionClosed(session: LauncherSession) {
        network.close()
    }
}