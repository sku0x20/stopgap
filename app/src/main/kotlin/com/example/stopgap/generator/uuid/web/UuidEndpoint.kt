package com.example.stopgap.generator.uuid.web

import com.example.stopgap.generator.uuid.UuidGen
import dev.sku20.helidon.endpoint.Endpoint
import dev.sku20.helidon.endpoint.Get
import io.helidon.webserver.http.ServerRequest
import io.helidon.webserver.http.ServerResponse

@Endpoint("/generate/uuid")
class UuidEndpoint(
    private val uuidGen: UuidGen
) {

    @Get("/")
    fun generateUuid(
        req: ServerRequest,
        res: ServerResponse
    ) {
        res.send(uuidGen.generate())
    }

}
