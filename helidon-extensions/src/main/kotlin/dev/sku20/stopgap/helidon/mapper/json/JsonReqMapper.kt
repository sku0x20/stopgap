package dev.sku20.stopgap.helidon.mapper.json

import dev.sku20.stopgap.helidon.mapper.ReqMapper
import io.helidon.webserver.http.ServerRequest
import kotlin.reflect.KType

class JsonReqMapper(private val codec: JsonCodec) : ReqMapper {
    override fun <T> map(request: ServerRequest, type: KType): T = TODO()
}
