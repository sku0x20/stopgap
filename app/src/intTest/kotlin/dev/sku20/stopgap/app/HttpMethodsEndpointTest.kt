package dev.sku20.stopgap.app

import dev.sku20.stopgap.helidon.test.InjectInstance
import dev.sku20.stopgap.helidon.test.integration.SetupCapture
import dev.sku20.stopgap.helidon.test.integration.WebserverTest
import io.helidon.http.Status
import io.helidon.webclient.api.WebClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@WebserverTest
class HttpMethodsEndpointTest {

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

    @Test
    fun delete() {
        val response = client.delete("/methods/delete").request()
        assertThat(response.status()).isEqualTo(Status.OK_200)
        assertThat(response.`as`(String::class.java)).isEqualTo("DELETE request received")
    }

    @Test
    fun put() {
        val response = client.put("/methods/put").request()
        assertThat(response.status()).isEqualTo(Status.OK_200)
        assertThat(response.`as`(String::class.java)).isEqualTo("PUT request received")
    }

    @Test
    fun patch() {
        val response = client.patch("/methods/patch").request()
        assertThat(response.status()).isEqualTo(Status.OK_200)
        assertThat(response.`as`(String::class.java)).isEqualTo("PATCH request received")
    }

    @Test
    fun head() {
        val response = client.head("/methods/head").request()
        assertThat(response.status()).isEqualTo(Status.OK_200)
        assertThat(response.entity().hasEntity()).isFalse()
    }

    @Test
    fun options() {
        val response = client.options("/methods/options").request()
        assertThat(response.status()).isEqualTo(Status.OK_200)
        assertThat(response.`as`(String::class.java)).isEqualTo("OPTIONS request received")
    }

    @Test
    fun trace() {
        val response = client.trace("/methods/trace").request()
        assertThat(response.status()).isEqualTo(Status.OK_200)
        assertThat(response.`as`(String::class.java)).isEqualTo("TRACE request received")
    }

    @Test
    fun wrongMethod() {
        val response = client.post("/methods/get").request()
        assertThat(response.status()).isEqualTo(Status.NOT_FOUND_404)
    }

    companion object {

        @JvmStatic
        @WebserverTest.Setup
        fun createEndpoint(): SetupCapture {
            return SetupCapture(HttpMethodsEndpoint())
        }

    }

}
