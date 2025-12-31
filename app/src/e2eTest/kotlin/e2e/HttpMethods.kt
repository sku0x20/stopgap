package e2e

import extension.InjectInstance
import io.helidon.http.Status
import io.helidon.webclient.api.WebClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class HttpMethods {

    @InjectInstance
    lateinit var client: WebClient

    @Test
    fun get() {
        val response = client.get("/methods/get").request()
        assertThat(response.status()).isEqualTo(Status.OK_200)
        assertThat(response.`as`(String::class.java)).isEqualTo("GET request received")
    }

    @Test
    fun post() {
        val response = client.post("/methods/post").request()
        assertThat(response.status()).isEqualTo(Status.OK_200)
        assertThat(response.`as`(String::class.java)).isEqualTo("POST request received")
    }

}