package dev.sku20.stopgap.helidon.serde.catalog

import dev.sku20.stopgap.helidon.serde.Deserializer
import dev.sku20.stopgap.helidon.serde.Serializer
import dev.sku20.stopgap.helidon.serde.exceptions.NotAcceptableException
import dev.sku20.stopgap.helidon.serde.exceptions.UnsupportedMediaTypeException
import io.helidon.http.HttpMediaType
import io.helidon.http.ServerRequestHeaders

/**
 * A [SerdeCatalog] that picks by media type: Content-Type for a [Deserializer], Accept for a [Serializer].
 *
 * Matches on type/subtype only, ignoring parameters such as `charset` or `q`, and doesn't support wildcards.
 */
class MediaTypeSerdeCatalog : SerdeCatalog {

    private val deserializers = HashMap<String, Deserializer>()
    private val serializers = HashMap<String, MatchedSerializer>()

    fun addDeserializer(mediaType: HttpMediaType, deserializer: Deserializer) {
        val key = key(mediaType)
        require(key !in deserializers) { "Deserializer already registered for $key" }
        deserializers[key] = deserializer
    }

    fun addSerializer(mediaType: HttpMediaType, serializer: Serializer) {
        val key = key(mediaType)
        require(key !in serializers) { "Serializer already registered for $key" }
        serializers[key] = MatchedSerializer(mediaType, serializer)
    }

    override fun getDeserializer(headers: ServerRequestHeaders): Deserializer {
        val contentType = headers.contentType().orElse(null) ?: throw UnsupportedMediaTypeException(null)
        return deserializers[key(contentType)] ?: throw UnsupportedMediaTypeException(contentType)
    }

    override fun getSerializer(headers: ServerRequestHeaders): MatchedSerializer {
        val acceptedTypes = headers.acceptedTypes()
        for (mediaType in acceptedTypes) {
            val serializer = serializers[key(mediaType)]
            if (serializer != null) return serializer
        }
        throw NotAcceptableException(acceptedTypes)
    }

    // HttpMediaType/MediaType equality is unusable as a key: it includes parameters, mixes enum and record
    // instances, and is case-sensitive, while type/subtype are case-insensitive (RFC 9110 §8.3.1).
    private fun key(mediaType: HttpMediaType): String =
        "${mediaType.mediaType().type()}/${mediaType.mediaType().subtype()}".lowercase()
}
