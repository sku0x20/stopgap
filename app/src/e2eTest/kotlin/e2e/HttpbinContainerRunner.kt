package e2e

import dev.sku20.stopgap.helidon.test.e2e.E2eStore
import dev.sku20.stopgap.helidon.test.e2e.E2eStoreKeys
import org.junit.platform.launcher.LauncherSession
import org.junit.platform.launcher.LauncherSessionListener
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.wait.strategy.Wait

// Alias and port must match HTTPBIN_URL in application.env.
class HttpbinContainerRunner : LauncherSessionListener {

    private lateinit var container: GenericContainer<*>

    override fun launcherSessionOpened(session: LauncherSession) {
        val network = session.store.get(E2eStore.NAMESPACE, E2eStoreKeys.NETWORK) as Network
        container = GenericContainer("mccutchen/go-httpbin:2.25.0")
            .withExposedPorts(8080)
            .withNetwork(network)
            .withNetworkAliases("httpbin")
            .waitingFor(Wait.forHttp("/get"))
        container.start()
    }

    override fun launcherSessionClosed(session: LauncherSession) {
        container.stop()
    }
}
