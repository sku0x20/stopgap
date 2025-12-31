package e2e

import extension.InjectInstance
import io.helidon.http.Status
import io.helidon.webclient.api.WebClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PostCalls {

    @InjectInstance
    lateinit var client: WebClient

    @Test
    fun badClient() {
        val response = client.post("/exception/bad-post").request()
        assertThat(response.status()).isEqualTo(Status.BAD_REQUEST_400)
        assertThat(response.`as`(String::class.java)).isEqualTo("Bad Request")
    }

}