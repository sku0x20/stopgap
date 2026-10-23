package dev.sku20.stopgap.app

import com.zaxxer.hikari.HikariDataSource
import dev.sku20.stopgap.helidon.registry.generated.initHttpRouting
import dev.sku20.stopgap.ir.InstanceRegistry
import dev.sku20.stopgap.ir.generated.initRegistry
import io.helidon.config.Config
import io.helidon.service.registry.Services
import io.helidon.webserver.WebServer
import io.helidon.webserver.http.HttpRouting
import io.helidon.webserver.observe.ObserveFeature
import io.micrometer.core.instrument.Metrics
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.instrumentation.micrometer.v1_5.OpenTelemetryMeterRegistry
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender
import org.slf4j.bridge.SLF4JBridgeHandler

fun main(args: Array<String>) {
    // pgjdbc logs via JUL; route it to slf4j
    SLF4JBridgeHandler.removeHandlersForRootLogger()
    SLF4JBridgeHandler.install()

    val registry = InstanceRegistry()
    initRegistry(registry)

    // Logs export through the same SDK as traces (RFC-0002).
    val openTelemetry = registry.getInstanceForType<OpenTelemetry>()
    OpenTelemetryAppender.install(openTelemetry)
    // Helidon's metrics modules look it up in Helidon's own registry, which would otherwise pick its own instance.
    // Must run before anything Helidon is built: the first lookup fixes the instance and a later set throws.
    Services.set(OpenTelemetry::class.java, openTelemetry)
    // Helidon's Micrometer meters (request KPIs, system meters) live on Micrometer's global registry; this child sends
    // them to the same SDK. Added before any meter exists: a registry added later misses earlier recordings.
    Metrics.globalRegistry.add(OpenTelemetryMeterRegistry.builder(openTelemetry).build())

    val routing = HttpRouting.builder()
    initHttpRouting(registry, routing)

    val config = registry.getInstanceForType<Config>()
    val server = WebServer.builder()
        .config(config.get("server"))
        .shutdownHook(false)
        .addFeature(registry.getInstanceForType<ObserveFeature>())
        .routing(routing)
        .build()
    server.start()

    // One hook instead of Helidon's own: JVM shutdown hooks run concurrently, so the pool could close while the
    // server still serves requests.
    val dataSource = registry.getInstanceForType<HikariDataSource>()
    Runtime.getRuntime().addShutdownHook(Thread {
        server.stop()
        dataSource.close()
    })
}
