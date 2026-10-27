package dev.sku20.stopgap.helidon.mapper.req.bodyreqmapper.plaintext

import dev.sku20.stopgap.helidon.mapper.req.bodyreqmapper.BodyDecoder
import kotlin.reflect.KType

// Decodes UTF-8 leniently on purpose: malformed bytes (e.g. unlabelled Latin-1) become U+FFFD instead of a 400.
// Always yields a String and ignores the requested type.
class PlainTextDecoder : BodyDecoder {
    @Suppress("UNCHECKED_CAST")
    override fun <T> decode(bytes: ByteArray, type: KType): T = String(bytes, Charsets.UTF_8) as T
}
