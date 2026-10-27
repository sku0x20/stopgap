package dev.sku20.stopgap.helidon.mapper.req.multipart

import dev.sku20.stopgap.helidon.mapper.req.exception.UnsupportedMediaTypeException
import io.helidon.http.HttpMediaType
import io.helidon.http.ServerRequestHeaders
import io.helidon.http.media.ReadableEntity
import io.helidon.webserver.http.ServerRequest
import io.helidon.webserver.http.ServerResponse
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.ByteArrayInputStream
import java.util.*
import kotlin.reflect.typeOf

class MultipartReqMapperTest {

    private val mapper = MultipartReqMapper()
    private val request = mock<ServerRequest>()
    private val response = mock<ServerResponse>()
    private val headers = mock<ServerRequestHeaders>()

    @Test
    fun rejectsUnsupportedMediaType() {
        whenever(request.headers()).thenReturn(headers)

        whenever(headers.contentType()).thenReturn(Optional.empty())
        assertThatThrownBy { mapper.map<Any>(request, response, typeOf<Any>()) }
            .isInstanceOf(UnsupportedMediaTypeException::class.java)

        whenever(headers.contentType()).thenReturn(Optional.of(HttpMediaType.create("application/json")))
        assertThatThrownBy { mapper.map<Any>(request, response, typeOf<Any>()) }
            .isInstanceOf(UnsupportedMediaTypeException::class.java)
    }

    @Test
    fun mapsValidRequest() {
        val content = mock<ReadableEntity>()
        whenever(request.headers()).thenReturn(headers)
        whenever(headers.contentType()).thenReturn(Optional.of(HttpMediaType.create("multipart/form-data; boundary=abc")))
        whenever(request.content()).thenReturn(content)
        whenever(content.inputStream()).thenReturn(ByteArrayInputStream("--abc--".toByteArray()))

        val multiPart = mapper.map<MultiPart>(request, response, typeOf<MultiPart>())

        assertThat(multiPart.parts).isEmpty()
    }
}
