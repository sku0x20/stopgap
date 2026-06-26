package com.example.stopgap.param

import dev.sku20.helidon.endpoint.Endpoint
import dev.sku20.helidon.endpoint.Get
import dev.sku20.helidon.param.PathParam
import io.helidon.webserver.http.ServerResponse

@Endpoint("/param")
class ParamEndpoint {

    @Get("/{id}")
    fun getById(@PathParam("id") id: String, res: ServerResponse) {
        res.send(id)
    }

}
