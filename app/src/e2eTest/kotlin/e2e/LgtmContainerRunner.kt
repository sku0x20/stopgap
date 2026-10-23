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
        // The image runs Pyroscope, but LgtmStackContainer doesn't expose its port.
        container.addExposedPort(PYROSCOPE_PORT)
        container.start()
        System.err.println("Grafana: ${container.grafanaHttpUrl}")
        // A system property, not the session store: test classes can't reach the launcher store without an extension.
        System.setProperty(TEMPO_URL_PROPERTY, container.tempoUrl)
        System.setProperty(PROMETHEUS_URL_PROPERTY, container.prometheusHttpUrl)
        System.setProperty(PYROSCOPE_URL_PROPERTY, "http://${container.host}:${container.getMappedPort(PYROSCOPE_PORT)}")
    }

    override fun launcherSessionClosed(session: LauncherSession) {
        container.stop()
    }

    companion object {
        const val TEMPO_URL_PROPERTY = "e2e.tempo.url"
        const val PROMETHEUS_URL_PROPERTY = "e2e.prometheus.url"
        const val PYROSCOPE_URL_PROPERTY = "e2e.pyroscope.url"
        private const val PYROSCOPE_PORT = 4040
    }
}
