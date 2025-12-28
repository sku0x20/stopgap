package com.example.stopgap

import dev.sku20.helidon.endpoint.generated.initEndpointRoutes
import dev.sku20.ir.InstanceRegistry
import dev.sku20.ir.generated.initRegistry
import io.helidon.config.Config
import io.helidon.webserver.WebServer
import io.helidon.webserver.http.HttpRouting

fun main(args: Array<String>) {
    val registry = InstanceRegistry()
    initRegistry(registry)

    val routing = HttpRouting.builder()
    initEndpointRoutes(routing, registry)

    val config = registry.getInstanceForType<Config>()
    val server = WebServer.builder()
        .config(config.get("server"))
        .protocolsDiscoverServices(false)
        .routing(routing)
        .build()
    server.start()
}
