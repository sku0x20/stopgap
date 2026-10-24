package dev.sku20.stopgap.helidon.serde.catalog

import dev.sku20.stopgap.helidon.serde.Deserializer
import io.helidon.common.media.type.MediaTypes
import io.helidon.http.HttpMediaTypes
import io.helidon.http.ServerRequestHeaders
import io.helidon.http.WritableHeaders
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class MediaTypeSerdeCatalogTest {

    private val catalog = MediaTypeSerdeCatalog()
    private val deserializer = mock<Deserializer>()

    @Test
    fun getDeserializerReturnsAddedDeserializer() {
        catalog.addDeserializer(HttpMediaTypes.JSON_UTF_8, deserializer)

        val headers = ServerRequestHeaders.create(WritableHeaders.create().contentType(MediaTypes.APPLICATION_JSON))

        assertThat(catalog.getDeserializer(headers)).isSameAs(deserializer)
    }
}
