package e2e

import dev.sku20.stopgap.helidon.test.InjectInstance
import io.helidon.http.HeaderNames
import io.helidon.http.Status
import io.helidon.webclient.api.WebClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AuthCalls {

    @InjectInstance
    lateinit var client: WebClient

    @Test
    fun publicEndpointReturnsOk() {
        val response = client.get("/secure/closed").request()
        assertThat(response.status()).isEqualTo(Status.OK_200)
        assertThat(response.`as`(String::class.java)).isEqualTo("secret")
    }

    @Test
    fun userEndpointReturnsForbiddenWithoutAuth() {
        val response = client.get("/secure/user").request()
        assertThat(response.status()).isEqualTo(Status.FORBIDDEN_403)
    }

    @Test
    fun userEndpointReturnsOkWithBearerToken() {
        val response = client.get("/secure/user")
            .header(HeaderNames.AUTHORIZATION, "Bearer alice")
            .request()
        assertThat(response.status()).isEqualTo(Status.OK_200)
        assertThat(response.`as`(String::class.java)).isEqualTo("alice")
    }
}
