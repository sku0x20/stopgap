package dev.sku20.stopgap.helidon.test.e2e

import dev.sku20.stopgap.helidon.test.client.ManagedClients
import org.junit.platform.launcher.LauncherSession
import org.junit.platform.launcher.LauncherSessionListener

class ClientsManager : LauncherSessionListener {

    private lateinit var clients: ManagedClients

    override fun launcherSessionOpened(session: LauncherSession) {
        val port = session.store.get(SharedStore.NAMESPACE, StoreKeys.PORT) as Int
        clients = ManagedClients()
        clients.setup("localhost", port)
        session.store.put(SharedStore.NAMESPACE, StoreKeys.CLIENTS, clients)
    }

    override fun launcherSessionClosed(session: LauncherSession) {
        clients.closeClients()
    }
}
