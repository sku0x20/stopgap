package dev.sku20.helidon.serde

import io.helidon.http.HttpMediaTypes
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class MapSerdeCatalogTest {

    private val catalog = MapSerdeCatalog()
    private val serde: Serde = mock()
    private val otherSerde: Serde = mock()

    @BeforeEach
    fun setup() {
        whenever(serde.mediaType).thenReturn(HttpMediaTypes.JSON_UTF_8)
        whenever(otherSerde.mediaType).thenReturn(HttpMediaTypes.JSON_UTF_8)
    }

    @Test
    fun returnsAddedSerde() {
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
        catalog.add(serde)
        catalog.add(otherSerde)

        assertThat(catalog.get(HttpMediaTypes.JSON_UTF_8)).isSameAs(otherSerde)
    }
}
