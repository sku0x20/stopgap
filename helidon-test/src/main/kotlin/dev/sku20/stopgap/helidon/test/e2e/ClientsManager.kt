package dev.sku20.stopgap.helidon.test.e2e

import dev.sku20.stopgap.helidon.test.client.ManagedClients
import org.junit.platform.launcher.LauncherSession
import org.junit.platform.launcher.LauncherSessionListener

class ClientsManager : LauncherSessionListener {

    private lateinit var clients: ManagedClients

    override fun launcherSessionOpened(session: LauncherSession) {
        val port = session.store.get(E2eStore.NAMESPACE, E2eStoreKeys.PORT) as Int
        clients = ManagedClients()
        clients.setup("localhost", port)
        session.store.put(E2eStore.NAMESPACE, E2eStoreKeys.CLIENTS, clients)
    }

    override fun launcherSessionClosed(session: LauncherSession) {
        clients.closeClients()
    }
}
