package com.example.stopgap.serde.plain

import dev.sku20.stopgap.helidon.serde.Serde
import dev.sku20.stopgap.helidon.serde.SerdeCatalog
import io.helidon.http.HttpMediaType

class PlainTextSerdeCatalog : SerdeCatalog {

    private val serde: Serde = ReqResPlainSerde()

    override fun getDeserializer(mediaType: HttpMediaType?): Serde = serde

    override fun getSerializer(mediaTypes: List<HttpMediaType>): Serde = serde
}
