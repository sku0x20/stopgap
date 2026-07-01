package dev.sku20.stopgap.helidon.test.e2e

import dev.sku20.stopgap.helidon.test.client.ManagedClients
import dev.sku20.stopgap.helidon.test.store.SharedStore
import org.junit.platform.launcher.LauncherSession
import org.junit.platform.launcher.LauncherSessionListener

class ClientsManager : LauncherSessionListener {

    companion object {
        const val CLIENTS_ID = "clients"
    }

    private lateinit var clients: ManagedClients

    override fun launcherSessionOpened(session: LauncherSession) {
        val port = session.store.get(SharedStore.GLOBAL_NAMESPACE, DockerImageRunner.SERVER_PORT_ID) as Int
        clients = ManagedClients()
        clients.setup("localhost", port)
        session.store.put(SharedStore.GLOBAL_NAMESPACE, CLIENTS_ID, clients)
    }

    override fun launcherSessionClosed(session: LauncherSession) {
        clients.closeClients()
    }
}
