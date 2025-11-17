package com.example.stopgap

import com.example.stopgap.instanceregistry.InstanceRegistry
import io.helidon.webserver.WebServer

object MainConfig {

    fun server(registry: InstanceRegistry): WebServer {
        val mainEndpoint = registry.getInstanceForType<MainEndpoint>()
        val config = registry.getInstanceForType<Config>() as HelidonConfig

        val server = WebServer.builder()
            .config(config.getConfig("server"))
            .protocolsDiscoverServices(false)
            .routing(mainEndpoint.routing(registry))
            .build()
        return server
    }

    fun config(registry: InstanceRegistry): Config {
        val config = HelidonConfig.loadDefault()
        return config
    }

    fun mainEndpoint(registry: InstanceRegistry): MainEndpoint {
        return MainEndpoint()
    }

}
