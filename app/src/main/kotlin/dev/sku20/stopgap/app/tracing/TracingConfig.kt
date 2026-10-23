package dev.sku20.stopgap.app.tracing

import dev.sku20.stopgap.ir.annotation.Creates
import io.helidon.tracing.Tracer
import io.helidon.webserver.observe.tracing.TracingObserver

object TracingConfig {

    // No `tracing:` config node, so Helidon's global tracer uses GlobalOpenTelemetry: the agent's SDK (RFC-0002).
    @Creates
    fun tracingObserver(): TracingObserver {
        return TracingObserver.create(Tracer.global())
    }
}
