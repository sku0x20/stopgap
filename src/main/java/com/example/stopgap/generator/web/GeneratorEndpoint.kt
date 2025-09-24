package com.example.stopgap.generator.web

import com.example.stopgap.Endpoint
import com.example.stopgap.generator.StaticGenerator
import com.example.stopgap.generator.uuid.web.UuidEndpoint
import com.example.stopgap.instanceregistry.InstanceRegistry
import io.helidon.webserver.http.HttpRules
import io.helidon.webserver.http.HttpService
import io.helidon.webserver.http.ServerRequest
import io.helidon.webserver.http.ServerResponse
import java.security.SecureRandom

class GeneratorEndpoint(
    private val staticGenerator: StaticGenerator
) : Endpoint {

    private val random = SecureRandom()

    override fun routes(registry: InstanceRegistry): HttpService {
        val uuidEndpoint = registry.getInstanceForType(UuidEndpoint::class.java)

        return HttpService { rules: HttpRules ->
            rules
                .get("/number", ::randomNumber)
                .get("/static", ::staticGen)
                .register("/uuid", uuidEndpoint.routes(registry))
        }
    }

    private fun randomNumber(
        req: ServerRequest,
        res: ServerResponse
    ) {
        res.send(random.nextLong().toString())
    }

    private fun staticGen(
        req: ServerRequest,
        res: ServerResponse
    ) {
        res.send(staticGenerator.value)
    }
}
