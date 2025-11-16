package extension.webserverclient

import SharedStore
import extension.DockerImageRunner
import org.junit.platform.launcher.LauncherSession
import org.junit.platform.launcher.LauncherSessionListener

class ClientsManager : LauncherSessionListener {

    companion object {
        const val CLIENTS_ID = "clients"
    }

    private lateinit var clients: Clients

    override fun launcherSessionOpened(session: LauncherSession) {
        val host = "localhost"
        val port = session.store.get(
            SharedStore.GLOBAL_NAMESPACE,
            DockerImageRunner.SERVER_PORT_ID
        ) as Int
        clients = Clients()
        clients.setup(host, port)

        session.store.put(
            SharedStore.GLOBAL_NAMESPACE,
            CLIENTS_ID,
            clients
        )
    }

    override fun launcherSessionClosed(session: LauncherSession) {
        clients.closeClients()
    }
}