package dev.sku20.stopgap.helidon.mapper.req.bodyreqmapper.plaintext

import dev.sku20.stopgap.helidon.mapper.req.bodyreqmapper.mediatyperule.MediaTypeRule
import io.helidon.http.HttpMediaType

// A missing charset counts as UTF-8; any other charset is rejected so the mapper can always decode UTF-8.
object PlainTextMediaTypeRule : MediaTypeRule {
    override fun accepts(mediaType: HttpMediaType?): Boolean {
        val type = mediaType?.mediaType() ?: return false
        val charset = mediaType.charset().orElse("utf-8")
        return type.type().equals("text", ignoreCase = true) &&
            type.subtype().equals("plain", ignoreCase = true) &&
            charset.equals("utf-8", ignoreCase = true)
    }
}
