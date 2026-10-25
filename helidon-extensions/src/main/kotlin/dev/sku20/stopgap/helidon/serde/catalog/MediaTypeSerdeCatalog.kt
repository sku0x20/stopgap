package dev.sku20.stopgap.helidon.serde.catalog

import dev.sku20.stopgap.helidon.serde.Deserializer
import dev.sku20.stopgap.helidon.serde.Serializer
import dev.sku20.stopgap.helidon.serde.exceptions.MissingContentTypeException
import dev.sku20.stopgap.helidon.serde.exceptions.UnsupportedMediaTypeException
import io.helidon.http.ServerRequestHeaders

class MediaTypeSerdeCatalog : SerdeCatalog {

    private val deserializers = mutableListOf<Deserializer>()
    private val serializers = mutableListOf<Serializer>()

    fun addDeserializer(deserializer: Deserializer) {
        deserializers.add(deserializer)
    }

    fun addSerializer(serializer: Serializer) {
        serializers.add(serializer)
    }

    // todo: cache Content-Type text -> deserializer. Clients pick the key, so bound it: fill-once capacity
    //  (or count-min sketch admission), skip long keys and 415s. Benchmark against the plain scan first.
    override fun getDeserializer(headers: ServerRequestHeaders): Deserializer {
        val contentType = headers.contentType().orElseThrow { MissingContentTypeException() }
        for (deserializer in deserializers) {
            if (deserializer.canDeserialize(contentType)) return deserializer
        }
        throw UnsupportedMediaTypeException(contentType)
    }

    override fun getSerializer(headers: ServerRequestHeaders): Serializer = TODO()
}
