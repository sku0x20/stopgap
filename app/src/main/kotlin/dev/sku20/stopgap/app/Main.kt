package dev.sku20.stopgap.app

import com.zaxxer.hikari.HikariDataSource
import dev.sku20.stopgap.helidon.registry.generated.initHttpRouting
import dev.sku20.stopgap.ir.InstanceRegistry
import dev.sku20.stopgap.ir.generated.initRegistry
import io.helidon.config.Config
import io.helidon.http.encoding.ContentEncodingContext
import io.helidon.http.encoding.gzip.GzipEncoding
import io.helidon.webserver.WebServer
import io.helidon.webserver.http.HttpRouting
import org.slf4j.bridge.SLF4JBridgeHandler

fun main(args: Array<String>) {
    // pgjdbc logs via JUL; route it to slf4j
    SLF4JBridgeHandler.removeHandlersForRootLogger()
    SLF4JBridgeHandler.install()

    val registry = InstanceRegistry()
    initRegistry(registry)

    val routing = HttpRouting.builder()
    initHttpRouting(registry, routing)

    val config = registry.getInstanceForType<Config>()
    val server = WebServer.builder()
        .config(config.get("server"))
        .shutdownHook(false)
        .protocolsDiscoverServices(false)
        .contentEncoding(
            ContentEncodingContext.builder()
                .contentEncodingsDiscoverServices(false)
                .addContentEncoding(GzipEncoding.create())
                .build()
        )
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
