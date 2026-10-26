package dev.sku20.stopgap.helidon.mapper.json

import kotlin.reflect.KType

interface JsonDecoder {
    fun <T> decode(bytes: ByteArray, type: KType): T
}
