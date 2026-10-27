package dev.sku20.stopgap.helidon.mapper.res.bodyresmapper.plaintext

import dev.sku20.stopgap.helidon.mapper.res.bodyresmapper.BodyEncoder

// A non-String value means an endpoint is wired to the wrong mapper: a server bug, not a client error.
class PlainTextEncoder : BodyEncoder {
    override fun <T> encode(value: T): ByteArray {
        require(value is String) { "PlainTextEncoder encodes String only, got ${value?.javaClass?.name}" }
        return value.toByteArray(Charsets.UTF_8)
    }
}
