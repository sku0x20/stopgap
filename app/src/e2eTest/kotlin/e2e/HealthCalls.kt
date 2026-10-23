package e2e

import dev.sku20.stopgap.helidon.test.InjectInstance
import io.helidon.http.Status
import io.helidon.webclient.api.WebClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class HealthCalls {

    @InjectInstance
    lateinit var client: WebClient

    @Test
    fun live() {
        val response = client.get("/observe/health/live").request()
        assertThat(response.status()).isEqualTo(Status.NO_CONTENT_204)
    }

    @Test
    fun ready() {
        val response = client.get("/observe/health/ready").request()
        assertThat(response.status()).isEqualTo(Status.NO_CONTENT_204)
    }

}
