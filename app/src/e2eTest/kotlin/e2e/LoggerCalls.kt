package e2e

import dev.sku20.stopgap.helidon.test.InjectInstance
import io.helidon.http.Status
import io.helidon.webclient.api.WebClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class LoggerCalls {

    @InjectInstance
    lateinit var client: WebClient

    @Test
    fun log() {
        val response = client.post("/logger/info").queryParam("message", "hello").request()
        assertThat(response.status()).isEqualTo(Status.OK_200)
    }

    @Test
    fun invalidLevel() {
        val response = client.post("/logger/loud").queryParam("message", "hello").request()
        assertThat(response.status()).isEqualTo(Status.BAD_REQUEST_400)
    }

}
