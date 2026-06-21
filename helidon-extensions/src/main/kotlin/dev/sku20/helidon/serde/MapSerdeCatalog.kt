package dev.sku20.helidon.serde

import io.helidon.http.HttpMediaType

/**
 * A generic [SerdeCatalog] backed by a map, with at most one [Serde] per media type.
 *
 * Throws [UnsupportedMediaTypeException] when no [Serde] matches. Does not support
 * wildcard media types (e.g. `*​/​*`).
 */
class MapSerdeCatalog : SerdeCatalog {

    private val serdes = HashMap<HttpMediaType, Serde>()

    fun add(serde: Serde) {
        require(serde.mediaType !in serdes) { "Serde already registered for ${serde.mediaType}" }
        serdes[serde.mediaType] = serde
    }

    override fun get(mediaType: HttpMediaType?): Serde {
        if (mediaType == null) throw UnsupportedMediaTypeException(mediaType)
        return serdes[mediaType] ?: throw UnsupportedMediaTypeException(mediaType)
    }

    override fun get(mediaTypes: List<HttpMediaType>): Serde {
        for (mediaType in mediaTypes) {
            serdes[mediaType]?.let { return it }
        }
        throw UnsupportedMediaTypeException(mediaTypes.firstOrNull())
    }
}
