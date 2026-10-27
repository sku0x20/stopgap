package dev.sku20.stopgap.app.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.uuid.Uuid

class FastJsonEncoderTest {

    private val encoder = FastJsonEncoder()

    @Test
    fun encodes() {
        val bytes = encoder.encode(SamplePayload(1, "alice"))

        assertThat(String(bytes)).isEqualTo("""{"id":1,"name":"alice"}""")
    }

    @Test
    fun encodesUuidAsString() {
        val id = Uuid.random()

        val bytes = encoder.encode(UuidPayload(id))

        assertThat(String(bytes)).isEqualTo("""{"id":"$id"}""")
    }

    data class SamplePayload(val id: Int, val name: String)
    data class UuidPayload(val id: Uuid)
}
