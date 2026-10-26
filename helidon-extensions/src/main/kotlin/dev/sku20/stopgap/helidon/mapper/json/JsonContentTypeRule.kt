package dev.sku20.stopgap.helidon.mapper.json

import dev.sku20.stopgap.helidon.mapper.ContentTypeRule
import io.helidon.http.HttpMediaType

// Compares type and subtype directly: HttpMediaType.test() treats wildcards as a match, so */* would pass.
object JsonContentTypeRule : ContentTypeRule {
    override fun accepts(contentType: HttpMediaType?): Boolean {
        val mediaType = contentType?.mediaType() ?: return false
        val subtype = mediaType.subtype()
        return mediaType.type().equals("application", ignoreCase = true) &&
            (subtype.equals("json", ignoreCase = true) || subtype.endsWith("+json", ignoreCase = true))
    }
}
