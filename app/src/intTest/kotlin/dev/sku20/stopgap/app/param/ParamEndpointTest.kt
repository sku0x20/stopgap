package dev.sku20.stopgap.app.param

import dev.sku20.stopgap.helidon.test.InjectInstance
import dev.sku20.stopgap.helidon.test.integration.SetupCapture
import dev.sku20.stopgap.helidon.test.integration.WebserverTest
import io.helidon.http.HeaderNames
import io.helidon.http.Status
import io.helidon.webclient.api.WebClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@WebserverTest
class ParamEndpointTest {

    @InjectInstance
    lateinit var client: WebClient

    @Test
    fun pathParam() {
        val response = client.get("/param/abc123").request()
        assertThat(response.status()).isEqualTo(Status.OK_200)
        assertThat(response.`as`(String::class.java)).isEqualTo("abc123")
    }

    @Test
    fun queryParam() {
        val response = client.get("/param/query").queryParam("name", "foo").request()
        assertThat(response.status()).isEqualTo(Status.OK_200)
        assertThat(response.`as`(String::class.java)).isEqualTo("foo")
    }

    @Test
    fun headerParam() {
        val response = client.get("/param/header").header(HeaderNames.ACCEPT_LANGUAGE, "en").request()
        assertThat(response.status()).isEqualTo(Status.OK_200)
        assertThat(response.`as`(String::class.java)).isEqualTo("en")
    }

    companion object {
        @JvmStatic
        @WebserverTest.Setup
        fun setup(): SetupCapture {
            return SetupCapture(ParamEndpoint(), emptyArray())
        }
    }
}
