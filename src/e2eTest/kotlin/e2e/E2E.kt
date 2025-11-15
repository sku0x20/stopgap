package e2e

import io.helidon.http.Status
import io.helidon.webclient.api.WebClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class E2E {

    @Test
    fun ping() {
        val client = WebClient.builder()
            .baseUri("http://localhost:8080")
            .build()

        val response = client.get("/ping").request()
        assertThat(response.status()).isEqualTo(Status.OK_200)
        assertThat(response.`as`(String::class.java)).isEqualTo("pong")

        client.closeResource()
    }

}