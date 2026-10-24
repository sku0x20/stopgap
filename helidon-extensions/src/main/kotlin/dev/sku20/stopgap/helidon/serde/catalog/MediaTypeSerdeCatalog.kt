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

    private val deserializers = HashMap<String, Matched<Deserializer>>()
    private val serializers = HashMap<String, Matched<Serializer>>()

    fun addDeserializer(mediaType: HttpMediaType, deserializer: Deserializer) {
        register(deserializers, mediaType, deserializer)
    }

    fun addSerializer(mediaType: HttpMediaType, serializer: Serializer) {
        register(serializers, mediaType, serializer)
    }

    override fun getDeserializer(headers: ServerRequestHeaders): Matched<Deserializer> {
        val contentType = headers.contentType().orElse(null) ?: throw UnsupportedMediaTypeException(null)
        return deserializers[key(contentType)] ?: throw UnsupportedMediaTypeException(contentType)
    }

    override fun getSerializer(headers: ServerRequestHeaders): Matched<Serializer> {
        val acceptedTypes = headers.acceptedTypes()
        for (mediaType in acceptedTypes) {
            val serializer = serializers[key(mediaType)]
            if (serializer != null) return serializer
        }
        throw NotAcceptableException(acceptedTypes)
    }

    private fun <T> register(registry: HashMap<String, Matched<T>>, mediaType: HttpMediaType, value: T) {
        val key = key(mediaType)
        require(key !in registry) { "Already registered for $key" }
        registry[key] = Matched(mediaType, value)
    }

    // HttpMediaType/MediaType equality is unusable as a key: it includes parameters, mixes enum and record
    // instances, and is case-sensitive, while type/subtype are case-insensitive (RFC 9110 §8.3.1).
    private fun key(mediaType: HttpMediaType): String =
        "${mediaType.mediaType().type()}/${mediaType.mediaType().subtype()}".lowercase()
}
