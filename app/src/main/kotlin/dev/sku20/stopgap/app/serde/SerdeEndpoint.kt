package dev.sku20.stopgap.app.serde

import dev.sku20.stopgap.helidon.endpoint.Endpoint
import dev.sku20.stopgap.helidon.endpoint.Get
import dev.sku20.stopgap.helidon.endpoint.Post
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

    @Get("/unitGet")
    fun unitGet() {
    }

    @Post("/genericPost")
    fun genericPost(
        body: List<dev.sku20.stopgap.app.serde.ResDto>
    ): ResDto {
        return ResDto(body.joinToString(" = ") { it.data })
    }

    @Post("/negotiate")
    fun negotiate(body: ReqDto): ResDto {
        return ResDto(body.data)
    }

}