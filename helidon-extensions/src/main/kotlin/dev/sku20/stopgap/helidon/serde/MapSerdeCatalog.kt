package dev.sku20.stopgap.helidon.serde

import io.helidon.http.HttpMediaType

/**
 * A generic [SerdeCatalog] backed by a map, with at most one [Serde] per media type.
 *
 * Throws [UnsupportedMediaTypeException]/[NotAcceptableException] when no [Serde] matches.
 * Does not support wildcard media types (e.g. `*​/​*`).
 */
class MapSerdeCatalog : SerdeCatalog {

    private val serdes = HashMap<HttpMediaType, Serde>()

    fun add(serde: Serde) {
        require(serde.mediaType !in serdes) { "Serde already registered for ${serde.mediaType}" }
        serdes[serde.mediaType] = serde
    }

    override fun getDeserializer(mediaType: HttpMediaType?): Serde {
        if (mediaType == null) throw UnsupportedMediaTypeException(mediaType)
        return serdes[mediaType] ?: throw UnsupportedMediaTypeException(mediaType)
    }

    override fun getSerializer(mediaTypes: List<HttpMediaType>): Serde {
        for (mediaType in mediaTypes) {
            val serde = serdes[mediaType]
            if (serde != null) return serde
        }
        throw NotAcceptableException(mediaTypes)
    }
}
