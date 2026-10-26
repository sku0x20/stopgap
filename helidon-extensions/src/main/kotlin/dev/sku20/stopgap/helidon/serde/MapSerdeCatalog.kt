package dev.sku20.stopgap.helidon.serde

import io.helidon.http.HttpMediaType

/**
 * A generic [SerdeCatalog] backed by a map, with at most one [Serde] per media type.
 *
 * Matches on type/subtype only, ignoring parameters such as `charset` or `q`.
 * Throws [UnsupportedMediaTypeException]/[NotAcceptableException] when no [Serde] matches.
 * Does not support wildcard media types (e.g. `*​/​*`).
 */
class MapSerdeCatalog : SerdeCatalog {

    private val serdes = HashMap<String, Serde>()

    fun add(serde: Serde) {
        val key = key(serde.mediaType)
        require(key !in serdes) { "Serde already registered for $key" }
        serdes[key] = serde
    }

    override fun getDeserializer(mediaType: HttpMediaType?): Serde {
        if (mediaType == null) throw UnsupportedMediaTypeException(mediaType)
        return serdes[key(mediaType)] ?: throw UnsupportedMediaTypeException(mediaType)
    }

    override fun getSerializer(mediaTypes: List<HttpMediaType>): Serde {
        for (mediaType in mediaTypes) {
            val serde = serdes[key(mediaType)]
            if (serde != null) return serde
        }
        throw NotAcceptableException(mediaTypes)
    }

    // HttpMediaType/MediaType equality is unusable as a key: it includes parameters, mixes enum and record
    // instances, and is case-sensitive, while type/subtype are case-insensitive (RFC 9110 §8.3.1).
    private fun key(mediaType: HttpMediaType): String =
        "${mediaType.mediaType().type()}/${mediaType.mediaType().subtype()}".lowercase()
}
