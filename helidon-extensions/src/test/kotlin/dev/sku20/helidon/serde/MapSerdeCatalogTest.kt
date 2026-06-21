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
    private val serde = mock<Serde>()
    private val otherSerde = mock<Serde>()
    private val plainTextSerde = mock<Serde>()

    @BeforeEach
    fun setup() {
        whenever(serde.mediaType).thenReturn(HttpMediaTypes.JSON_UTF_8)
        whenever(otherSerde.mediaType).thenReturn(HttpMediaTypes.JSON_UTF_8)
        whenever(plainTextSerde.mediaType).thenReturn(HttpMediaTypes.PLAINTEXT_UTF_8)
    }

    @Test
    fun returnsAddedSerde() {
        catalog.add(serde)

        assertThat(catalog.get(HttpMediaTypes.JSON_UTF_8)).isSameAs(serde)
    }

    @Test
    fun getReturnsSerdeMatchingMediaType() {
        catalog.add(serde)
        catalog.add(plainTextSerde)

        assertThat(catalog.get(HttpMediaTypes.JSON_UTF_8)).isSameAs(serde)
        assertThat(catalog.get(HttpMediaTypes.PLAINTEXT_UTF_8)).isSameAs(plainTextSerde)
    }

    @Test
    fun throwsWhenNoSerdeRegisteredForMediaType() {
        assertThatThrownBy { catalog.get(HttpMediaTypes.JSON_UTF_8) }
            .isInstanceOf(UnsupportedMediaTypeException::class.java)
    }

    @Test
    fun throwsWhenMediaTypeIsNull() {
        assertThatThrownBy { catalog.get(null) }
            .isInstanceOf(UnsupportedMediaTypeException::class.java)
    }

    @Test
    fun throwsWhenAddingDuplicateMediaType() {
        catalog.add(serde)

        assertThatThrownBy { catalog.add(otherSerde) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun getFromListReturnsFirstPreferredMatch() {
        catalog.add(serde)
        catalog.add(plainTextSerde)

        val result = catalog.get(listOf(HttpMediaTypes.PLAINTEXT_UTF_8, HttpMediaTypes.JSON_UTF_8))

        assertThat(result).isSameAs(plainTextSerde)
    }

    @Test
    fun getFromListSkipsUnmatchedPreferredTypes() {
        catalog.add(plainTextSerde)

        val result = catalog.get(listOf(HttpMediaTypes.JSON_UTF_8, HttpMediaTypes.PLAINTEXT_UTF_8))

        assertThat(result).isSameAs(plainTextSerde)
    }

    @Test
    fun throwsWhenNoSerdeMatchesAnyTypeInList() {
        catalog.add(plainTextSerde)

        assertThatThrownBy { catalog.get(listOf(HttpMediaTypes.JSON_UTF_8)) }
            .isInstanceOf(UnsupportedMediaTypeException::class.java)
    }

    @Test
    fun throwsWhenMediaTypesListIsEmpty() {
        assertThatThrownBy { catalog.get(emptyList()) }
            .isInstanceOf(UnsupportedMediaTypeException::class.java)
    }
}
