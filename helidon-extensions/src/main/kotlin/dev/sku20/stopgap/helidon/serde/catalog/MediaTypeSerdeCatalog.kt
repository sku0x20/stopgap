package dev.sku20.stopgap.helidon.serde.catalog

import dev.sku20.stopgap.helidon.serde.Deserializer
import dev.sku20.stopgap.helidon.serde.Serializer
import dev.sku20.stopgap.helidon.serde.exceptions.UnsupportedMediaTypeException
import io.helidon.http.HttpMediaType
import io.helidon.http.ServerRequestHeaders

class MediaTypeSerdeCatalog : SerdeCatalog {

    private val deserializers = mutableMapOf<String, MutableList<Deserializer>>()

    fun add(deserializer: Deserializer) {
        deserializers.getOrPut(key(deserializer.mediaType)) { mutableListOf() }.add(deserializer)
    }

    override fun getDeserializer(headers: ServerRequestHeaders): Deserializer {
        val contentType = headers.contentType().orElseThrow { UnsupportedMediaTypeException(null) }
        val candidates = deserializers[key(contentType)] ?: throw UnsupportedMediaTypeException(contentType)
        return fullMatch(candidates, contentType) ?: candidates.first()
    }

    override fun getSerializer(headers: ServerRequestHeaders): Serializer = TODO()

    private fun fullMatch(candidates: List<Deserializer>, contentType: HttpMediaType): Deserializer? {
        val charset = contentType.charset().orElse(null) ?: return null
        return candidates.firstOrNull { it.mediaType.charset().orElse(null).equals(charset, ignoreCase = true) }
    }

    // Type/subtype only: HttpMediaType equality includes parameters (charset) and is case-sensitive.
    private fun key(mediaType: HttpMediaType): String =
        "${mediaType.mediaType().type()}/${mediaType.mediaType().subtype()}".lowercase()
}
