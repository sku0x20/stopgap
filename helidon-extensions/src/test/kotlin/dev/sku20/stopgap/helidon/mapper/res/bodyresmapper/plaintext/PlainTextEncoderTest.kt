package dev.sku20.stopgap.helidon.mapper.res.bodyresmapper.plaintext

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class PlainTextEncoderTest {

    private val encoder = PlainTextEncoder()

    @Test
    fun encodesUtf8() {
        val bytes = encoder.encode("héllo")

        assertThat(bytes).isEqualTo("héllo".toByteArray(Charsets.UTF_8))
    }

    @Test
    fun throwsOnNonString() {
        assertThatThrownBy { encoder.encode(42) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun throwsOnNull() {
        assertThatThrownBy { encoder.encode(null) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }
}
