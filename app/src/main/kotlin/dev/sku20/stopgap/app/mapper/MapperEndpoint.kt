package dev.sku20.stopgap.app.mapper

import dev.sku20.stopgap.helidon.mapper.req.CustomReqMapper
import dev.sku20.stopgap.helidon.mapper.req.bodyreqmapper.plaintext.PlainTextReqMapper
import dev.sku20.stopgap.helidon.mapper.req.multipart.MultiPart
import dev.sku20.stopgap.helidon.mapper.req.multipart.MultipartReqMapper
import dev.sku20.stopgap.helidon.mapper.res.CustomResMapper
import dev.sku20.stopgap.helidon.mapper.res.bodyresmapper.plaintext.PlainTextResMapper
import dev.sku20.stopgap.helidon.endpoint.Endpoint
import dev.sku20.stopgap.helidon.endpoint.Get
import dev.sku20.stopgap.helidon.endpoint.Post
import io.helidon.webserver.http.ServerResponse

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

    @Post("/desPlainText")
    @CustomReqMapper(clazz = PlainTextReqMapper::class)
    fun desPlainText(
        res: ServerResponse,
        body: String
    ) {
        res.send(body)
    }

    @Post("/desMultipart")
    @CustomReqMapper(clazz = MultipartReqMapper::class)
    fun desMultipart(
        res: ServerResponse,
        body: MultiPart
    ) {
        res.send(body.parts.joinToString(" ") { "${it.name}:${it.fileName}:${it.body.size}" })
    }

    @Get("/serObj")
    fun serObj(): ResDto {
        return ResDto("foo")
    }

    @Get("/serGeneric")
    fun serGeneric(): List<ResDto> {
        return listOf(ResDto("foo"), ResDto("bar"))
    }

    @Get("/serPlainText")
    @CustomResMapper(clazz = PlainTextResMapper::class)
    fun serPlainText(): String {
        return "foo"
    }

}
