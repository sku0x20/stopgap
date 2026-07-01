package dev.sku20.stopgap.app.exception

import dev.sku20.stopgap.helidon.test.extension.InjectInstance
import dev.sku20.stopgap.helidon.test.integration.SetupCapture
import dev.sku20.stopgap.helidon.test.integration.WebserverTest
import io.helidon.http.Status
import io.helidon.webclient.api.WebClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


@WebserverTest
class ExceptionEndpointTest {

    @InjectInstance
    lateinit var client: WebClient

    @Test
    fun badClient1() {
        val response = client.get("/exception/bad-client-1").request()
        assertThat(response.status()).isEqualTo(Status.BAD_REQUEST_400)
    }

    @Test
    fun badClient2() {
        val response = client.get("/exception/bad-client-2").request()
        assertThat(response.status()).isEqualTo(Status.BAD_REQUEST_400)
    }

    companion object {

        @JvmStatic
        @WebserverTest.Setup
        fun createEndpoint(): SetupCapture {
            return SetupCapture(ExceptionEndpoint())
        }

    }

}