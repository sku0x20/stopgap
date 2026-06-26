package com.example.stopgap.param

import dev.sku20.helidon.endpoint.Endpoint
import dev.sku20.helidon.endpoint.Get
import io.helidon.webserver.http.ServerRequest
import io.helidon.webserver.http.ServerResponse

@Endpoint("/param")
class ParamEndpoint {

    @Get("/{id}")
    fun getById(req: ServerRequest, res: ServerResponse) {
        val id = req.path().pathParam("id")
        res.send(id)
    }

}
