package dev.sku20.stopgap.helidon.mapper.bodyreqmapper

import io.helidon.http.HttpMediaType
import kotlin.reflect.KType

class CompositeMediaTypeReqMapper(mappers: List<MediaTypeReqMapper> = emptyList()) : MediaTypeReqMapper {
    private val mappers = mappers.toMutableList()

    fun add(mapper: MediaTypeReqMapper) {
        mappers.add(mapper)
    }

    override fun accepts(mediaType: HttpMediaType?): Boolean = TODO()

    override fun <T> decode(bytes: ByteArray, type: KType): T = TODO()
}
