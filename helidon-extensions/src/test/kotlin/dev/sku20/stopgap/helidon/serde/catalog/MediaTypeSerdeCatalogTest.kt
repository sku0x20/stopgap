package dev.sku20.stopgap.helidon.serde.catalog

import dev.sku20.stopgap.helidon.serde.Deserializer
import io.helidon.http.HeaderNames
import io.helidon.http.HttpMediaType
import io.helidon.http.ServerRequestHeaders
import io.helidon.http.WritableHeaders
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class MediaTypeSerdeCatalogTest {

    private val catalog = MediaTypeSerdeCatalog()

    @Test
    fun getDeserializerReturnsMatch() {
        val utf8 = deserializerOf("text/plain; charset=UTF-8")
        val latin1 = deserializerOf("text/plain; charset=ISO-8859-1")
        catalog.add(utf8)
        catalog.add(latin1)

        assertThat(catalog.getDeserializer(contentType("text/plain; charset=UTF-8")))
            .isSameAs(utf8)
        assertThat(catalog.getDeserializer(contentType("text/plain; charset=ISO-8859-1")))
            .isSameAs(latin1)
    }

    private fun deserializerOf(mediaType: String): Deserializer {
        val deserializer = mock<Deserializer>()
        whenever(deserializer.canDeserialize(HttpMediaType.create(mediaType))).thenReturn(true)
        return deserializer
    }

    private fun contentType(value: String): ServerRequestHeaders =
        ServerRequestHeaders.create(WritableHeaders.create().set(HeaderNames.CONTENT_TYPE, value))
}
