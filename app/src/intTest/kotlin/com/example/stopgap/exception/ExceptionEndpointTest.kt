package com.example.stopgap.exception

import extension.InjectInstance
import extension.webservertest.WebserverTest
import io.helidon.http.Status
import io.helidon.webclient.api.WebClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


@WebserverTest
class ExceptionEndpointTest {

    @InjectInstance
    lateinit var client: WebClient

    @Test
    fun badClient1() {
        val response = client.get("/exception/bad-client-1").request()
        assertThat(response.status()).isEqualTo(Status.BAD_REQUEST_400)
    }

    @Test
    fun badClient2() {
        val response = client.get("/exception/bad-client-2").request()
        assertThat(response.status()).isEqualTo(Status.BAD_REQUEST_400)
    }

    companion object {

        @JvmStatic
        @WebserverTest.Setup
        fun createEndpoint(extras: MutableMap<Class<*>, Any>): ExceptionEndpoint {
            return ExceptionEndpoint()
        }

    }

}