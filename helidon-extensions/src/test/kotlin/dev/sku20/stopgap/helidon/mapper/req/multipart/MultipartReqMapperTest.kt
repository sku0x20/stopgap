package dev.sku20.stopgap.helidon.mapper.req.multipart

import dev.sku20.stopgap.helidon.mapper.req.exception.MissingBoundaryException
import dev.sku20.stopgap.helidon.mapper.req.exception.UnsupportedMediaTypeException
import io.helidon.http.HttpMediaType
import io.helidon.http.ServerRequestHeaders
import io.helidon.http.media.ReadableEntity
import io.helidon.webserver.http.ServerRequest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.ByteArrayInputStream
import java.util.*
import kotlin.reflect.typeOf

class MultipartReqMapperTest {

    private val mapper = MultipartReqMapper()
    private val request = mock<ServerRequest>()
    private val headers = mock<ServerRequestHeaders>()

    @BeforeEach
    fun setup() {
        whenever(request.headers()).thenReturn(headers)
    }

    @Test
    fun rejectsMissingContentType() {
        whenever(headers.contentType()).thenReturn(Optional.empty())

        assertThatThrownBy { mapper.map<Any>(request, typeOf<Any>()) }
            .isInstanceOf(UnsupportedMediaTypeException::class.java)
    }

    @Test
    fun rejectsOtherMediaType() {
        contentType("application/json")

        assertThatThrownBy { mapper.map<Any>(request, typeOf<Any>()) }
            .isInstanceOf(UnsupportedMediaTypeException::class.java)
    }

    @Test
    fun rejectsOtherMultipartSubtype() {
        contentType("multipart/mixed; boundary=abc")

        assertThatThrownBy { mapper.map<Any>(request, typeOf<Any>()) }
            .isInstanceOf(UnsupportedMediaTypeException::class.java)
    }

    @Test
    fun rejectsWildcards() {
        contentType("multipart/*; boundary=abc")

        assertThatThrownBy { mapper.map<Any>(request, typeOf<Any>()) }
            .isInstanceOf(UnsupportedMediaTypeException::class.java)
    }

    @Test
    fun rejectsMissingBoundary() {
        contentType("multipart/form-data")

        assertThatThrownBy { mapper.map<Any>(request, typeOf<Any>()) }
            .isInstanceOf(MissingBoundaryException::class.java)
    }

    @Test
    fun rejectsEmptyBoundary() {
        contentType("multipart/form-data; boundary=\"\"")

        assertThatThrownBy { mapper.map<Any>(request, typeOf<Any>()) }
            .isInstanceOf(MissingBoundaryException::class.java)
    }

    @Test
    fun skipsBodyWhenRejected() {
        contentType("application/json")

        assertThatThrownBy { mapper.map<Any>(request, typeOf<Any>()) }

        verify(request, never()).content()
    }

    @Test
    fun mapsFile() {
        contentType("multipart/form-data; boundary=abc")
        body(
            "--abc\r\n" +
                "Content-Disposition: form-data; name=\"attachment\"; filename=\"notes.txt\"\r\n" +
                "Content-Type: text/plain\r\n" +
                "\r\n" +
                "hello\r\n" +
                "--abc--\r\n"
        )

        val file = mapper.map<UploadedFile>(request, typeOf<UploadedFile>())

        assertThat(file).isEqualTo(UploadedFile("attachment", "notes.txt", HttpMediaType.create("text/plain"), 5))
    }

    private fun body(value: String) {
        val content = mock<ReadableEntity>()
        whenever(request.content()).thenReturn(content)
        whenever(content.inputStream()).thenReturn(ByteArrayInputStream(value.toByteArray()))
    }

    private fun contentType(value: String) {
        whenever(headers.contentType()).thenReturn(Optional.of(HttpMediaType.create(value)))
    }
}
