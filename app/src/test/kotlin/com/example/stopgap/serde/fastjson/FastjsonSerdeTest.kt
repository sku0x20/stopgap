package com.example.stopgap.serde.fastjson

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class FastjsonSerdeTest {

    private val serde = FastjsonSerde()

    data class SamplePayload(val id: Int, val name: String)

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
}