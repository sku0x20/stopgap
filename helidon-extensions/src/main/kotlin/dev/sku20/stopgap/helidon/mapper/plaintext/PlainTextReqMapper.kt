package dev.sku20.stopgap.helidon.mapper.plaintext

import dev.sku20.stopgap.helidon.mapper.ReqMapper
import io.helidon.webserver.http.ServerRequest
import kotlin.reflect.KType

// Decodes UTF-8 leniently on purpose: malformed bytes (e.g. unlabelled Latin-1) become U+FFFD instead of a 400.
class PlainTextReqMapper : ReqMapper {
    override fun <T> map(request: ServerRequest, type: KType): T = TODO()
}
