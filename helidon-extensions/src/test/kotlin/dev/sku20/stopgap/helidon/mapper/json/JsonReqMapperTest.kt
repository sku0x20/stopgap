package dev.sku20.stopgap.helidon.mapper.json

import dev.sku20.stopgap.helidon.mapper.ContentTypeRule
import dev.sku20.stopgap.helidon.mapper.EmptyBodyException
import io.helidon.http.media.ReadableEntity
import io.helidon.webserver.http.ServerRequest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.ByteArrayInputStream
import kotlin.reflect.typeOf

class JsonReqMapperTest {

    private val decoder = mock<JsonDecoder>()
    private val contentTypeRule = mock<ContentTypeRule>()
    private val mapper = JsonReqMapper(decoder, contentTypeRule)
    private val request = mock<ServerRequest>()
    private val content = mock<ReadableEntity>()

    private class Note(val title: String)

    @Test
    fun decodesBody() {
        val bytes = """{"title":"groceries"}""".toByteArray()
        val type = typeOf<Note>()
        val note = Note("groceries")
        whenever(request.content()).thenReturn(content)
        whenever(content.inputStream()).thenReturn(ByteArrayInputStream(bytes))
        whenever(decoder.decode<Note>(bytes, type)).thenReturn(note)

        val result = mapper.map<Note>(request, type)

        assertThat(result).isSameAs(note)
    }

    @Test
    fun throwsOnEmptyBody() {
        whenever(request.content()).thenReturn(content)
        whenever(content.inputStream()).thenReturn(ByteArrayInputStream(ByteArray(0)))

        assertThatThrownBy { mapper.map<Note>(request, typeOf<Note>()) }
            .isInstanceOf(EmptyBodyException::class.java)
    }
}
