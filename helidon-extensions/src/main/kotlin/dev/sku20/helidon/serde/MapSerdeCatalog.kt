package dev.sku20.helidon.serde

import io.helidon.http.HttpMediaType

/**
 * A generic [SerdeCatalog] backed by a map, with at most one [Serde] per media type.
 *
 * Both [get] overloads throw [UnsupportedMediaTypeException] when no [Serde] is registered
 * for the requested media type(s), or when none was specified at all (`null`, or an empty
 * list) — a missing Content-Type/Accept header is treated the same as an unsupported one
 * (RFC 7231 §3.1.1.5 permits this).
 *
 * The wildcard media type `&#42;/&#42;` (and other wildcards) is not given special treatment
 * here — it's matched literally against registered media types, so it will not resolve unless
 * a [Serde] is registered for that exact wildcard. Different [SerdeCatalog] implementations
 * can choose to support wildcard or "default" handling (e.g. fall back to a designated
 * [Serde] when nothing else matches).
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
