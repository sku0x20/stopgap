package com.example.stopgap.serde

import dev.sku20.helidon.endpoint.Endpoint
import dev.sku20.helidon.endpoint.Get
import dev.sku20.helidon.endpoint.Post
import io.helidon.webserver.http.ServerRequest
import io.helidon.webserver.http.ServerResponse

@Endpoint("/serde")
class SerdeEndpoint {

    @Post("/desObj")
    fun desObj(
        req: ServerRequest,
        res: ServerResponse,
        body: ReqDto
    ) {
        res.send(body.data)
    }

    @Get("/serObj")
    fun serObj(
        req: ServerRequest,
        res: ServerResponse
    ): ResDto {
        val query = req.query()
        val data = query.get("data")

        return ResDto(data)
    }

}