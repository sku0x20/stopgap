package com.example.stopgap

import dev.sku20.ir.InstanceRegistry
import dev.sku20.ir.generated.IrInitCreators
import io.helidon.config.Config
import io.helidon.webserver.WebServer

fun main(args: Array<String>) {
    val registry = InstanceRegistry()

    IrInitCreators.init(registry)

    val mainEndpoint = registry.getInstanceForType<MainEndpoint>()

    val config = registry.getInstanceForType<Config>()

    val server = WebServer.builder()
        .config(config.get("server"))
        .protocolsDiscoverServices(false)
        .routing(mainEndpoint.routing(registry))
        .build()
    server.start()
}
