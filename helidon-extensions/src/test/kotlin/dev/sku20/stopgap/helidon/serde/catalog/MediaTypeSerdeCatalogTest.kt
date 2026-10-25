package dev.sku20.stopgap.helidon.serde.catalog

import dev.sku20.stopgap.helidon.serde.Deserializer
import dev.sku20.stopgap.helidon.serde.Serializer
import dev.sku20.stopgap.helidon.serde.exceptions.MissingContentTypeException
import dev.sku20.stopgap.helidon.serde.exceptions.UnsupportedMediaTypeException
import io.helidon.http.HeaderNames
import io.helidon.http.HttpMediaType
import io.helidon.http.ServerRequestHeaders
import io.helidon.http.WritableHeaders
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class MediaTypeSerdeCatalogTest {

    private val catalog = MediaTypeSerdeCatalog()

    @Test
    fun getDeserializerReturnsMatch() {
        val utf8 = deserializerOf("text/plain; charset=UTF-8")
        val latin1 = deserializerOf("text/plain; charset=ISO-8859-1")
        catalog.addDeserializer(utf8)
        catalog.addDeserializer(latin1)

        assertThat(catalog.getDeserializer(contentType("text/plain; charset=UTF-8")))
            .isSameAs(utf8)
        assertThat(catalog.getDeserializer(contentType("text/plain; charset=ISO-8859-1")))
            .isSameAs(latin1)
    }

    @Test
    fun getDeserializerRejectsUnsupported() {
        catalog.addDeserializer(deserializerOf("application/json"))

        assertThatThrownBy { catalog.getDeserializer(contentType("text/plain")) }
            .isInstanceOf(UnsupportedMediaTypeException::class.java)
            .hasMessage("Unsupported Content-Type: text/plain")
    }

    @Test
    fun getDeserializerRejectsMissingContentType() {
        catalog.addDeserializer(deserializerOf("application/json"))

        assertThatThrownBy { catalog.getDeserializer(ServerRequestHeaders.create(WritableHeaders.create())) }
            .isInstanceOf(MissingContentTypeException::class.java)
    }

    @Test
    fun getSerializerMatchesAccept() {
        val json = serializerOf("application/json")
        val text = serializerOf("text/plain")
        catalog.addSerializer(json)
        catalog.addSerializer(text)

        assertThat(catalog.getSerializer(accept("text/plain"))).isSameAs(text)
        assertThat(catalog.getSerializer(accept("application/json"))).isSameAs(json)
    }

    private fun deserializerOf(mediaType: String): Deserializer {
        val deserializer = mock<Deserializer>()
        whenever(deserializer.canDeserialize(HttpMediaType.create(mediaType))).thenReturn(true)
        return deserializer
    }

    private fun serializerOf(mediaType: String): Serializer {
        val serializer = mock<Serializer>()
        whenever(serializer.mediaType).thenReturn(HttpMediaType.create(mediaType))
        return serializer
    }

    private fun contentType(value: String): ServerRequestHeaders =
        ServerRequestHeaders.create(WritableHeaders.create().set(HeaderNames.CONTENT_TYPE, value))

    private fun accept(value: String): ServerRequestHeaders =
        ServerRequestHeaders.create(WritableHeaders.create().set(HeaderNames.ACCEPT, value))
}
