package dev.sku20.stopgap.helidon.serde.catalog

import dev.sku20.stopgap.helidon.serde.Deserializer
import dev.sku20.stopgap.helidon.serde.Serializer
import dev.sku20.stopgap.helidon.serde.exceptions.UnsupportedMediaTypeException
import io.helidon.http.ServerRequestHeaders

class CachingMediaTypeSerdeCatalog : SerdeCatalog {

    private val deserializers = mutableListOf<Deserializer>()

    fun add(deserializer: Deserializer) {
        deserializers.add(deserializer)
    }

    override fun getDeserializer(headers: ServerRequestHeaders): Deserializer {
        val contentType = headers.contentType().orElseThrow { UnsupportedMediaTypeException(null) }
        for (deserializer in deserializers) {
            if (deserializer.canDeserialize(contentType)) return deserializer
        }
        throw UnsupportedMediaTypeException(contentType)
    }

    override fun getSerializer(headers: ServerRequestHeaders): Serializer = TODO()
}
