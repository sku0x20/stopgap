package dev.sku20.stopgap.helidon.mapper.plaintext

import dev.sku20.stopgap.helidon.mapper.ReqMapper
import io.helidon.webserver.http.ServerRequest
import kotlin.reflect.KType

class PlainTextReqMapper : ReqMapper {
    override fun <T> map(request: ServerRequest, type: KType): T = TODO()
}
