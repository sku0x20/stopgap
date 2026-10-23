package dev.sku20.stopgap.app

import dev.sku20.stopgap.ir.annotation.Creates
import io.helidon.webserver.observe.ObserveFeature
import io.helidon.webserver.observe.health.HealthObserver
import io.helidon.webserver.observe.tracing.TracingObserver

object ObserveConfig {

    @Creates
    fun observeFeature(
        healthObserver: HealthObserver,
        tracingObserver: TracingObserver
    ): ObserveFeature {
        return ObserveFeature.builder()
            .observersDiscoverServices(false)
            .addObserver(healthObserver)
            .addObserver(tracingObserver)
            .build()
    }
}
