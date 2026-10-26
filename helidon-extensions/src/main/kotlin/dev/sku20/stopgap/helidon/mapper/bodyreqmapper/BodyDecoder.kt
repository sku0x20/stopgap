package dev.sku20.stopgap.helidon.mapper.bodyreqmapper

import kotlin.reflect.KType

interface BodyDecoder {
    /** Throws a 400 `HttpException` subclass for bytes it cannot decode, so library errors never surface as a 500. */
    fun <T> decode(bytes: ByteArray, type: KType): T
}
