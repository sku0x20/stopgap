package dev.sku20.stopgap.app.metrics

import dev.sku20.stopgap.ir.annotation.Creates
import io.helidon.config.Config
import io.helidon.webserver.observe.metrics.MetricsObserver

object MetricsConfig {

    // Its HTTP server filter records http.server.request.duration with the OTel instance handed to Services in main.
    @Creates
    fun metricsObserver(config: Config): MetricsObserver {
        return MetricsObserver.create(config.get("metrics"))
    }
}
