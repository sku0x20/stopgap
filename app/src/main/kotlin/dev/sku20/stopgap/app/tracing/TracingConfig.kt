package dev.sku20.stopgap.app.tracing

import dev.sku20.stopgap.ir.annotation.Creates
import io.helidon.config.Config
import io.helidon.tracing.Tracer
import io.helidon.tracing.providers.opentelemetry.HelidonOpenTelemetry
import io.helidon.webserver.observe.tracing.TracingObserver
import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.OpenTelemetry

object TracingConfig {

    // The one SDK everything traces through: the agent's when attached, otherwise no-op. Helidon never builds its
    // own (RFC-0002).
    @Creates
    fun openTelemetry(config: Config): OpenTelemetry {
        if (HelidonOpenTelemetry.AgentDetector.isAgentPresent(config)) {
            return GlobalOpenTelemetry.get()
        }
        return OpenTelemetry.noop()
    }

    @Creates
    fun tracer(openTelemetry: OpenTelemetry): Tracer {
        return HelidonOpenTelemetry.create(
            openTelemetry,
            openTelemetry.getTracer("io.helidon.tracing"),
            mapOf(),
        )
    }

    @Creates
    fun tracingObserver(tracer: Tracer): TracingObserver {
        return TracingObserver.create(tracer)
    }
}
