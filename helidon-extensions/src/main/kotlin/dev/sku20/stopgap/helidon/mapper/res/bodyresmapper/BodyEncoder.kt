package dev.sku20.stopgap.helidon.mapper.res.bodyresmapper

interface BodyEncoder {
    fun <T> encode(value: T): ByteArray
}
