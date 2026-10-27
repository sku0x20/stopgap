package dev.sku20.stopgap.helidon.mapper.req.multipart.part

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream

class PartReaderTest {

    @Test
    fun returnsNullAtCloseDelimiter() {
        val reader = PartReader("abc", ByteArrayInputStream("--abc--".toByteArray()))

        assertThat(reader.part()).isNull()
    }
}
