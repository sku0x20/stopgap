package dev.sku20.stopgap.app.tracing

import dev.sku20.stopgap.ir.annotation.Creates
import io.helidon.config.Config
import io.helidon.tracing.Tracer
import io.helidon.tracing.TracerBuilder
import io.helidon.tracing.providers.opentelemetry.HelidonOpenTelemetry
import io.helidon.webserver.observe.tracing.TracingObserver
import io.opentelemetry.api.GlobalOpenTelemetry

object TracingConfig {

    // One SDK either way: the agent's when attached (RFC-0002), otherwise Helidon builds its own from `tracing:`.
    @Creates
    fun tracer(config: Config): Tracer {
        if (HelidonOpenTelemetry.AgentDetector.isAgentPresent(config)) {
            val openTelemetry = GlobalOpenTelemetry.get()
            return HelidonOpenTelemetry.create(openTelemetry, openTelemetry.getTracer("io.helidon.tracing"), mapOf())
        }
        return TracerBuilder.create(config.get("tracing")).build()
    }

    @Creates
    fun tracingObserver(tracer: Tracer): TracingObserver {
        return TracingObserver.create(tracer)
    }
}
