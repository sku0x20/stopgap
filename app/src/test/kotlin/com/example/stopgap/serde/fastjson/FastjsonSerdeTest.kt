package com.example.stopgap.serde.fastjson

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
    fun failsOnGenericType() {
        val bytes = """{"data":{"id":1,"name":"alice"}}""".toByteArray()
        assertThatThrownBy { serde.deserialize<Wrapper<SamplePayload>>(bytes, typeOf<Wrapper<SamplePayload>>()) }
            .isInstanceOf(UnsupportedOperationException::class.java)
    }

    data class SamplePayload(val id: Int, val name: String)
    data class Wrapper<T>(val data: T)
}