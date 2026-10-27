package dev.sku20.stopgap.helidon.mapper.req.multipart.part

import dev.sku20.stopgap.helidon.mapper.req.exception.EmptyBodyException
import dev.sku20.stopgap.helidon.mapper.req.exception.MalformedBodyException
import dev.sku20.stopgap.helidon.mapper.req.multipart.part.partcontent.FilePartContent
import dev.sku20.stopgap.helidon.mapper.req.multipart.part.partcontent.InMemoryPartContent
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.nio.file.Files
import java.nio.file.Path

class PartReaderTest {

    @Test
    fun hasNoNextAtCloseDelimiter() {
        val reader = PartReader("abc", ByteArrayInputStream("--abc--".toByteArray()))

        assertThat(reader.hasNext()).isFalse()
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

        val part = reader.next()

        assertThat(part.name).isEqualTo("title")
        assertThat(part.filename).isNull()
        assertThat(part.type.text()).isEqualTo("text/plain")
        assertThat(part.content.inputStream().readAllBytes()).isEqualTo("groceries".toByteArray())
        assertThat(reader.hasNext()).isFalse()
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

        val first = reader.next()
        val second = reader.next()

        assertThat(first.name).isEqualTo("title")
        assertThat(first.content.inputStream().readAllBytes()).isEqualTo("groceries".toByteArray())
        assertThat(second.name).isEqualTo("attachment")
        assertThat(second.filename).isEqualTo("notes.txt")
        assertThat(second.content.inputStream().readAllBytes()).isEqualTo("hello".toByteArray())
        assertThat(reader.hasNext()).isFalse()
    }

    @Test
    fun switchesToFileOverBufferSize() {
        val body = "--abc\r\n" +
            "Content-Disposition: form-data; name=\"small\"\r\n" +
            "\r\n" +
            "hi\r\n" +
            "--abc\r\n" +
            "Content-Disposition: form-data; name=\"large\"\r\n" +
            "\r\n" +
            "hello\r\n" +
            "--abc--"
        val reader = PartReader("abc", ByteArrayInputStream(body.toByteArray()), 4)

        val small = reader.next()
        val large = reader.next()

        assertThat(small.content).isInstanceOf(InMemoryPartContent::class.java)
        assertThat(small.content.inputStream().readAllBytes()).isEqualTo("hi".toByteArray())
        assertThat(large.content).isInstanceOf(FilePartContent::class.java)
        assertThat(large.content.inputStream().readAllBytes()).isEqualTo("hello".toByteArray())
        large.close()
    }

    @Test
    fun ignoresHeaderNameCase() {
        val part = partWithHeaders("CONTENT-DISPOSITION: form-data; name=\"title\"\r\ncontent-TYPE: text/html\r\n")

        assertThat(part.name).isEqualTo("title")
        assertThat(part.type.text()).isEqualTo("text/html")
    }

    @Test
    fun defaultsContentTypeToTextPlain() {
        val part = partWithHeaders("Content-Disposition: form-data; name=\"title\"\r\n")

        assertThat(part.type.text()).isEqualTo("text/plain")
    }

    @Test
    fun readsNonAsciiFileName() {
        val part = partWithHeaders("Content-Disposition: form-data; name=\"cv\"; filename=\"résumé.pdf\"\r\n")

        assertThat(part.filename).isEqualTo("résumé.pdf")
    }

    @Test
    fun readsRepeatedName() {
        val body = "--abc\r\n" +
            "Content-Disposition: form-data; name=\"tag\"\r\n" +
            "\r\n" +
            "a\r\n" +
            "--abc\r\n" +
            "Content-Disposition: form-data; name=\"tag\"\r\n" +
            "\r\n" +
            "b\r\n" +
            "--abc--"
        val reader = PartReader("abc", ByteArrayInputStream(body.toByteArray()))

        val first = reader.next()
        val second = reader.next()

        assertThat(first.name).isEqualTo("tag")
        assertThat(first.content.inputStream().readAllBytes()).isEqualTo("a".toByteArray())
        assertThat(second.name).isEqualTo("tag")
        assertThat(second.content.inputStream().readAllBytes()).isEqualTo("b".toByteArray())
    }

    @Test
    fun rejectsMalformedHeaders() {
        assertThatThrownBy { partWithHeaders("Content-Disposition form-data; name=\"title\"\r\n") }
            .isInstanceOf(MalformedBodyException::class.java)
        assertThatThrownBy { partWithHeaders("Content-Type: text/plain\r\n") }
            .isInstanceOf(MalformedBodyException::class.java)
        assertThatThrownBy { partWithHeaders("Content-Disposition: form-data\r\n") }
            .isInstanceOf(MalformedBodyException::class.java)
        assertThatThrownBy {
            partWithHeaders("Content-Disposition: form-data; name=\"title\"\r\nContent-Type: text\r\n")
        }
            .isInstanceOf(MalformedBodyException::class.java)
    }

    @Test
    fun skipsPreamble() {
        val body = "This is the preamble.\r\n" +
            "--abc\r\n" +
            "Content-Disposition: form-data; name=\"title\"\r\n" +
            "\r\n" +
            "groceries\r\n" +
            "--abc--"
        val reader = PartReader("abc", ByteArrayInputStream(body.toByteArray()))

        val part = reader.next()

        assertThat(part.name).isEqualTo("title")
        assertThat(part.content.inputStream().readAllBytes()).isEqualTo("groceries".toByteArray())
        assertThat(reader.hasNext()).isFalse()
    }

    @Test
    fun allowsTransportPadding() {
        val body = "--abc \t \r\n" +
            "Content-Disposition: form-data; name=\"title\"\r\n" +
            "\r\n" +
            "groceries\r\n" +
            "--abc--"
        val reader = PartReader("abc", ByteArrayInputStream(body.toByteArray()))

        val part = reader.next()

        assertThat(part.name).isEqualTo("title")
        assertThat(part.content.inputStream().readAllBytes()).isEqualTo("groceries".toByteArray())
        assertThat(reader.hasNext()).isFalse()
    }

    // RFC 2046 5.1.1: a line that starts with the boundary is a boundary line, so "--abcd" can't be content.
    @Test
    fun rejectsBoundaryPrefixInContent() {
        val body = "--abc\r\n" +
            "Content-Disposition: form-data; name=\"title\"\r\n" +
            "\r\n" +
            "groceries\r\n" +
            "--abcd\r\n" +
            "--abc--"
        val reader = PartReader("abc", ByteArrayInputStream(body.toByteArray()))

        assertThatThrownBy { reader.next() }
            .isInstanceOf(MalformedBodyException::class.java)
    }

    @Test
    fun rejectsEmptyBody() {
        assertThatThrownBy { PartReader("abc", ByteArrayInputStream(ByteArray(0))) }
            .isInstanceOf(EmptyBodyException::class.java)
    }

    @Test
    fun rejectsBufferSizeOverInMemoryLimit() {
        assertThatThrownBy { PartReader("abc", ByteArrayInputStream(ByteArray(0)), InMemoryPartContent.MAX_SIZE + 1) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun deletesFileWhenUploadIsTruncated() {
        val body = "--abc\r\n" +
            "Content-Disposition: form-data; name=\"attachment\"; filename=\"big.txt\"\r\n" +
            "\r\n" +
            "a".repeat(20)
        val filesBefore = tempFiles()
        val reader = PartReader("abc", ByteArrayInputStream(body.toByteArray()), 4)

        assertThatThrownBy { reader.next() }
            .isInstanceOf(MalformedBodyException::class.java)
        assertThat(tempFiles()).isEqualTo(filesBefore)
    }

    @Test
    fun deletesFileWhenBoundaryLineIsMalformed() {
        val body = "--abc\r\n" +
            "Content-Disposition: form-data; name=\"attachment\"; filename=\"notes.txt\"\r\n" +
            "\r\n" +
            "hello\r\n" +
            "--abcd\r\n" +
            "--abc--"
        val filesBefore = tempFiles()
        val reader = PartReader("abc", ByteArrayInputStream(body.toByteArray()), 4)

        assertThatThrownBy { reader.next() }
            .isInstanceOf(MalformedBodyException::class.java)
        assertThat(tempFiles()).isEqualTo(filesBefore)
    }

    // Reads a single part with the given header lines, each ending in CRLF.
    private fun partWithHeaders(headers: String): Part {
        val body = "--abc\r\n" + headers + "\r\n" + "content\r\n" + "--abc--"
        return PartReader("abc", ByteArrayInputStream(body.toByteArray())).next()
    }

    private fun tempFiles(): Set<Path> {
        val tempDir = Path.of(System.getProperty("java.io.tmpdir"))
        Files.newDirectoryStream(tempDir, "stopgap-multipart-*").use { return it.toSet() }
    }
}
