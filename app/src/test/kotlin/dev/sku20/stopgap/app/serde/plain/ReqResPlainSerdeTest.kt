package dev.sku20.stopgap.app.serde.plain

import dev.sku20.stopgap.app.serde.ReqDto
import dev.sku20.stopgap.app.serde.ResDto
import io.helidon.http.HttpMediaType
import io.helidon.http.HttpMediaTypes
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ReqResPlainSerdeTest {

    private val serde = ReqResPlainSerde()

    @Test
    fun mediaType() {
        assertThat(serde.mediaType as Any).isEqualTo(HttpMediaTypes.PLAINTEXT_UTF_8)
    }

    @Test
    fun serializes() {
        val bytes = serde.serialize(ResDto("alice"))
        assertThat(String(bytes)).isEqualTo("alice")
    }

    @Test
    fun deserializes() {
        val bytes = "alice".toByteArray()
        assertThat(serde.deserialize(bytes, ReqDto::class)).isEqualTo(ReqDto("alice"))
    }
}
