package com.example.stopgap.serde

import com.example.stopgap.serde.fastjson.FastjsonSerde
import dev.sku20.helidon.serde.Serde
import dev.sku20.helidon.serde.SerdeCatalog
import extension.InjectInstance
import extension.webservertest.SetupCapture
import extension.webservertest.WebserverTest
import io.helidon.http.HeaderNames
import io.helidon.http.HeaderValues
import io.helidon.http.HttpMediaType
import io.helidon.http.HttpMediaTypes
import io.helidon.http.Status
import io.helidon.webclient.api.WebClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.random.Random
import kotlin.reflect.KClass
import kotlin.reflect.KType

@WebserverTest
class SerdeEndpointTest {

    @InjectInstance
    lateinit var client: WebClient

    @Test
    fun des() {
        val random = Random.nextLong()
        val data = """{"data": "$random"}"""

        val response = client.post("/serde/desObj")
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
            .request()
        assertThat(response.status()).isEqualTo(Status.OK_200)
        assertThat(response.headers().contentType()).hasValue(HttpMediaTypes.JSON_UTF_8)
        val text = response.inputStream().bufferedReader().readText()
        assertThat(text).isEqualTo("""{"data":"$random"}""")
    }

    @Test
    fun negotiatesJson() {
        val response = client.post("/serde/negotiate")
            .header(HeaderValues.create(HeaderNames.CONTENT_TYPE, HttpMediaTypes.JSON_UTF_8.text()))
            .submit("""{"data":"foo"}""")
        assertThat(response.status()).isEqualTo(Status.OK_200)
        assertThat(response.headers().contentType()).hasValue(HttpMediaTypes.JSON_UTF_8)
        val text = response.inputStream().bufferedReader().readText()
        assertThat(text).isEqualTo("""{"data":"foo"}""")
    }

    @Test
    fun negotiatesPlainText() {
        val response = client.post("/serde/negotiate")
            .header(HeaderValues.create(HeaderNames.CONTENT_TYPE, HttpMediaTypes.PLAINTEXT_UTF_8.text()))
            .submit("foo")
        assertThat(response.status()).isEqualTo(Status.OK_200)
        assertThat(response.headers().contentType()).hasValue(HttpMediaTypes.PLAINTEXT_UTF_8)
        val text = response.inputStream().bufferedReader().readText()
        assertThat(text).isEqualTo("foo")
    }

    private class PlainTextSerde : Serde {
        override val mediaType: HttpMediaType = HttpMediaTypes.PLAINTEXT_UTF_8

        @Suppress("UNCHECKED_CAST")
        override fun <T : Any> deserialize(bytes: ByteArray, kClazz: KClass<T>): T {
            return ReqDto(String(bytes)) as T
        }

        override fun <T> deserialize(bytes: ByteArray, type: KType): T {
            throw UnsupportedOperationException()
        }

        @Suppress("UNCHECKED_CAST")
        override fun <T> serialize(obj: T): ByteArray {
            return (obj as ResDto).data.toByteArray()
        }
    }

    private class TestSerdeCatalog : SerdeCatalog {
        private val json = FastjsonSerde()
        private val plainText = PlainTextSerde()

        override fun get(mediaType: HttpMediaType): Serde = when (mediaType) {
            HttpMediaTypes.JSON_UTF_8 -> json
            HttpMediaTypes.PLAINTEXT_UTF_8 -> plainText
            else -> throw IllegalArgumentException("No serde registered for $mediaType")
        }
    }

    companion object {
        @JvmStatic
        @WebserverTest.Setup
        fun setup(): SetupCapture {
            return SetupCapture(SerdeEndpoint(), arrayOf(TestSerdeCatalog()))
        }
    }
}