package com.example.stopgap

import com.example.stopgap.instanceregistry.InstanceRegistry
import io.helidon.webserver.WebServer

fun main(args: Array<String>) {
    val config = HelidonConfig.loadDefault()
    val instanceRegistry = InstanceRegistry(config)
    MainConfig.setup(instanceRegistry)

    val mainEndpoint = instanceRegistry.getInstanceForType(MainEndpoint::class.java)

    val server = WebServer.builder()
        .config(config.getConfig("server"))
        .protocolsDiscoverServices(false)
        .routing(mainEndpoint.routing(instanceRegistry))
        .build()
    server.start()
}
