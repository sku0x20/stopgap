package e2e

import dev.sku20.stopgap.helidon.test.InjectInstance
import io.helidon.http.HeaderNames
import io.helidon.http.HttpMediaTypes
import io.helidon.http.Status
import io.helidon.webclient.api.WebClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream

class EncodingCalls {

    @InjectInstance
    lateinit var client: WebClient

    @Test
    fun gzipResponse() {
        val response = client.get("/ping")
            .header(HeaderNames.ACCEPT_ENCODING, "gzip")
            .request()
        assertThat(response.status()).isEqualTo(Status.OK_200)
        assertThat(response.headers().first(HeaderNames.CONTENT_ENCODING)).hasValue("gzip")
        // WebClient decodes it: the gzip codec is on the e2e classpath via the app's dependencies.
        assertThat(response.`as`(String::class.java)).isEqualTo("pong")
    }

    @Test
    fun gzipRequest() {
        val response = client.post("/serde/desObj")
            .contentType(HttpMediaTypes.JSON_UTF_8)
            .header(HeaderNames.CONTENT_ENCODING, "gzip")
            .submit(gzip("""{"data": "alice"}"""))
        assertThat(response.status()).isEqualTo(Status.OK_200)
        assertThat(response.inputStream().bufferedReader().readText()).isEqualTo("alice")
    }

    private fun gzip(text: String): ByteArray {
        val bytes = ByteArrayOutputStream()
        GZIPOutputStream(bytes).use { it.write(text.toByteArray()) }
        return bytes.toByteArray()
    }
}
