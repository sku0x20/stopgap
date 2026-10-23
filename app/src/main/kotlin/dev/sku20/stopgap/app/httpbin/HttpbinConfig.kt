package dev.sku20.stopgap.app.httpbin

import dev.sku20.stopgap.app.httpbin.web.HttpbinEndpoint
import dev.sku20.stopgap.app.webclient.LoggingClientService
import dev.sku20.stopgap.ir.annotation.Creates
import io.helidon.config.Config
import io.helidon.tracing.Tracer
import io.helidon.webclient.api.WebClient
import io.helidon.webclient.telemetry.metrics.WebClientTelemetryMetrics
import io.helidon.webclient.tracing.WebClientTracing

object HttpbinConfig {

    // Services wired explicitly: discovery would add a second tracing service on Tracer.global() (RFC-0002).
    // Tracing first, so it wraps logging and the span covers the logged duration. Metrics take their OTel instance
    // from Helidon's Services, set in main.
    @Creates
    fun httpbinClient(
        config: Config,
        tracer: Tracer
    ): WebClient {
        val httpbin = config.get("httpbin")
        return WebClient.builder()
            .config(httpbin.get("client"))
            .baseUri(httpbin.get("url").asString().get())
            .servicesDiscoverServices(false)
            .addService(WebClientTracing.create(tracer))
            .addService(WebClientTelemetryMetrics.create())
            .addService(LoggingClientService())
            .build()
    }

    @Creates
    fun httpbinEndpoint(httpbinClient: WebClient): HttpbinEndpoint {
        return HttpbinEndpoint(httpbinClient)
    }
}
