package dev.sku20.stopgap.helidon.mapper.res.bodyresmapper.plaintext

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PlainTextEncoderTest {

    private val encoder = PlainTextEncoder()

    @Test
    fun encodesUtf8() {
        val bytes = encoder.encode("héllo")

        assertThat(bytes).isEqualTo("héllo".toByteArray(Charsets.UTF_8))
    }

    @Test
    fun encodesToString() {
        val bytes = encoder.encode(42)

        assertThat(bytes).isEqualTo("42".toByteArray(Charsets.UTF_8))
    }
}
