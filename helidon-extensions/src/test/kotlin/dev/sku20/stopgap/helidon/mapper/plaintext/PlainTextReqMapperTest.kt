package dev.sku20.stopgap.helidon.mapper.plaintext

import dev.sku20.stopgap.helidon.mapper.exception.EmptyBodyException
import dev.sku20.stopgap.helidon.mapper.exception.UnsupportedMediaTypeException
import io.helidon.http.HttpMediaType
import io.helidon.http.ServerRequestHeaders
import io.helidon.http.media.ReadableEntity
import io.helidon.webserver.http.ServerRequest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.ByteArrayInputStream
import java.util.Optional
import kotlin.reflect.typeOf

class PlainTextReqMapperTest {

    private val mapper = PlainTextReqMapper()
    private val request = mock<ServerRequest>()
    private val headers = mock<ServerRequestHeaders>()
    private val content = mock<ReadableEntity>()

    @BeforeEach
    fun setup() {
        whenever(request.headers()).thenReturn(headers)
        whenever(request.content()).thenReturn(content)
    }

    @Test
    fun decodesUtf8Body() {
        givenRequest("text/plain; charset=utf-8", "héllo".toByteArray(Charsets.UTF_8))

        assertThat(mapper.map<String>(request, typeOf<String>())).isEqualTo("héllo")
    }

    @Test
    fun decodesAsUtf8WithoutCharset() {
        givenRequest("text/plain", "héllo".toByteArray(Charsets.UTF_8))

        assertThat(mapper.map<String>(request, typeOf<String>())).isEqualTo("héllo")
    }

    @Test
    fun replacesMalformedBytes() {
        givenRequest("text/plain", "héllo".toByteArray(Charsets.ISO_8859_1))

        assertThat(mapper.map<String>(request, typeOf<String>())).isEqualTo("h�llo")
    }

    @Test
    fun throwsOnEmptyBody() {
        givenRequest("text/plain", ByteArray(0))

        assertThatThrownBy { mapper.map<String>(request, typeOf<String>()) }
            .isInstanceOf(EmptyBodyException::class.java)
    }

    @Test
    fun throwsWhenMediaTypeRejected() {
        givenRequest("text/plain; charset=ISO-8859-1", "hello".toByteArray())

        assertThatThrownBy { mapper.map<String>(request, typeOf<String>()) }
            .isInstanceOf(UnsupportedMediaTypeException::class.java)
    }

    @Test
    fun throwsWhenContentTypeMissing() {
        whenever(headers.contentType()).thenReturn(Optional.empty())
        whenever(content.inputStream()).thenReturn(ByteArrayInputStream("hello".toByteArray()))

        assertThatThrownBy { mapper.map<String>(request, typeOf<String>()) }
            .isInstanceOf(UnsupportedMediaTypeException::class.java)
    }

    private fun givenRequest(contentType: String, body: ByteArray) {
        whenever(headers.contentType()).thenReturn(Optional.of(HttpMediaType.create(contentType)))
        whenever(content.inputStream()).thenReturn(ByteArrayInputStream(body))
    }
}
