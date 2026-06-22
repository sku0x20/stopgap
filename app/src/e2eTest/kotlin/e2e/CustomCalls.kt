package e2e

import extension.InjectInstance
import io.helidon.http.HttpMediaTypes
import io.helidon.http.Status
import io.helidon.webclient.api.WebClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CustomCalls {

    @InjectInstance
    lateinit var client: WebClient

    @Test
    fun getReturnsPlaintext() {
        val response = client.get("/custom-serde-catalog/")
            .accept(HttpMediaTypes.PLAINTEXT_UTF_8)
            .request()
        assertThat(response.status()).isEqualTo(Status.OK_200)
        assertThat(response.headers().contentType()).hasValue(HttpMediaTypes.PLAINTEXT_UTF_8)
        val text = response.inputStream().bufferedReader().readText()
        assertThat(text).isEqualTo("hello")
    }

    @Test
    fun getJsonReturnsJson() {
        val response = client.get("/custom-serde-catalog/json")
            .accept(HttpMediaTypes.JSON_UTF_8)
            .request()
        assertThat(response.status()).isEqualTo(Status.OK_200)
        assertThat(response.headers().contentType()).hasValue(HttpMediaTypes.JSON_UTF_8)
        val text = response.inputStream().bufferedReader().readText()
        assertThat(text).isEqualTo("""{"data":"hello-json"}""")
    }

}
