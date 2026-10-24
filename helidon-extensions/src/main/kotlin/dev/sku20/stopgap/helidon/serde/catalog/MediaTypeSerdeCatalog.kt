package dev.sku20.stopgap.helidon.serde.catalog

import dev.sku20.stopgap.helidon.serde.Deserializer
import dev.sku20.stopgap.helidon.serde.Serializer
import dev.sku20.stopgap.helidon.serde.exceptions.UnsupportedMediaTypeException
import io.helidon.http.HttpMediaType
import io.helidon.http.ServerRequestHeaders

class MediaTypeSerdeCatalog : SerdeCatalog {

    private val deserializers = mutableMapOf<String, Deserializer>()

    fun add(deserializer: Deserializer) {
        deserializers[key(deserializer.mediaType)] = deserializer
    }

    override fun getDeserializer(headers: ServerRequestHeaders): Deserializer {
        val contentType = headers.contentType().orElseThrow { UnsupportedMediaTypeException(null) }
        return deserializers[key(contentType)] ?: throw UnsupportedMediaTypeException(contentType)
    }

    override fun getSerializer(headers: ServerRequestHeaders): Serializer = TODO()

    // Helidon keeps type, subtype and parameter values as sent, but media types are case-insensitive.
    private fun key(mediaType: HttpMediaType): String = mediaType.text().lowercase()
}
