package dev.sku20.stopgap.helidon.mapper.bodyreqmapper

import dev.sku20.stopgap.helidon.mapper.ReqMapper
import dev.sku20.stopgap.helidon.mapper.mediatyperule.MediaTypeRule
import io.helidon.webserver.http.ServerRequest
import kotlin.reflect.KType

class BodyReqMapper(
    private val rule: MediaTypeRule,
    private val decoder: BodyDecoder,
) : ReqMapper {
    override fun <T> map(request: ServerRequest, type: KType): T = TODO()
}
