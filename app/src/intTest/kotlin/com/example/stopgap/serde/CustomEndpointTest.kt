package com.example.stopgap.serde

import com.example.stopgap.serde.plain.ReqResPlainSerde
import dev.sku20.helidon.serde.MapSerdeCatalog
import extension.InjectInstance
import extension.webservertest.SetupCapture
import extension.webservertest.WebserverTest
import io.helidon.http.HttpMediaTypes
import io.helidon.http.Status
import io.helidon.webclient.api.WebClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@WebserverTest
class CustomEndpointTest {

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

    companion object {
        @JvmStatic
        @WebserverTest.Setup
        fun setup(): SetupCapture {
            val catalog = MapSerdeCatalog()
            catalog.add(ReqResPlainSerde())
            return SetupCapture(CustomEndpoint(), arrayOf(catalog))
        }
    }
}
