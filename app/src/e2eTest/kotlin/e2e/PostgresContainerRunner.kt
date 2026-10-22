package e2e

import dev.sku20.stopgap.helidon.test.e2e.E2eStore
import dev.sku20.stopgap.helidon.test.e2e.E2eStoreKeys
import org.junit.platform.launcher.LauncherSession
import org.junit.platform.launcher.LauncherSessionListener
import org.testcontainers.containers.Network
import org.testcontainers.postgresql.PostgreSQLContainer

// Alias and credentials must match the DB_POSTGRES_* entries in application.env.
class PostgresContainerRunner : LauncherSessionListener {

    private lateinit var container: PostgreSQLContainer

    override fun launcherSessionOpened(session: LauncherSession) {
        val network = session.store.get(E2eStore.NAMESPACE, E2eStoreKeys.NETWORK) as Network
        container = PostgreSQLContainer("postgres:18-alpine")
            .withDatabaseName("stopgap")
            .withUsername("postgres")
            .withPassword("postgres")
            .withNetwork(network)
            .withNetworkAliases("postgres")
        container.start()
    }

    override fun launcherSessionClosed(session: LauncherSession) {
        container.stop()
    }
}
