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

        assertThat(catalog.getDeserializer(HttpMediaTypes.JSON_UTF_8)).isSameAs(serde)
    }

    @Test
    fun getDeserializerReturnsSerdeMatchingMediaType() {
        catalog.add(serde)
        catalog.add(plainTextSerde)

        assertThat(catalog.getDeserializer(HttpMediaTypes.JSON_UTF_8)).isSameAs(serde)
        assertThat(catalog.getDeserializer(HttpMediaTypes.PLAINTEXT_UTF_8)).isSameAs(plainTextSerde)
    }

    @Test
    fun throwsWhenNoSerdeRegisteredForMediaType() {
        assertThatThrownBy { catalog.getDeserializer(HttpMediaTypes.JSON_UTF_8) }
            .isInstanceOf(UnsupportedMediaTypeException::class.java)
    }

    @Test
    fun throwsWhenMediaTypeIsNull() {
        assertThatThrownBy { catalog.getDeserializer(null) }
            .isInstanceOf(UnsupportedMediaTypeException::class.java)
    }

    @Test
    fun throwsWhenAddingDuplicateMediaType() {
        catalog.add(serde)

        assertThatThrownBy { catalog.add(otherSerde) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun getSerializerReturnsFirstPreferredMatch() {
        catalog.add(serde)
        catalog.add(plainTextSerde)

        val result = catalog.getSerializer(listOf(HttpMediaTypes.PLAINTEXT_UTF_8, HttpMediaTypes.JSON_UTF_8))

        assertThat(result).isSameAs(plainTextSerde)
    }

    @Test
    fun getSerializerSkipsUnmatchedPreferredTypes() {
        catalog.add(plainTextSerde)

        val result = catalog.getSerializer(listOf(HttpMediaTypes.JSON_UTF_8, HttpMediaTypes.PLAINTEXT_UTF_8))

        assertThat(result).isSameAs(plainTextSerde)
    }

    @Test
    fun throwsWhenNoSerdeMatchesAnyTypeInList() {
        catalog.add(plainTextSerde)

        assertThatThrownBy { catalog.getSerializer(listOf(HttpMediaTypes.JSON_UTF_8)) }
            .isInstanceOf(NotAcceptableException::class.java)
    }

    @Test
    fun throwsWhenMediaTypesListIsEmpty() {
        assertThatThrownBy { catalog.getSerializer(emptyList()) }
            .isInstanceOf(NotAcceptableException::class.java)
    }
}
