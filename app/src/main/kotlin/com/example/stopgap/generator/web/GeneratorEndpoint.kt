package com.example.stopgap.generator.web

import com.example.stopgap.generator.StaticGenerator
import io.helidon.webserver.http.ServerRequest
import io.helidon.webserver.http.ServerResponse
import java.security.SecureRandom

class GeneratorEndpoint(
    private val staticGenerator: StaticGenerator
) {

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
}
