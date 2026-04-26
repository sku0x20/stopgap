package com.example.stopgap.serde

import extension.InjectInstance
import extension.webservertest.WebserverTest
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
        val data = """{"expected": "$random"}"""

        val response = client.post("/serde/desObj")
            .submit(data)
        assertThat(response.status()).isEqualTo(Status.OK_200)
        assertThat(response.headers().contentType()).hasValue(HttpMediaTypes.PLAINTEXT_UTF_8)
        val text = response.inputStream().bufferedReader().readText()
        assertThat(text).isEqualTo(random.toString())
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

    companion object {
        @JvmStatic
        @WebserverTest.Setup
        fun createEndpoint(extras: MutableMap<Class<*>, Any>): SerdeEndpoint {
            return SerdeEndpoint()
        }
    }
}