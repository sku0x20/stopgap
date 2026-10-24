package dev.sku20.stopgap.helidon.serde.catalog

import dev.sku20.stopgap.helidon.serde.Deserializer
import io.helidon.common.media.type.MediaTypes
import io.helidon.http.HeaderNames
import io.helidon.http.HttpMediaType
import io.helidon.http.HttpMediaTypes
import io.helidon.http.ServerRequestHeaders
import io.helidon.http.WritableHeaders
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class MediaTypeSerdeCatalogTest {

    private val catalog = MediaTypeSerdeCatalog()
    private val deserializer = mock<Deserializer> { on { mediaType } doReturn HttpMediaTypes.JSON_UTF_8 }

    @Test
    fun getDeserializerReturnsAddedDeserializer() {
        catalog.add(deserializer)

        val headers = ServerRequestHeaders.create(WritableHeaders.create().contentType(MediaTypes.APPLICATION_JSON))

        assertThat(catalog.getDeserializer(headers)).isSameAs(deserializer)
    }

    @Test
    fun getDeserializerMatchesTypeAndCharset() {
        val utf8 = mock<Deserializer> { on { mediaType } doReturn HttpMediaType.create("text/plain; charset=UTF-8") }
        val latin1 = mock<Deserializer> { on { mediaType } doReturn HttpMediaType.create("text/plain; charset=ISO-8859-1") }
        catalog.add(utf8)
        catalog.add(latin1)

        assertThat(catalog.getDeserializer(contentType("text/plain; charset=UTF-8"))).isSameAs(utf8)
        assertThat(catalog.getDeserializer(contentType("text/plain; charset=ISO-8859-1"))).isSameAs(latin1)
    }

    private fun contentType(value: String): ServerRequestHeaders =
        ServerRequestHeaders.create(WritableHeaders.create().set(HeaderNames.CONTENT_TYPE, value))
}
