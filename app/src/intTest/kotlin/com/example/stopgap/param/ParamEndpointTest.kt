package com.example.stopgap.param

import extension.InjectInstance
import extension.webservertest.SetupCapture
import extension.webservertest.WebserverTest
import io.helidon.http.Status
import io.helidon.webclient.api.WebClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@WebserverTest
class ParamEndpointTest {

    @InjectInstance
    lateinit var client: WebClient

    @Test
    fun pathParam() {
        val response = client.get("/param/abc123").request()
        assertThat(response.status()).isEqualTo(Status.OK_200)
        assertThat(response.`as`(String::class.java)).isEqualTo("abc123")
    }

    companion object {
        @JvmStatic
        @WebserverTest.Setup
        fun setup(): SetupCapture {
            return SetupCapture(ParamEndpoint(), emptyArray())
        }
    }
}
