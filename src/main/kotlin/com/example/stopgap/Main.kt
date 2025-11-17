package com.example.stopgap

import io.helidon.webserver.WebServer

fun main(args: Array<String>) {
    InstanceRegistryInit.init()
    val registry = InstanceRegistryInit.registry
    val server = registry.getInstanceForType<WebServer>()
    server.start()
}
