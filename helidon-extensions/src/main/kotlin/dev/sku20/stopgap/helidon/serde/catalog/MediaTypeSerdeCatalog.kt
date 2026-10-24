package dev.sku20.stopgap.helidon.serde.catalog

import dev.sku20.stopgap.helidon.serde.Deserializer
import dev.sku20.stopgap.helidon.serde.Serializer
import io.helidon.http.ServerRequestHeaders

class MediaTypeSerdeCatalog : SerdeCatalog {

    override fun getDeserializer(headers: ServerRequestHeaders): Deserializer = TODO()

    override fun getSerializer(headers: ServerRequestHeaders): Serializer = TODO()
}
