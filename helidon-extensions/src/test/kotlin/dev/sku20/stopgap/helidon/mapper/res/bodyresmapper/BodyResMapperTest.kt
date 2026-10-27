package dev.sku20.stopgap.helidon.mapper.res.bodyresmapper

import io.helidon.http.HttpMediaTypes
import io.helidon.http.ServerResponseHeaders
import io.helidon.webserver.http.ServerRequest
import io.helidon.webserver.http.ServerResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class BodyResMapperTest {

    private val encoder = mock<BodyEncoder>()
    private val mapper = BodyResMapper(HttpMediaTypes.JSON, encoder)
    private val request = mock<ServerRequest>()
    private val response = mock<ServerResponse>()
    private val headers = mock<ServerResponseHeaders>()

    private data class Note(val title: String)

    @BeforeEach
    fun setup() {
        whenever(response.headers()).thenReturn(headers)
    }

    @Test
    fun sendsEncodedBody() {
        val bytes = """{"title":"groceries"}""".toByteArray()
        whenever(encoder.encode(Note("groceries"))).thenReturn(bytes)

        mapper.map(request, response, Note("groceries"))

        verify(response).send(bytes)
    }

    @Test
    fun setsContentType() {
        mapper.map(request, response, Note("groceries"))

        verify(headers).contentType(HttpMediaTypes.JSON)
    }
}
