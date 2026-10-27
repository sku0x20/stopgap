package dev.sku20.stopgap.helidon.mapper.req.multipart

import dev.sku20.stopgap.helidon.mapper.req.exception.EmptyBodyException
import dev.sku20.stopgap.helidon.mapper.req.exception.MalformedBodyException
import dev.sku20.stopgap.helidon.mapper.req.exception.MissingBoundaryException
import dev.sku20.stopgap.helidon.mapper.req.exception.UnsupportedMediaTypeException
import io.helidon.http.HttpMediaType
import io.helidon.http.ServerRequestHeaders
import io.helidon.http.media.ReadableEntity
import io.helidon.webserver.http.ServerRequest
import io.helidon.webserver.http.ServerResponse
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.*
import kotlin.reflect.typeOf

class MultipartReqMapperTest {

    private val mapper = MultipartReqMapper()
    private val request = mock<ServerRequest>()
    private val response = mock<ServerResponse>()
    private val headers = mock<ServerRequestHeaders>()
    private val textPlain = HttpMediaType.create("text/plain")

    @BeforeEach
    fun setup() {
        whenever(request.headers()).thenReturn(headers)
    }

    @Test
    fun rejectsMissingContentType() {
        whenever(headers.contentType()).thenReturn(Optional.empty())

        assertThatThrownBy { mapper.map<Any>(request, response, typeOf<Any>()) }
            .isInstanceOf(UnsupportedMediaTypeException::class.java)
    }

    @Test
    fun rejectsOtherMediaType() {
        contentType("application/json")

        assertThatThrownBy { mapper.map<Any>(request, response, typeOf<Any>()) }
            .isInstanceOf(UnsupportedMediaTypeException::class.java)
    }

    @Test
    fun rejectsOtherMultipartSubtype() {
        contentType("multipart/mixed; boundary=abc")

        assertThatThrownBy { mapper.map<Any>(request, response, typeOf<Any>()) }
            .isInstanceOf(UnsupportedMediaTypeException::class.java)
    }

    @Test
    fun rejectsWildcards() {
        contentType("multipart/*; boundary=abc")

        assertThatThrownBy { mapper.map<Any>(request, response, typeOf<Any>()) }
            .isInstanceOf(UnsupportedMediaTypeException::class.java)
    }

    @Test
    fun rejectsMissingBoundary() {
        contentType("multipart/form-data")

        assertThatThrownBy { mapper.map<Any>(request, response, typeOf<Any>()) }
            .isInstanceOf(MissingBoundaryException::class.java)
    }

    @Test
    fun rejectsEmptyBoundary() {
        contentType("multipart/form-data; boundary=\"\"")

        assertThatThrownBy { mapper.map<Any>(request, response, typeOf<Any>()) }
            .isInstanceOf(MissingBoundaryException::class.java)
    }

    @Test
    fun skipsBodyWhenRejected() {
        contentType("application/json")

        assertThatThrownBy { mapper.map<Any>(request, response, typeOf<Any>()) }

        verify(request, never()).content()
    }

    @Test
    fun mapsPart() {
        contentType("multipart/form-data; boundary=abc")
        body(
            "--abc\r\n" +
                "Content-Disposition: form-data; name=\"attachment\"; filename=\"notes.txt\"\r\n" +
                "Content-Type: text/plain\r\n" +
                "\r\n" +
                "hello\r\n" +
                "--abc--\r\n"
        )

        val multiPart = mapper.map<MultiPart>(request, response, typeOf<MultiPart>())

        assertThat(multiPart).isEqualTo(
            MultiPart(listOf(Part("attachment", "notes.txt", HttpMediaType.create("text/plain"))))
        )
    }

    @Test
    fun mapsFieldAndFile() {
        contentType("multipart/form-data; boundary=abc")
        body(
            "--abc\r\n" +
                "Content-Disposition: form-data; name=\"title\"\r\n" +
                "\r\n" +
                "groceries\r\n" +
                "--abc\r\n" +
                "Content-Disposition: form-data; name=\"attachment\"; filename=\"photo.png\"\r\n" +
                "Content-Type: image/png\r\n" +
                "\r\n" +
                "png\r\n" +
                "--abc--\r\n"
        )

        assertThat(map()).isEqualTo(
            MultiPart(
                listOf(
                    Part("title", null, textPlain),
                    Part("attachment", "photo.png", HttpMediaType.create("image/png")),
                )
            )
        )
    }

    @Test
    fun mapsRepeatedName() {
        contentType("multipart/form-data; boundary=abc")
        body(
            "--abc\r\n" +
                "Content-Disposition: form-data; name=\"files\"; filename=\"a.txt\"\r\n" +
                "\r\n" +
                "a\r\n" +
                "--abc\r\n" +
                "Content-Disposition: form-data; name=\"files\"; filename=\"b.txt\"\r\n" +
                "\r\n" +
                "b\r\n" +
                "--abc--\r\n"
        )

        assertThat(map()).isEqualTo(
            MultiPart(listOf(Part("files", "a.txt", textPlain), Part("files", "b.txt", textPlain)))
        )
    }

    @Test
    fun defaultsContentTypeToTextPlain() {
        contentType("multipart/form-data; boundary=abc")
        body(
            "--abc\r\n" +
                "Content-Disposition: form-data; name=\"title\"\r\n" +
                "\r\n" +
                "groceries\r\n" +
                "--abc--\r\n"
        )

        assertThat(map()).isEqualTo(MultiPart(listOf(Part("title", null, textPlain))))
    }

    @Test
    fun mapsEmptyFileName() {
        contentType("multipart/form-data; boundary=abc")
        body(
            "--abc\r\n" +
                "Content-Disposition: form-data; name=\"attachment\"; filename=\"\"\r\n" +
                "Content-Type: application/octet-stream\r\n" +
                "\r\n" +
                "\r\n" +
                "--abc--\r\n"
        )

        assertThat(map()).isEqualTo(
            MultiPart(listOf(Part("attachment", "", HttpMediaType.create("application/octet-stream"))))
        )
    }

    @Test
    fun mapsNonAsciiFileName() {
        contentType("multipart/form-data; boundary=abc")
        body(
            "--abc\r\n" +
                "Content-Disposition: form-data; name=\"attachment\"; filename=\"nöte.txt\"\r\n" +
                "\r\n" +
                "hello\r\n" +
                "--abc--\r\n"
        )

        assertThat(map()).isEqualTo(MultiPart(listOf(Part("attachment", "nöte.txt", textPlain))))
    }

    @Test
    fun ignoresHeaderNameCase() {
        contentType("multipart/form-data; boundary=abc")
        body(
            "--abc\r\n" +
                "content-disposition: form-data; name=\"title\"\r\n" +
                "CONTENT-TYPE: text/csv\r\n" +
                "\r\n" +
                "a,b\r\n" +
                "--abc--\r\n"
        )

        assertThat(map()).isEqualTo(MultiPart(listOf(Part("title", null, HttpMediaType.create("text/csv")))))
    }

    @Test
    fun mapsQuotedBoundary() {
        contentType("multipart/form-data; boundary=\"abc\"")
        body(
            "--abc\r\n" +
                "Content-Disposition: form-data; name=\"title\"\r\n" +
                "\r\n" +
                "groceries\r\n" +
                "--abc--\r\n"
        )

        assertThat(map()).isEqualTo(MultiPart(listOf(Part("title", null, textPlain))))
    }

    @Test
    fun skipsPreamble() {
        contentType("multipart/form-data; boundary=abc")
        body(
            "ignored preamble\r\n" +
                "--abc\r\n" +
                "Content-Disposition: form-data; name=\"title\"\r\n" +
                "\r\n" +
                "groceries\r\n" +
                "--abc--\r\n"
        )

        assertThat(map()).isEqualTo(MultiPart(listOf(Part("title", null, textPlain))))
    }

    @Test
    fun skipsEpilogue() {
        contentType("multipart/form-data; boundary=abc")
        body(
            "--abc\r\n" +
                "Content-Disposition: form-data; name=\"title\"\r\n" +
                "\r\n" +
                "groceries\r\n" +
                "--abc--\r\n" +
                "ignored epilogue"
        )

        assertThat(map()).isEqualTo(MultiPart(listOf(Part("title", null, textPlain))))
    }

    @Test
    fun allowsTransportPadding() {
        contentType("multipart/form-data; boundary=abc")
        body(
            "--abc \t\r\n" +
                "Content-Disposition: form-data; name=\"title\"\r\n" +
                "\r\n" +
                "groceries\r\n" +
                "--abc--\r\n"
        )

        assertThat(map()).isEqualTo(MultiPart(listOf(Part("title", null, textPlain))))
    }

    @Test
    fun allowsCrlfInContent() {
        contentType("multipart/form-data; boundary=abc")
        body(
            "--abc\r\n" +
                "Content-Disposition: form-data; name=\"notes\"\r\n" +
                "\r\n" +
                "line one\r\nline two\r\n" +
                "--abc--\r\n"
        )

        assertThat(map()).isEqualTo(MultiPart(listOf(Part("notes", null, textPlain))))
    }

    @Test
    fun allowsBoundaryPrefixInContent() {
        contentType("multipart/form-data; boundary=abc")
        body(
            "--abc\r\n" +
                "Content-Disposition: form-data; name=\"notes\"\r\n" +
                "\r\n" +
                "x\r\n--abcd\r\ny\r\n" +
                "--abc--\r\n"
        )

        assertThat(map()).isEqualTo(MultiPart(listOf(Part("notes", null, textPlain))))
    }

    @Test
    fun rejectsEmptyBody() {
        contentType("multipart/form-data; boundary=abc")
        body("")

        assertThatThrownBy { map() }.isInstanceOf(EmptyBodyException::class.java)
    }

    @Test
    fun rejectsMissingDelimiter() {
        contentType("multipart/form-data; boundary=abc")
        body("no delimiter here")

        assertThatThrownBy { map() }.isInstanceOf(MalformedBodyException::class.java)
    }

    @Test
    fun rejectsMissingCloseDelimiter() {
        contentType("multipart/form-data; boundary=abc")
        body(
            "--abc\r\n" +
                "Content-Disposition: form-data; name=\"title\"\r\n" +
                "\r\n" +
                "groceries"
        )

        assertThatThrownBy { map() }.isInstanceOf(MalformedBodyException::class.java)
    }

    @Test
    fun rejectsPartWithoutHeaders() {
        contentType("multipart/form-data; boundary=abc")
        body(
            "--abc\r\n" +
                "\r\n" +
                "groceries\r\n" +
                "--abc--\r\n"
        )

        assertThatThrownBy { map() }.isInstanceOf(MalformedBodyException::class.java)
    }

    @Test
    fun rejectsMissingContentDisposition() {
        contentType("multipart/form-data; boundary=abc")
        body(
            "--abc\r\n" +
                "Content-Type: text/plain\r\n" +
                "\r\n" +
                "groceries\r\n" +
                "--abc--\r\n"
        )

        assertThatThrownBy { map() }.isInstanceOf(MalformedBodyException::class.java)
    }

    @Test
    fun rejectsMissingName() {
        contentType("multipart/form-data; boundary=abc")
        body(
            "--abc\r\n" +
                "Content-Disposition: form-data; filename=\"notes.txt\"\r\n" +
                "\r\n" +
                "hello\r\n" +
                "--abc--\r\n"
        )

        assertThatThrownBy { map() }.isInstanceOf(MalformedBodyException::class.java)
    }

    @Test
    fun rejectsHeaderWithoutColon() {
        contentType("multipart/form-data; boundary=abc")
        body(
            "--abc\r\n" +
                "Content-Disposition form-data; name=\"title\"\r\n" +
                "\r\n" +
                "groceries\r\n" +
                "--abc--\r\n"
        )

        assertThatThrownBy { map() }.isInstanceOf(MalformedBodyException::class.java)
    }

    @Test
    fun rejectsInvalidPartContentType() {
        contentType("multipart/form-data; boundary=abc")
        body(
            "--abc\r\n" +
                "Content-Disposition: form-data; name=\"title\"\r\n" +
                "Content-Type: not a media type\r\n" +
                "\r\n" +
                "groceries\r\n" +
                "--abc--\r\n"
        )

        assertThatThrownBy { map() }.isInstanceOf(MalformedBodyException::class.java)
    }

    @Test
    fun mapsBodyReadOneByteAtATime() {
        contentType("multipart/form-data; boundary=abc")
        val bytes = (
            "--abc\r\n" +
                "Content-Disposition: form-data; name=\"title\"\r\n" +
                "\r\n" +
                "x\r\n--abcd\r\n" +
                "--abc\r\n" +
                "Content-Disposition: form-data; name=\"attachment\"; filename=\"notes.txt\"\r\n" +
                "\r\n" +
                "hello\r\n" +
                "--abc--\r\n"
            ).toByteArray()
        body(OneByteInputStream(bytes))

        assertThat(map()).isEqualTo(
            MultiPart(listOf(Part("title", null, textPlain), Part("attachment", "notes.txt", textPlain)))
        )
    }

    @Test
    fun mapsPartLargerThanBuffer() {
        contentType("multipart/form-data; boundary=abc")
        body(
            "--abc\r\n" +
                "Content-Disposition: form-data; name=\"attachment\"; filename=\"big.txt\"\r\n" +
                "\r\n" +
                "a".repeat(20_000) + "\r\n" +
                "--abc\r\n" +
                "Content-Disposition: form-data; name=\"title\"\r\n" +
                "\r\n" +
                "groceries\r\n" +
                "--abc--\r\n"
        )

        assertThat(map()).isEqualTo(
            MultiPart(listOf(Part("attachment", "big.txt", textPlain), Part("title", null, textPlain)))
        )
    }

    private fun map(): MultiPart = mapper.map(request, response, typeOf<MultiPart>())

    // Hands out one byte per read, so a delimiter always straddles reads.
    private class OneByteInputStream(bytes: ByteArray) : InputStream() {
        private val delegate = ByteArrayInputStream(bytes)

        override fun read(): Int = delegate.read()

        override fun read(b: ByteArray, off: Int, len: Int): Int = delegate.read(b, off, minOf(len, 1))
    }

    private fun body(value: String) {
        body(ByteArrayInputStream(value.toByteArray()))
    }

    private fun body(stream: InputStream) {
        val content = mock<ReadableEntity>()
        whenever(request.content()).thenReturn(content)
        whenever(content.inputStream()).thenReturn(stream)
    }

    private fun contentType(value: String) {
        whenever(headers.contentType()).thenReturn(Optional.of(HttpMediaType.create(value)))
    }
}
