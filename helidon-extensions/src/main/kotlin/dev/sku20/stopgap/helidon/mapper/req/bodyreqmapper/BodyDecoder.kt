package dev.sku20.stopgap.helidon.mapper.req.bodyreqmapper

import dev.sku20.stopgap.helidon.mapper.req.exception.MalformedBodyException
import kotlin.reflect.KType

interface BodyDecoder {
    /** Throws [MalformedBodyException] for bytes it cannot decode, so library errors never surface as a 500. */
    fun <T> decode(bytes: ByteArray, type: KType): T
}
