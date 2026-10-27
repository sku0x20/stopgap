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

    @Test
    fun readsSinglePart() {
        val body = "--abc\r\n" +
            "Content-Disposition: form-data; name=\"title\"\r\n" +
            "Content-Type: text/plain\r\n" +
            "\r\n" +
            "groceries\r\n" +
            "--abc--"
        val reader = PartReader("abc", ByteArrayInputStream(body.toByteArray()))

        val part = reader.part()!!

        assertThat(part.name).isEqualTo("title")
        assertThat(part.filename).isNull()
        assertThat(part.type.text()).isEqualTo("text/plain")
        assertThat(part.content.inputStream().readAllBytes()).isEqualTo("groceries".toByteArray())
        assertThat(reader.part()).isNull()
    }

    @Test
    fun readsTwoParts() {
        val body = "--abc\r\n" +
            "Content-Disposition: form-data; name=\"title\"\r\n" +
            "\r\n" +
            "groceries\r\n" +
            "--abc\r\n" +
            "Content-Disposition: form-data; name=\"attachment\"; filename=\"notes.txt\"\r\n" +
            "Content-Type: text/plain\r\n" +
            "\r\n" +
            "hello\r\n" +
            "--abc--"
        val reader = PartReader("abc", ByteArrayInputStream(body.toByteArray()))

        val first = reader.part()!!
        val second = reader.part()!!

        assertThat(first.name).isEqualTo("title")
        assertThat(first.content.inputStream().readAllBytes()).isEqualTo("groceries".toByteArray())
        assertThat(second.name).isEqualTo("attachment")
        assertThat(second.filename).isEqualTo("notes.txt")
        assertThat(second.content.inputStream().readAllBytes()).isEqualTo("hello".toByteArray())
        assertThat(reader.part()).isNull()
    }
}
