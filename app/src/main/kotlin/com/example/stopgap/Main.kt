package com.example.stopgap

import io.helidon.config.Config
import io.helidon.webserver.WebServer

fun main(args: Array<String>) {
    InstanceRegistryInit.init()

    val registry = InstanceRegistryInit.registry
    val mainEndpoint = registry.getInstanceForType<MainEndpoint>()

    val config = registry.getInstanceForType<Config>()

    val server = WebServer.builder()
        .config(config.get("server"))
        .protocolsDiscoverServices(false)
        .routing(mainEndpoint.routing(registry))
        .build()
    server.start()
}
