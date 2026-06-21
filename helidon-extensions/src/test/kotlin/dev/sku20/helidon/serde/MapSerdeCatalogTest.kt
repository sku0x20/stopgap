package dev.sku20.helidon.serde

import io.helidon.http.HttpMediaType
import io.helidon.http.HttpMediaTypes
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import kotlin.reflect.KClass
import kotlin.reflect.KType

class MapSerdeCatalogTest {

    private val catalog = MapSerdeCatalog()

    @Test
    fun returnsAddedSerde() {
        val serde = FakeSerde(HttpMediaTypes.JSON_UTF_8)
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
        val first = FakeSerde(HttpMediaTypes.JSON_UTF_8)
        val second = FakeSerde(HttpMediaTypes.JSON_UTF_8)
        catalog.add(first)
        catalog.add(second)

        assertThat(catalog.get(HttpMediaTypes.JSON_UTF_8)).isSameAs(second)
    }

    private class FakeSerde(override val mediaType: HttpMediaType) : Serde {
        override fun <T : Any> deserialize(bytes: ByteArray, kClazz: KClass<T>): T {
            throw UnsupportedOperationException()
        }

        override fun <T> deserialize(bytes: ByteArray, type: KType): T {
            throw UnsupportedOperationException()
        }

        override fun <T> serialize(obj: T): ByteArray {
            throw UnsupportedOperationException()
        }
    }
}
