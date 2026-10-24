package dev.sku20.stopgap.app.serde.plain

import dev.sku20.stopgap.helidon.serde.SerdeCatalogDeprecated
import dev.sku20.stopgap.helidon.serde.SerdeDeprecated
import io.helidon.http.HttpMediaType

class PlainTextSerdeCatalog : SerdeCatalogDeprecated {

    private val serde: SerdeDeprecated = ReqResPlainSerde()

    override fun getDeserializer(mediaType: HttpMediaType?): SerdeDeprecated = serde

    override fun getSerializer(mediaTypes: List<HttpMediaType>): SerdeDeprecated = serde
}
