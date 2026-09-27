package dev.sku20.stopgap.app.authn

import dev.sku20.stopgap.helidon.test.InjectInstance
import dev.sku20.stopgap.helidon.test.integration.NoAuthN
import dev.sku20.stopgap.helidon.test.integration.SetupCapture
import dev.sku20.stopgap.helidon.test.integration.TestAuthFakeResolver
import dev.sku20.stopgap.helidon.test.integration.WebserverTest
import io.helidon.http.Status
import io.helidon.webclient.api.WebClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@WebserverTest
class SecureEndpointFakeAuthTest {

    @InjectInstance
    lateinit var client: WebClient

    @InjectInstance
    lateinit var fakeAuth: TestAuthFakeResolver

    @Test
    fun userEndpointReturnsOkWhenFakeAuthIsUser() {
        fakeAuth.currentAuth = UserAuthN("bob")
        val response = client.get("/secure/user").request()
        assertThat(response.status()).isEqualTo(Status.OK_200)
        assertThat(response.`as`(String::class.java)).isEqualTo("bob")
    }

    @Test
    fun userEndpointForbiddenWhenFakeAuthIsNoAuth() {
        fakeAuth.currentAuth = NoAuthN
        val response = client.get("/secure/user").request()
        assertThat(response.status()).isEqualTo(Status.FORBIDDEN_403)
    }

    companion object {
        @JvmStatic
        @WebserverTest.Setup
        fun setup(): SetupCapture {
            return SetupCapture(SecureEndpoint())
        }
    }
}
