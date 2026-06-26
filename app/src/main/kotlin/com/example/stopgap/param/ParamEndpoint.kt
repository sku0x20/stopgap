package com.example.stopgap.param

import dev.sku20.helidon.endpoint.Endpoint
import dev.sku20.helidon.endpoint.Get
import dev.sku20.helidon.param.HeaderParam
import dev.sku20.helidon.param.PathParam
import dev.sku20.helidon.param.QueryParam
import io.helidon.http.Header
import io.helidon.webserver.http.ServerResponse

@Endpoint("/param")
class ParamEndpoint {

    // literal routes must be declared before path param routes; Helidon matches first-registered wins
    @Get("/query")
    fun getByQuery(@QueryParam("name") name: String, res: ServerResponse) {
        res.send(name)
    }

    @Get("/header")
    fun getByHeader(@HeaderParam("Accept-Language") name: Header, res: ServerResponse) {
        res.send(name.get())
    }

    @Get("/{id}")
    fun getById(@PathParam("id") id: String, res: ServerResponse) {
        res.send(id)
    }

}
