package com.example.stopgap

import com.example.stopgap.exception.ExceptionEndpoint
import com.example.stopgap.generator.web.GeneratorEndpoint
import com.example.stopgap.instanceregistry.InstanceRegistry
import io.helidon.webserver.http.*

class MainEndpoint : Endpoint {

    fun routing(registry: InstanceRegistry): HttpRouting.Builder? {
        return HttpRouting.builder()
            .register("/", routes(registry))
    }

    override fun routes(registry: InstanceRegistry): HttpService {
        val generator = registry.getInstanceForType<GeneratorEndpoint>()
        val exception = registry.getInstanceForType<ExceptionEndpoint>()

        return HttpService { rules: HttpRules ->
            rules
                .register("/generate", generator.routes(registry))
                .register("/exception", exception.routes(registry))
                .get("/ping", { req: ServerRequest, res: ServerResponse -> res.send("pong") })
        }
    }

}
