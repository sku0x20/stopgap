package dev.sku20.stopgap.app.mapper

import dev.sku20.stopgap.app.serde.DataClassReq
import dev.sku20.stopgap.app.serde.ReqDto
import dev.sku20.stopgap.helidon.mapper.req.CustomReqMapper
import dev.sku20.stopgap.helidon.endpoint.Endpoint
import dev.sku20.stopgap.helidon.endpoint.Post
import io.helidon.webserver.http.ServerResponse

// Responses are sent raw so only the request side goes through a mapper.
@Endpoint("/mapper")
class MapperEndpoint {

    @Post("/desObj")
    fun desObj(
        res: ServerResponse,
        body: ReqDto
    ) {
        res.send(body.data)
    }

    @Post("/desDataClass")
    fun desDataClass(
        res: ServerResponse,
        body: DataClassReq
    ) {
        res.send("${body.id}:${body.name}")
    }

    @Post("/desGeneric")
    fun desGeneric(
        res: ServerResponse,
        body: List<ReqDto>
    ) {
        res.send(body.joinToString(" = ") { it.data })
    }

    // TODO: KSP ignores @CustomReqMapper for now, so this still decodes through the default SerdeCatalog.
    @Post("/desPlainText")
    @CustomReqMapper(clazz = PlainTextReqMapper::class)
    fun desPlainText(
        res: ServerResponse,
        body: String
    ) {
        res.send(body)
    }

}
