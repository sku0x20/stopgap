package e2e

import extension.InjectInstance
import extension.webserverclient.ClientExtension
import io.helidon.http.Status
import io.helidon.webclient.api.WebClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(ClientExtension::class)
class E2E {

    @InjectInstance
    lateinit var client: WebClient

    @Test
    fun ping() {
        val response = client.get("/ping").request()
        assertThat(response.status()).isEqualTo(Status.OK_200)
        assertThat(response.`as`(String::class.java)).isEqualTo("pong")
    }

}