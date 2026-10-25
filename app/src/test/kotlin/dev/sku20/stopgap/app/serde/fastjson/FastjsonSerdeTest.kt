package dev.sku20.stopgap.app.serde.fastjson

import io.helidon.http.HttpMediaType
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import kotlin.reflect.typeOf

class FastjsonSerdeTest {

    private val serde = FastjsonSerde()

    @Test
    fun serializes() {
        val bytes = serde.serialize(SamplePayload(1, "alice"))
        assertThat(String(bytes)).isEqualTo("""{"id":1,"name":"alice"}""")
    }

    @Test
    fun deserializes() {
        val bytes = """{"id":1,"name":"alice"}""".toByteArray()
        assertThat(serde.deserialize(bytes, SamplePayload::class)).isEqualTo(SamplePayload(1, "alice"))
    }

    @Test
    fun deserializesGeneric() {
        val bytes = """{"data":{"id":1,"name":"alice"}}""".toByteArray()
        val type = typeOf<Wrapper<SamplePayload>>()
        val result = serde.deserialize<Wrapper<SamplePayload>>(bytes, type)
        assertThat(result).isEqualTo(Wrapper(SamplePayload(1, "alice")))
    }

    @Test
    fun deserializeMissingField() {
        assertThatThrownBy { serde.deserialize("""{"id":1}""".toByteArray(), SamplePayload::class) }
            .isInstanceOf(MalformedBodyException::class.java)
    }

    @Test
    fun deserializeInvalidJson() {
        assertThatThrownBy { serde.deserialize("""{not json""".toByteArray(), SamplePayload::class) }
            .isInstanceOf(MalformedBodyException::class.java)
    }

    @Test
    fun deserializeEmpty() {
        assertThatThrownBy { serde.deserialize(ByteArray(0), SamplePayload::class) }
            .isInstanceOf(MalformedBodyException::class.java)
    }

    @Test
    fun deserializeGenericInvalidJson() {
        val type = typeOf<Wrapper<SamplePayload>>()
        assertThatThrownBy { serde.deserialize<Wrapper<SamplePayload>>("[1]".toByteArray(), type) }
            .isInstanceOf(MalformedBodyException::class.java)
    }

    @Test
    fun canDeserializeJson() {
        assertThat(serde.canDeserialize(HttpMediaType.create("application/json"))).isTrue()
        assertThat(serde.canDeserialize(HttpMediaType.create("application/json; charset=utf-8"))).isTrue()
    }

    @Test
    fun canDeserializeIgnoresCase() {
        assertThat(serde.canDeserialize(HttpMediaType.create("APPlication/JSON"))).isTrue()
        assertThat(serde.canDeserialize(HttpMediaType.create("application/json; charset=UTF-8"))).isTrue()
    }

    @Test
    fun cannotDeserializeOtherTypes() {
        assertThat(serde.canDeserialize(HttpMediaType.create("text/plain"))).isFalse()
        assertThat(serde.canDeserialize(HttpMediaType.create("application/json; charset=iso-8859-1"))).isFalse()
    }

    data class SamplePayload(val id: Int, val name: String)
    data class Wrapper<T>(val data: T)
}