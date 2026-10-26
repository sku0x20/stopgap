package dev.sku20.stopgap.helidon.mapper.req.bodyreqmapper

import dev.sku20.stopgap.helidon.mapper.req.bodyreqmapper.mediatyperule.MediaTypeRule
import dev.sku20.stopgap.helidon.mapper.req.exception.EmptyBodyException
import dev.sku20.stopgap.helidon.mapper.req.exception.UnsupportedMediaTypeException
import io.helidon.http.HttpMediaTypes
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

class BodyReqMapperTest {

    private val rule = mock<MediaTypeRule>()
    private val decoder = mock<BodyDecoder>()
    private val mapper = BodyReqMapper(rule, decoder)
    private val request = mock<ServerRequest>()
    private val headers = mock<ServerRequestHeaders>()
    private val content = mock<ReadableEntity>()

    private class Note(val title: String)

    @BeforeEach
    fun setup() {
        whenever(request.headers()).thenReturn(headers)
        whenever(request.content()).thenReturn(content)
        whenever(headers.contentType()).thenReturn(Optional.of(HttpMediaTypes.JSON))
        whenever(rule.accepts(HttpMediaTypes.JSON)).thenReturn(true)
    }

    @Test
    fun decodesBody() {
        val bytes = """{"title":"groceries"}""".toByteArray()
        val type = typeOf<Note>()
        val note = Note("groceries")
        whenever(content.inputStream()).thenReturn(ByteArrayInputStream(bytes))
        whenever(decoder.decode<Note>(bytes, type)).thenReturn(note)

        val result = mapper.map<Note>(request, type)

        assertThat(result).isSameAs(note)
    }

    @Test
    fun throwsOnEmptyBody() {
        whenever(content.inputStream()).thenReturn(ByteArrayInputStream(ByteArray(0)))

        assertThatThrownBy { mapper.map<Note>(request, typeOf<Note>()) }
            .isInstanceOf(EmptyBodyException::class.java)
    }

    @Test
    fun throwsWhenMediaTypeRejected() {
        whenever(rule.accepts(HttpMediaTypes.JSON)).thenReturn(false)

        assertThatThrownBy { mapper.map<Note>(request, typeOf<Note>()) }
            .isInstanceOf(UnsupportedMediaTypeException::class.java)
    }

    @Test
    fun skipsBodyWhenMediaTypeRejected() {
        whenever(rule.accepts(HttpMediaTypes.JSON)).thenReturn(false)

        assertThatThrownBy { mapper.map<Note>(request, typeOf<Note>()) }

        verify(request, never()).content()
    }
}
