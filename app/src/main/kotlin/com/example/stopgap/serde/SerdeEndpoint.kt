package com.example.stopgap.serde

import dev.sku20.helidon.endpoint.Endpoint
import dev.sku20.helidon.endpoint.Post
import io.helidon.webserver.http.ServerRequest
import io.helidon.webserver.http.ServerResponse

@Endpoint("/serde")
class SerdeEndpoint {

    @Post("/des")
    fun des(
        req: ServerRequest,
        res: ServerResponse
    ) {
        res.send("des")
    }

}