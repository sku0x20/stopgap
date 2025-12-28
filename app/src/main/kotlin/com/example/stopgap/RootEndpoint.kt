package com.example.stopgap

import dev.sku20.helidon.endpoint.Endpoint
import io.helidon.webserver.http.ServerRequest
import io.helidon.webserver.http.ServerResponse

@Endpoint("/")
class RootEndpoint {

    fun ping(req: ServerRequest, res: ServerResponse) {
        res.send("pong")
    }

}
