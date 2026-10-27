package dev.sku20.stopgap.helidon.mapper.res.bodyresmapper.plaintext

import dev.sku20.stopgap.helidon.mapper.res.bodyresmapper.BodyEncoder

// Encodes any value via toString(), so non-String values still produce a body instead of failing.
class PlainTextEncoder : BodyEncoder {
    override fun <T> encode(value: T): ByteArray = value.toString().toByteArray(Charsets.UTF_8)
}
