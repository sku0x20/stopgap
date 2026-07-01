package dev.sku20.stopgap.helidon.test.e2e

import dev.sku20.stopgap.helidon.test.StoreKeys
import dev.sku20.stopgap.helidon.test.client.ManagedClients
import org.junit.platform.engine.support.store.Namespace
import org.junit.platform.launcher.LauncherSession
import org.junit.platform.launcher.LauncherSessionListener

class ClientsManager : LauncherSessionListener {

    private val namespace = Namespace.create(ClientsManager::class.java)

    private lateinit var clients: ManagedClients

    override fun launcherSessionOpened(session: LauncherSession) {
        val port = session.store.get(Namespace.create(DockerImageRunner::class.java), StoreKeys.E2e.PORT) as Int
        clients = ManagedClients()
        clients.setup("localhost", port)
        session.store.put(namespace, StoreKeys.E2e.CLIENTS, clients)
    }

    override fun launcherSessionClosed(session: LauncherSession) {
        clients.closeClients()
    }
}
