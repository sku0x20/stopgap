package dev.sku20.stopgap.app

import dev.sku20.stopgap.helidon.endpoint.Endpoint
import dev.sku20.stopgap.helidon.endpoint.Get
import io.helidon.webserver.http.ServerRequest
import io.helidon.webserver.http.ServerResponse

@Endpoint("/")
class RootEndpoint {

    @Get("/ping")
    fun ping(req: ServerRequest, res: ServerResponse) {
        res.send("pong")
    }

}
