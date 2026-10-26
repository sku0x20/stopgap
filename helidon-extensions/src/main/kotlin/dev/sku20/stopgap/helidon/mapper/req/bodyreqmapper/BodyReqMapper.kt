package dev.sku20.stopgap.helidon.mapper.req.bodyreqmapper

import dev.sku20.stopgap.helidon.mapper.req.ReqMapper
import dev.sku20.stopgap.helidon.mapper.req.bodyreqmapper.mediatyperule.MediaTypeRule
import dev.sku20.stopgap.helidon.mapper.req.exception.EmptyBodyException
import dev.sku20.stopgap.helidon.mapper.req.exception.UnsupportedMediaTypeException
import io.helidon.webserver.http.ServerRequest
import kotlin.reflect.KType

class BodyReqMapper(
    private val rule: MediaTypeRule,
    private val decoder: BodyDecoder,
) : ReqMapper {
    override fun <T> map(request: ServerRequest, type: KType): T {
        val contentType = request.headers().contentType().orElse(null)
        if (!rule.accepts(contentType)) {
            throw UnsupportedMediaTypeException(contentType)
        }
        val bytes = request.content().inputStream().readAllBytes()
        if (bytes.isEmpty()) {
            throw EmptyBodyException()
        }
        return decoder.decode(bytes, type)
    }
}
