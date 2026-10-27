package dev.sku20.stopgap.app.mapper

import dev.sku20.stopgap.helidon.mapper.req.bodyreqmapper.json.JsonReqMapper
import dev.sku20.stopgap.helidon.mapper.req.bodyreqmapper.plaintext.PlainTextReqMapper
import dev.sku20.stopgap.helidon.mapper.req.multipart.MultipartReqMapper
import dev.sku20.stopgap.helidon.mapper.res.bodyresmapper.json.JsonResMapper
import dev.sku20.stopgap.helidon.mapper.res.bodyresmapper.plaintext.PlainTextResMapper
import dev.sku20.stopgap.helidon.test.InjectInstance
import dev.sku20.stopgap.helidon.test.integration.SetupCapture
import dev.sku20.stopgap.helidon.test.integration.WebserverTest
import io.helidon.http.HttpMediaType
import io.helidon.http.HttpMediaTypes
import io.helidon.http.Status
import io.helidon.webclient.api.WebClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

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

    @Test
    fun desMultipart() {
        val response = client.post("/mapper/desMultipart")
            .contentType(HttpMediaType.create("multipart/form-data; boundary=abc"))
            .submit(
                "--abc\r\n" +
                    "Content-Disposition: form-data; name=\"title\"\r\n" +
                    "\r\n" +
                    "groceries\r\n" +
                    "--abc\r\n" +
                    "Content-Disposition: form-data; name=\"attachment\"; filename=\"notes.txt\"\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "\r\n" +
                    "hello\r\n" +
                    "--abc--\r\n"
            )
        assertThat(response.status()).isEqualTo(Status.OK_200)
        val text = response.inputStream().bufferedReader().readText()
        assertThat(text).isEqualTo("title:null:9 attachment:notes.txt:5")
    }

    @Test
    fun desMultipartDeletesTempFiles() {
        val response = client.post("/mapper/desMultipart")
            .contentType(HttpMediaType.create("multipart/form-data; boundary=abc"))
            .submit(
                "--abc\r\n" +
                    "Content-Disposition: form-data; name=\"attachment\"; filename=\"big.txt\"\r\n" +
                    "\r\n" +
                    "a".repeat(2000) + "\r\n" +
                    "--abc--\r\n"
            )
        assertThat(response.status()).isEqualTo(Status.OK_200)
        val text = response.inputStream().bufferedReader().readText()
        assertThat(text).isEqualTo("attachment:big.txt:2000")
        assertThat(awaitEmpty(tempDir)).isTrue()
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

    // finally runs after send returns, so the client can see the response a moment before the delete.
    private fun awaitEmpty(directory: Path): Boolean {
        val deadline = System.nanoTime() + 2_000_000_000
        while (System.nanoTime() < deadline) {
            if (Files.list(directory).use { it.count() } == 0L) return true
            Thread.sleep(10)
        }
        return false
    }

    companion object {
        private val tempDir: Path = Files.createTempDirectory("mapper-endpoint-test")

        @JvmStatic
        @WebserverTest.Setup
        fun setup(): SetupCapture {
            return SetupCapture(
                MapperEndpoint(),
                registerParams = arrayOf(
                    JsonReqMapper(FastJsonDecoder()),
                    PlainTextReqMapper(),
                    MultipartReqMapper(1024, tempDir),
                    JsonResMapper(FastJsonEncoder()),
                    PlainTextResMapper()
                )
            )
        }
    }
}
