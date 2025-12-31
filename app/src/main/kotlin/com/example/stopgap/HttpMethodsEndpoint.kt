package com.example.stopgap

import dev.sku20.helidon.endpoint.*
import io.helidon.webserver.http.ServerRequest
import io.helidon.webserver.http.ServerResponse

@Endpoint("/methods")
class HttpMethodsEndpoint {

    @Get("/get")
    fun get(req: ServerRequest, res: ServerResponse) {
        res.send("GET request received")
    }

    @Post("/post")
    fun post(req: ServerRequest, res: ServerResponse) {
        res.send("POST request received")
    }

    @Delete("/delete")
    fun delete(req: ServerRequest, res: ServerResponse) {
        res.send("DELETE request received")
    }

    @Put("/put")
    fun put(req: ServerRequest, res: ServerResponse) {
        res.send("PUT request received")
    }

    @Patch("/patch")
    fun patch(req: ServerRequest, res: ServerResponse) {
        res.send("PATCH request received")
    }

    @Head("/head")
    fun head(req: ServerRequest, res: ServerResponse) {
        res.send("HEAD request received")
    }

    @Options("/options")
    fun options(req: ServerRequest, res: ServerResponse) {
        res.send("OPTIONS request received")
    }

    @Trace("/trace")
    fun trace(req: ServerRequest, res: ServerResponse) {
        res.send("TRACE request received")
    }

}