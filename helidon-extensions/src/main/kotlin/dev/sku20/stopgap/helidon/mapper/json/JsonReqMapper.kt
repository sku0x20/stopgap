package dev.sku20.stopgap.helidon.mapper.json

import dev.sku20.stopgap.helidon.mapper.ReqMapper
import dev.sku20.stopgap.helidon.mapper.bodyreqmapper.mediatyperule.MediaTypeRule
import dev.sku20.stopgap.helidon.mapper.exception.EmptyBodyException
import dev.sku20.stopgap.helidon.mapper.exception.UnsupportedMediaTypeException
import io.helidon.webserver.http.ServerRequest
import kotlin.reflect.KType

class JsonReqMapper(
    private val decoder: JsonDecoder,
    private val mediaTypeRule: MediaTypeRule
) : ReqMapper {
    override fun <T> map(request: ServerRequest, type: KType): T {
        val contentType = request.headers().contentType().orElse(null)
        if (!mediaTypeRule.accepts(contentType)) {
            throw UnsupportedMediaTypeException(contentType)
        }
        val bytes = request.content().inputStream().readAllBytes()
        if (bytes.isEmpty()) {
            throw EmptyBodyException()
        }
        return decoder.decode(bytes, type)
    }
}
