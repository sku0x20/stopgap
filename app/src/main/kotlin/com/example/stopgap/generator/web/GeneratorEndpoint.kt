package com.example.stopgap.generator.web

import com.example.stopgap.generator.StaticGenerator
import dev.sku20.helidon.endpoint.Endpoint
import dev.sku20.helidon.endpoint.Get
import io.helidon.webserver.http.ServerRequest
import io.helidon.webserver.http.ServerResponse
import java.security.SecureRandom

@Endpoint("/generate")
class GeneratorEndpoint(
    private val staticGenerator: StaticGenerator
) {

    private val random = SecureRandom()

    @Get("/number")
    fun randomNumber(
        req: ServerRequest,
        res: ServerResponse
    ) {
        res.send(random.nextLong().toString())
    }

    @Get("/static")
    fun staticGen(
        req: ServerRequest,
        res: ServerResponse
    ) {
        res.send(staticGenerator.value)
    }
}
