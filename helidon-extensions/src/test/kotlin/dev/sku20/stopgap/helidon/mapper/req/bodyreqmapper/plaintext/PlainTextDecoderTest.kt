package dev.sku20.stopgap.helidon.mapper.req.bodyreqmapper.plaintext

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.reflect.typeOf

class PlainTextDecoderTest {

    private val decoder = PlainTextDecoder()

    @Test
    fun decodesUtf8() {
        val text = decoder.decode<String>("héllo".toByteArray(Charsets.UTF_8), typeOf<String>())

        assertThat(text).isEqualTo("héllo")
    }

    @Test
    fun replacesMalformedBytes() {
        val text = decoder.decode<String>("héllo".toByteArray(Charsets.ISO_8859_1), typeOf<String>())

        assertThat(text).isEqualTo("h�llo")
    }
}
