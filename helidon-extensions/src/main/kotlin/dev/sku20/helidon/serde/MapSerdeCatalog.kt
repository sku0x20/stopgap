package dev.sku20.helidon.serde

import io.helidon.http.HttpMediaType

/**
 * A generic [SerdeCatalog] backed by a map, with at most one [Serde] per media type.
 *
 * Throws [UnsupportedMediaTypeException] from [get] when no [Serde] is registered for the
 * requested media type.
 */
class MapSerdeCatalog : SerdeCatalog {

    private val serdes = HashMap<HttpMediaType, Serde>()

    fun add(serde: Serde) {
        require(serde.mediaType !in serdes) { "Serde already registered for ${serde.mediaType}" }
        serdes[serde.mediaType] = serde
    }

    override fun get(mediaType: HttpMediaType): Serde {
        return serdes[mediaType] ?: throw UnsupportedMediaTypeException(mediaType)
    }
}
