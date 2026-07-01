package dev.sku20.stopgap.app.serde

import dev.sku20.stopgap.app.serde.fastjson.FastjsonSerde
import dev.sku20.stopgap.app.serde.plain.ReqResPlainSerde
import dev.sku20.stopgap.helidon.serde.MapSerdeCatalog
import dev.sku20.stopgap.helidon.test.InjectInstance
import dev.sku20.stopgap.helidon.test.integration.extension.SetupCapture
import dev.sku20.stopgap.helidon.test.integration.extension.WebserverTest
import io.helidon.http.HttpMediaTypes
import io.helidon.http.Status
import io.helidon.webclient.api.WebClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.random.Random

@WebserverTest
class SerdeEndpointTest {

    @InjectInstance
    lateinit var client: WebClient

    @Test
    fun des() {
        val random = Random.nextLong()
        val data = """{"data": "$random"}"""

        val response = client.post("/serde/desObj")
            .contentType(HttpMediaTypes.JSON_UTF_8)
            .accept(HttpMediaTypes.JSON_UTF_8)
            .submit(data)
        assertThat(response.status()).isEqualTo(Status.OK_200)
        assertThat(response.headers().contentType()).hasValue(HttpMediaTypes.PLAINTEXT_UTF_8)
        val text = response.inputStream().bufferedReader().readText()
        assertThat(text).isEqualTo(random.toString())
    }

    @Test
    fun unitGet() {
        val response = client.get("/serde/unitGet").request()
        assertThat(response.status()).isEqualTo(Status.OK_200)
        val text = response.inputStream().bufferedReader().readText()
        assertThat(text).isEmpty()
    }

    @Test
    fun genericPost() {
        val body = """[{"data":"foo"},{"data":"bar"}]"""

        val response = client.post("/serde/genericPost")
            .contentType(HttpMediaTypes.JSON_UTF_8)
            .accept(HttpMediaTypes.JSON_UTF_8)
            .submit(body)
        assertThat(response.status()).isEqualTo(Status.OK_200)
        assertThat(response.headers().contentType()).hasValue(HttpMediaTypes.JSON_UTF_8)
        val text = response.inputStream().bufferedReader().readText()
        assertThat(text).isEqualTo("""{"data":"foo = bar"}""")
    }

    @Test
    fun ser() {
        val random = Random.nextLong()
        val response = client.get("/serde/serObj")
            .queryParam("data", random.toString())
            .accept(HttpMediaTypes.JSON_UTF_8)
            .request()
        assertThat(response.status()).isEqualTo(Status.OK_200)
        assertThat(response.headers().contentType()).hasValue(HttpMediaTypes.JSON_UTF_8)
        val text = response.inputStream().bufferedReader().readText()
        assertThat(text).isEqualTo("""{"data":"$random"}""")
    }

    @Test
    fun negotiate() {
        var response = client.post("/serde/negotiate")
            .contentType(HttpMediaTypes.JSON_UTF_8)
            .accept(HttpMediaTypes.JSON_UTF_8)
            .submit("""{"data":"foo"}""")
        assertThat(response.headers().contentType()).hasValue(HttpMediaTypes.JSON_UTF_8)
        var text = response.inputStream().bufferedReader().readText()
        assertThat(text).isEqualTo("""{"data":"foo"}""")

        response = client.post("/serde/negotiate")
            .contentType(HttpMediaTypes.PLAINTEXT_UTF_8)
            .accept(HttpMediaTypes.PLAINTEXT_UTF_8)
            .submit("foo")
        assertThat(response.status()).isEqualTo(Status.OK_200)
        assertThat(response.headers().contentType()).hasValue(HttpMediaTypes.PLAINTEXT_UTF_8)
        text = response.inputStream().bufferedReader().readText()
        assertThat(text).isEqualTo("foo")
    }

    companion object {
        @JvmStatic
        @WebserverTest.Setup
        fun setup(): SetupCapture {
            val catalog = MapSerdeCatalog()
            catalog.add(FastjsonSerde())
            catalog.add(ReqResPlainSerde())
            return SetupCapture(SerdeEndpoint(), arrayOf(catalog))
        }
    }
}