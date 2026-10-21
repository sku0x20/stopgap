package dev.sku20.stopgap.app

import dev.sku20.stopgap.helidon.registry.generated.initHttpRouting
import dev.sku20.stopgap.ir.InstanceRegistry
import dev.sku20.stopgap.ir.generated.initRegistry
import io.helidon.config.Config
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
        .protocolsDiscoverServices(false)
        .routing(routing)
        .build()
    server.start()
}
