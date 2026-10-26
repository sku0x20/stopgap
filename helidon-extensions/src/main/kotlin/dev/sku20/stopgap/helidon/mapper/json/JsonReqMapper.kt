package dev.sku20.stopgap.helidon.mapper.json

import dev.sku20.stopgap.helidon.mapper.ReqMapper
import io.helidon.webserver.http.ServerRequest
import kotlin.reflect.KType

class JsonReqMapper(private val decoder: JsonDecoder) : ReqMapper {
    override fun <T> map(request: ServerRequest, type: KType): T =
        decoder.decode(request.content().inputStream().readAllBytes(), type)
}
