package dev.sku20.stopgap.app.httpbin.web

import dev.sku20.stopgap.helidon.test.InjectInstance
import dev.sku20.stopgap.helidon.test.integration.SetupCapture
import dev.sku20.stopgap.helidon.test.integration.WebserverTest
import io.helidon.common.buffers.DataReader
import io.helidon.http.Status
import io.helidon.webclient.api.HttpClientRequest
import io.helidon.webclient.api.WebClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import java.io.UncheckedIOException
import java.net.ConnectException
import java.net.SocketTimeoutException

// Transport failures can't be triggered against a real httpbin, so the client is mocked with the exceptions Helidon's
// WebClient actually throws (see HttpbinEndpoint.send).
@WebserverTest
class HttpbinEndpointTest {

    @InjectInstance
    lateinit var client: WebClient

    @BeforeEach
    fun resetHttpbin() {
        reset(httpbinClient, httpbinRequest)
        whenever(httpbinClient.get(any<String>())).thenReturn(httpbinRequest)
    }

    @Test
    fun readTimeout() {
        whenever(httpbinRequest.request()).thenThrow(UncheckedIOException(SocketTimeoutException("Read timed out")))
        assertThat(client.get("/httpbin/delay/1").request().status()).isEqualTo(Status.GATEWAY_TIMEOUT_504)
    }

    @Test
    fun connectTimeout() {
        whenever(httpbinRequest.request()).thenThrow(UncheckedIOException(SocketTimeoutException("Connect timed out")))
        assertThat(client.get("/httpbin/delay/1").request().status()).isEqualTo(Status.GATEWAY_TIMEOUT_504)
    }

    @Test
    fun connectionRefused() {
        whenever(httpbinRequest.request()).thenThrow(UncheckedIOException(ConnectException("Connection refused")))
        assertThat(client.get("/httpbin/delay/1").request().status()).isEqualTo(Status.BAD_GATEWAY_502)
    }

    @Test
    fun unknownHost() {
        whenever(httpbinRequest.request()).thenThrow(IllegalArgumentException("Failed to get address for host httpbin"))
        assertThat(client.get("/httpbin/headers").request().status()).isEqualTo(Status.BAD_GATEWAY_502)
    }

    @Test
    fun connectionDropped() {
        whenever(httpbinRequest.request()).thenThrow(DataReader.InsufficientDataAvailableException())
        assertThat(client.get("/httpbin/status-mapped/200").request().status()).isEqualTo(Status.BAD_GATEWAY_502)
    }

    companion object {
        // Not an injected instance: the test's own WebClient is injected by the same type.
        private val httpbinClient = mock<WebClient>()
        private val httpbinRequest = mock<HttpClientRequest>()

        @JvmStatic
        @WebserverTest.Setup
        fun setup(): SetupCapture {
            return SetupCapture(HttpbinEndpoint(httpbinClient))
        }
    }
}
