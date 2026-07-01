package dev.sku20.stopgap.app.generator.uuid.web

import dev.sku20.stopgap.app.generator.uuid.UuidGen
import dev.sku20.stopgap.helidon.endpoint.Endpoint
import dev.sku20.stopgap.helidon.endpoint.Get
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
