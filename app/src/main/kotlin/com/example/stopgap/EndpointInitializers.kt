package com.example.stopgap

import com.example.stopgap.exception.ExceptionEndpoint
import com.example.stopgap.generator.web.GeneratorEndpoint
import dev.sku20.ir.InstanceRegistry
import io.helidon.webserver.http.HttpRouting

fun initEndpointsRoutes(
    routes: HttpRouting.Builder,
    registry: InstanceRegistry
) {

    val rootEndpoint = registry.getInstanceForType<RootEndpoint>()
    routes.register("/", { rules ->
        rules.get("/ping", rootEndpoint::ping)
    })

    val exceptionEndpoint = registry.getInstanceForType<ExceptionEndpoint>()
    routes.register("/exception", exceptionEndpoint.routes(registry))

    val generatorEndpoint = registry.getInstanceForType<GeneratorEndpoint>()
    routes.register("/generate", generatorEndpoint.routes(registry))
}