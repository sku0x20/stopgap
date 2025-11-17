package com.example.stopgap

import io.helidon.webserver.WebServer

fun main(args: Array<String>) {
    InstanceRegistryInit.init()

    val registry = InstanceRegistryInit.registry
    val mainEndpoint = registry.getInstanceForType<MainEndpoint>()

    val config = registry.getInstanceForType<Config>() as HelidonConfig

    val server = WebServer.builder()
        .config(config.getConfig("server"))
        .protocolsDiscoverServices(false)
        .routing(mainEndpoint.routing(registry))
        .build()
    server.start()
}
