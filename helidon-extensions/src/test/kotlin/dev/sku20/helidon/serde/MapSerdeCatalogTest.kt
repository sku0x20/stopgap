package dev.sku20.helidon.serde

import io.helidon.http.HttpMediaTypes
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class MapSerdeCatalogTest {

    private val catalog = MapSerdeCatalog()

    @Test
    fun returnsAddedSerde() {
        val serde = mock<Serde> { on { mediaType } doReturn HttpMediaTypes.JSON_UTF_8 }
        catalog.add(serde)

        assertThat(catalog.get(HttpMediaTypes.JSON_UTF_8)).isSameAs(serde)
    }

    @Test
    fun throwsWhenNoSerdeRegisteredForMediaType() {
        assertThatThrownBy { catalog.get(HttpMediaTypes.JSON_UTF_8) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun lastAddedSerdeWinsForSameMediaType() {
        val first = mock<Serde> { on { mediaType } doReturn HttpMediaTypes.JSON_UTF_8 }
        val second = mock<Serde> { on { mediaType } doReturn HttpMediaTypes.JSON_UTF_8 }
        catalog.add(first)
        catalog.add(second)

        assertThat(catalog.get(HttpMediaTypes.JSON_UTF_8)).isSameAs(second)
    }
}
