package com.example.stopgap

import io.helidon.webserver.WebServer

fun main(args: Array<String>) {
    val config = HelidonConfig.loadDefault()

    InstanceRegistryInit.init(config)

    val registry = InstanceRegistryInit.registry
    MainConfig.setup(registry)

    val mainEndpoint = registry.getInstanceForType<MainEndpoint>()

    val server = WebServer.builder()
        .config(config.getConfig("server"))
        .protocolsDiscoverServices(false)
        .routing(mainEndpoint.routing(registry))
        .build()
    server.start()
}
