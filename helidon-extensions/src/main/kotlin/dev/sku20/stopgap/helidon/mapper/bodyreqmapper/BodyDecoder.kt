package dev.sku20.stopgap.helidon.mapper.bodyreqmapper

import kotlin.reflect.KType

interface BodyDecoder {
    fun <T> decode(bytes: ByteArray, type: KType): T
}
