package com.example.stopgap.generator.web

import com.example.stopgap.Endpoint
import com.example.stopgap.generator.StaticGenerator
import dev.sku20.ir.InstanceRegistry
import io.helidon.webserver.http.HttpRules
import io.helidon.webserver.http.HttpService
import io.helidon.webserver.http.ServerRequest
import io.helidon.webserver.http.ServerResponse
import java.security.SecureRandom

class GeneratorEndpoint(
    private val staticGenerator: StaticGenerator
) : Endpoint {

    private val random = SecureRandom()

    fun randomNumber(
        req: ServerRequest,
        res: ServerResponse
    ) {
        res.send(random.nextLong().toString())
    }

    fun staticGen(
        req: ServerRequest,
        res: ServerResponse
    ) {
        res.send(staticGenerator.value)
    }

    override fun routes(registry: InstanceRegistry): HttpService {
        return HttpService { rules: HttpRules ->
            rules
                .get("/number", ::randomNumber)
                .get("/static", ::staticGen)

        }
    }
}
