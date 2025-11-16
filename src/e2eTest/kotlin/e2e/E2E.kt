package e2e

import extension.InjectInstance
import io.helidon.http.Status
import io.helidon.webclient.api.WebClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class E2E {

    @InjectInstance
    lateinit var client: WebClient

    @Test
    fun ping() {
        val response = client.get("/ping").request()
        assertThat(response.status()).isEqualTo(Status.OK_200)
        assertThat(response.`as`(String::class.java)).isEqualTo("pong")
    }

    @Test
    fun exception() {
        val response = client.get("/exception/bad-client-1").request()
        assertThat(response.status()).isEqualTo(Status.BAD_REQUEST_400)

        val response2 = client.get("/exception/bad-client-2").request()
        assertThat(response2.status()).isEqualTo(Status.BAD_REQUEST_400)
    }

    @Test
    fun generator() {
        val response = client.get("/generate/uuid").request()
        assertThat(response.status()).isEqualTo(Status.OK_200)
        assertThat(response.`as`(String::class.java))
            .matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")

        val response2 = client.get("/generate/number").request()
        assertThat(response2.status()).isEqualTo(Status.OK_200)
        assertThat(response2.`as`(String::class.java)).matches("\\d+")

        val response3 = client.get("/generate/static").request()
        assertThat(response3.status()).isEqualTo(Status.OK_200)
        assertThat(response3.`as`(String::class.java)).isEqualTo("from static generator")
    }

    @Test
    fun notFound() {
        val response = client.get("/not-found").request()
        assertThat(response.status()).isEqualTo(Status.NOT_FOUND_404)
    }

}