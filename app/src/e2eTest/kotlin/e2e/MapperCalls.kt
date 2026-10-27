package e2e

import dev.sku20.stopgap.helidon.test.InjectInstance
import io.helidon.http.HttpMediaTypes
import io.helidon.http.Status
import io.helidon.webclient.api.WebClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MapperCalls {

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

    @Test
    fun serObj() {
        val response = client.get("/mapper/serObj").request()
        assertThat(response.status()).isEqualTo(Status.OK_200)
        assertThat(response.headers().contentType()).hasValue(HttpMediaTypes.JSON)
        val text = response.inputStream().bufferedReader().readText()
        assertThat(text).isEqualTo("""{"data":"foo"}""")
    }

    @Test
    fun serGeneric() {
        val response = client.get("/mapper/serGeneric").request()
        assertThat(response.status()).isEqualTo(Status.OK_200)
        assertThat(response.headers().contentType()).hasValue(HttpMediaTypes.JSON)
        val text = response.inputStream().bufferedReader().readText()
        assertThat(text).isEqualTo("""[{"data":"foo"},{"data":"bar"}]""")
    }

    @Test
    fun serPlainText() {
        val response = client.get("/mapper/serPlainText").request()
        assertThat(response.status()).isEqualTo(Status.OK_200)
        assertThat(response.headers().contentType()).hasValue(HttpMediaTypes.PLAINTEXT_UTF_8)
        val text = response.inputStream().bufferedReader().readText()
        assertThat(text).isEqualTo("foo")
    }

}
