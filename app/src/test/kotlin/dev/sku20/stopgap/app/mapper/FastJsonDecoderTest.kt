package dev.sku20.stopgap.app.mapper

import com.alibaba.fastjson2.JSONException
import dev.sku20.stopgap.helidon.mapper.req.exception.MalformedBodyException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import kotlin.reflect.typeOf
import kotlin.uuid.Uuid

class FastJsonDecoderTest {

    private val decoder = FastJsonDecoder()

    @Test
    fun decodes() {
        val payload = decoder.decode<SamplePayload>("""{"id":1,"name":"alice"}""".toByteArray(), typeOf<SamplePayload>())

        assertThat(payload).isEqualTo(SamplePayload(1, "alice"))
    }

    @Test
    fun decodesGeneric() {
        val bytes = """{"data":{"id":1,"name":"alice"}}""".toByteArray()

        val wrapper = decoder.decode<Wrapper<SamplePayload>>(bytes, typeOf<Wrapper<SamplePayload>>())

        assertThat(wrapper).isEqualTo(Wrapper(SamplePayload(1, "alice")))
    }

    @Test
    fun decodesUuidFromString() {
        val id = Uuid.random()

        val payload = decoder.decode<UuidPayload>("""{"id":"$id"}""".toByteArray(), typeOf<UuidPayload>())

        assertThat(payload).isEqualTo(UuidPayload(id))
    }

    @Test
    fun decodesNullForNullableType() {
        val payload = decoder.decode<SamplePayload?>("null".toByteArray(), typeOf<SamplePayload?>())

        assertThat(payload).isNull()
    }

    @Test
    fun rejectsInvalidJson() {
        assertThatThrownBy { decoder.decode<SamplePayload>("{not json".toByteArray(), typeOf<SamplePayload>()) }
            .isInstanceOf(MalformedBodyException::class.java)
            .hasCauseInstanceOf(JSONException::class.java)
    }

    @Test
    fun rejectsMissingField() {
        assertThatThrownBy { decoder.decode<SamplePayload>("""{"id":1}""".toByteArray(), typeOf<SamplePayload>()) }
            .isInstanceOf(MalformedBodyException::class.java)
            .hasCauseInstanceOf(NullPointerException::class.java)
    }

    @Test
    fun rejectsGenericMismatch() {
        assertThatThrownBy { decoder.decode<Wrapper<SamplePayload>>("[1]".toByteArray(), typeOf<Wrapper<SamplePayload>>()) }
            .isInstanceOf(MalformedBodyException::class.java)
    }

    @Test
    fun rejectsEmpty() {
        assertThatThrownBy { decoder.decode<SamplePayload>(ByteArray(0), typeOf<SamplePayload>()) }
            .isInstanceOf(MalformedBodyException::class.java)
    }

    @Test
    fun rejectsWhitespace() {
        assertThatThrownBy { decoder.decode<SamplePayload>("  \n".toByteArray(), typeOf<SamplePayload>()) }
            .isInstanceOf(MalformedBodyException::class.java)
    }

    @Test
    fun rejectsNullForNonNullType() {
        assertThatThrownBy { decoder.decode<SamplePayload>("null".toByteArray(), typeOf<SamplePayload>()) }
            .isInstanceOf(MalformedBodyException::class.java)
    }

    data class SamplePayload(val id: Int, val name: String)
    data class Wrapper<T>(val data: T)
    data class UuidPayload(val id: Uuid)
}
