package dev.sku20.stopgap.helidon.mapper.json

import dev.sku20.stopgap.helidon.mapper.bodyreqmapper.mediatyperule.MediaTypeRule
import io.helidon.http.HttpMediaType

// Compares type and subtype directly: HttpMediaType.test() treats wildcards as a match, so */* would pass.
object JsonMediaTypeRule : MediaTypeRule {
    override fun accepts(mediaType: HttpMediaType?): Boolean {
        val type = mediaType?.mediaType() ?: return false
        val subtype = type.subtype()
        return type.type().equals("application", ignoreCase = true) &&
            (subtype.equals("json", ignoreCase = true) || subtype.endsWith("+json", ignoreCase = true))
    }
}
