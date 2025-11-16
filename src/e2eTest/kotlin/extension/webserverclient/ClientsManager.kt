package extension.webserverclient

import extension.DockerImageRunner
import extension.SharedStore
import org.junit.platform.launcher.LauncherSession
import org.junit.platform.launcher.LauncherSessionListener

class ClientsManager : LauncherSessionListener {

    private lateinit var clients: Clients

    override fun launcherSessionOpened(session: LauncherSession) {
        val host = "localhost"
        val port = session.store.get(
            SharedStore.GLOBAL_NAMESPACE,
            DockerImageRunner.SERVER_PORT_ID
        ) as Int
        clients = Clients()
        clients.setup(host, port)
    }

    override fun launcherSessionClosed(session: LauncherSession) {
        clients.closeClients()
    }
}