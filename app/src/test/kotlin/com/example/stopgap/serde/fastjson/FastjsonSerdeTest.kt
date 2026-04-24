package com.example.stopgap.serde.fastjson

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class FastjsonSerdeTest {

    private val serde = FastjsonSerde()

    data class SamplePayload(val id: Int, val name: String)

    @Test
    fun serializeNonEmpty() {
        val bytes = serde.serialize(SamplePayload(1, "test"))
        assertThat(bytes).isNotEmpty()
    }

    @Test
    fun serializeDeserializeRoundtrip() {
        val original = SamplePayload(42, "hello")
        val bytes = serde.serialize(original)
        val result = serde.deserialize(bytes, SamplePayload::class)
        assertThat(result).isEqualTo(original)
    }
}