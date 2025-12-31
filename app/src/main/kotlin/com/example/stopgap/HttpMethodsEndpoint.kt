package com.example.stopgap

import dev.sku20.helidon.endpoint.Endpoint
import dev.sku20.helidon.endpoint.Get
import dev.sku20.helidon.endpoint.Post
import io.helidon.webserver.http.ServerRequest
import io.helidon.webserver.http.ServerResponse

@Endpoint("/methods")
class HttpMethodsEndpoint {

    @Get("/get")
    fun get(req: ServerRequest, res: ServerResponse) {
        res.send("GET request received");
    }

    @Post("/post")
    fun post(req: ServerRequest, res: ServerResponse) {
        res.send("POST request received");
    }

}