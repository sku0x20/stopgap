package dev.sku20.helidon.serde

import io.helidon.http.HttpMediaType

/**
 * A generic [SerdeCatalog] backed by a map, with at most one [Serde] per media type.
 *
 * Throws [UnsupportedMediaTypeException] from [get] when no [Serde] is registered for the
 * requested media type, or when no media type was specified at all — a missing
 * Content-Type/Accept header is treated the same as an unsupported one (RFC 7231 §3.1.1.5
 * permits this).
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
