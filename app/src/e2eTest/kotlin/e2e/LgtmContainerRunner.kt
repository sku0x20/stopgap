package e2e

import dev.sku20.stopgap.helidon.test.e2e.E2eStore
import dev.sku20.stopgap.helidon.test.e2e.E2eStoreKeys
import org.junit.platform.launcher.LauncherSession
import org.junit.platform.launcher.LauncherSessionListener
import org.testcontainers.containers.Network
import org.testcontainers.grafana.LgtmStackContainer

// One container for the collector, Tempo, Loki, Prometheus and Grafana. Alias must match the OTEL_EXPORTER_OTLP_*
// endpoints in application.env.
class LgtmContainerRunner : LauncherSessionListener {

    private lateinit var container: LgtmStackContainer

    override fun launcherSessionOpened(session: LauncherSession) {
        val network = session.store.get(E2eStore.NAMESPACE, E2eStoreKeys.NETWORK) as Network
        container = LgtmStackContainer("grafana/otel-lgtm:0.38.0")
            .withNetwork(network)
            .withNetworkAliases("lgtm")
        container.start()
        System.err.println("Grafana: ${container.grafanaHttpUrl}")
    }

    override fun launcherSessionClosed(session: LauncherSession) {
        container.stop()
    }
}
