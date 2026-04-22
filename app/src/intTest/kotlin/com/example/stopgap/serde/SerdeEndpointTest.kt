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

        val response = client.post("/serde/des")
            .submit(data)
        assertThat(response.status()).isEqualTo(Status.OK_200)
        assertThat(response.headers().contentType()).hasValue(HttpMediaTypes.PLAINTEXT_UTF_8)
    }

    companion object {
        @JvmStatic
        @WebserverTest.CreateEndpoint
        fun createEndpoint(extras: MutableMap<Class<*>, Any>): SerdeEndpoint {
            return SerdeEndpoint()
        }
    }
}