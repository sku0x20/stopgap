package com.example.stopgap

import com.example.stopgap.exception.ExceptionEndpoint
import com.example.stopgap.generator.uuid.web.UuidEndpoint
import com.example.stopgap.generator.web.GeneratorEndpoint
import dev.sku20.ir.InstanceRegistry
import io.helidon.webserver.http.HttpRouting
import io.helidon.webserver.http.HttpRules

private fun initEndpointsRoutes(
    routes: HttpRouting.Builder,
    registry: InstanceRegistry
) {

    val rootEndpoint = registry.getInstanceForType<RootEndpoint>()
    routes.register("/", { rules ->
        rules.get("/ping", rootEndpoint::ping)
    })

    val exceptionEndpoint = registry.getInstanceForType<ExceptionEndpoint>()
    routes.register("/exception", { rules: HttpRules ->
        rules
            .get("/bad-client-1", exceptionEndpoint::badClientException)
            .get("/bad-client-2", exceptionEndpoint::viaHttpException)
    })

    val generatorEndpoint = registry.getInstanceForType<GeneratorEndpoint>()
    routes.register("/generate", { rules: HttpRules ->
        rules
            .get("/number", generatorEndpoint::randomNumber)
            .get("/static", generatorEndpoint::staticGen)
    })

    val uuidEndpoint = registry.getInstanceForType<UuidEndpoint>()
    routes.register("/generate/uuid", { rules: HttpRules ->
        rules
            .get("/", uuidEndpoint::generateUuid)
    })
}