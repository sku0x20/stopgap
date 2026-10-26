package dev.sku20.stopgap.helidon.mapper

import io.helidon.http.HttpMediaType
import kotlin.reflect.KType

interface MediaTypeReqMapper {
    fun accepts(mediaType: HttpMediaType?): Boolean
    fun <T> decode(bytes: ByteArray, type: KType): T
}
