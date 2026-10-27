package dev.sku20.stopgap.helidon.mapper.res.bodyresmapper

import io.helidon.http.HttpMediaTypes
import io.helidon.http.ServerResponseHeaders
import io.helidon.webserver.http.ServerResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class BodyResMapperTest {

    private val encoder = mock<BodyEncoder>()
    private val mapper = BodyResMapper(HttpMediaTypes.JSON, encoder)
    private val response = mock<ServerResponse>()
    private val headers = mock<ServerResponseHeaders>()

    private data class Note(val title: String)

    @BeforeEach
    fun setup() {
        whenever(response.headers()).thenReturn(headers)
    }

    @Test
    fun sendsEncodedBody() {
        val note = Note("groceries")
        val bytes = """{"title":"groceries"}""".toByteArray()
        whenever(encoder.encode(note)).thenReturn(bytes)

        mapper.map(response, note)

        verify(response).send(bytes)
    }

    @Test
    fun setsContentType() {
        val note = Note("groceries")
        whenever(encoder.encode(note)).thenReturn(ByteArray(0))

        mapper.map(response, note)

        verify(headers).contentType(HttpMediaTypes.JSON)
    }
}
