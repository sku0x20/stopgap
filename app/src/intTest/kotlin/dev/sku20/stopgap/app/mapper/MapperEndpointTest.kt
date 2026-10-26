package dev.sku20.stopgap.app.mapper

import dev.sku20.stopgap.app.serde.fastjson.FastjsonSerde
import dev.sku20.stopgap.helidon.serde.MapSerdeCatalog
import dev.sku20.stopgap.helidon.test.InjectInstance
import dev.sku20.stopgap.helidon.test.integration.SetupCapture
import dev.sku20.stopgap.helidon.test.integration.WebserverTest
import io.helidon.http.HttpMediaTypes
import io.helidon.http.Status
import io.helidon.webclient.api.WebClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@WebserverTest
class MapperEndpointTest {

    @InjectInstance
    lateinit var client: WebClient

    @Test
    fun desObj() {
        val response = client.post("/mapper/desObj")
            .contentType(HttpMediaTypes.JSON_UTF_8)
            .submit("""{"data":"foo"}""")
        assertThat(response.status()).isEqualTo(Status.OK_200)
        val text = response.inputStream().bufferedReader().readText()
        assertThat(text).isEqualTo("foo")
    }

    @Test
    fun desDataClass() {
        val response = client.post("/mapper/desDataClass")
            .contentType(HttpMediaTypes.JSON_UTF_8)
            .submit("""{"id": 1, "name": "alice"}""")
        assertThat(response.status()).isEqualTo(Status.OK_200)
        val text = response.inputStream().bufferedReader().readText()
        assertThat(text).isEqualTo("1:alice")
    }

    @Test
    fun desGeneric() {
        val response = client.post("/mapper/desGeneric")
            .contentType(HttpMediaTypes.JSON_UTF_8)
            .submit("""[{"data":"foo"},{"data":"bar"}]""")
        assertThat(response.status()).isEqualTo(Status.OK_200)
        val text = response.inputStream().bufferedReader().readText()
        assertThat(text).isEqualTo("foo = bar")
    }

    @Test
    fun desPlainText() {
        val response = client.post("/mapper/desPlainText")
            .contentType(HttpMediaTypes.PLAINTEXT_UTF_8)
            .submit("foo")
        assertThat(response.status()).isEqualTo(Status.OK_200)
        val text = response.inputStream().bufferedReader().readText()
        assertThat(text).isEqualTo("foo")
    }

    companion object {
        @JvmStatic
        @WebserverTest.Setup
        fun setup(): SetupCapture {
            val catalog = MapSerdeCatalog()
            catalog.add(FastjsonSerde())
            return SetupCapture(MapperEndpoint(), registerParams = arrayOf(catalog))
        }
    }
}
