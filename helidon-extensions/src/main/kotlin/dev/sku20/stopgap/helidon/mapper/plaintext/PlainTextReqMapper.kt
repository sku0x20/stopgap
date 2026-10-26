package dev.sku20.stopgap.helidon.mapper.plaintext

import dev.sku20.stopgap.helidon.mapper.ReqMapper
import dev.sku20.stopgap.helidon.mapper.exception.EmptyBodyException
import dev.sku20.stopgap.helidon.mapper.exception.UnsupportedMediaTypeException
import io.helidon.webserver.http.ServerRequest
import kotlin.reflect.KType

// Decodes UTF-8 leniently on purpose: malformed bytes (e.g. unlabelled Latin-1) become U+FFFD instead of a 400.
// Always yields a String and ignores the requested type.
class PlainTextReqMapper : ReqMapper {
    @Suppress("UNCHECKED_CAST")
    override fun <T> map(request: ServerRequest, type: KType): T {
        val contentType = request.headers().contentType().orElse(null)
        if (!PlainTextMediaTypeRule.accepts(contentType)) {
            throw UnsupportedMediaTypeException(contentType)
        }
        val bytes = request.content().inputStream().readAllBytes()
        if (bytes.isEmpty()) {
            throw EmptyBodyException()
        }
        return String(bytes, Charsets.UTF_8) as T
    }
}
