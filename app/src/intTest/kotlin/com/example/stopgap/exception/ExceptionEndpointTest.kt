package com.example.stopgap.exception

import extension.InjectInstance
import extension.webservertest.WebserverTest
import io.helidon.http.Status
import io.helidon.webclient.api.WebClient
import io.helidon.webserver.http.HttpRouting
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


@WebserverTest
class ExceptionEndpointTest {

    @InjectInstance
    lateinit var client: WebClient

    @Test
    fun badClient1() {
        val response = client.get("/bad-client-1").request()
        assertThat(response.status()).isEqualTo(Status.BAD_REQUEST_400)
    }

    @Test
    fun badClient2() {
        val response = client.get("/bad-client-2").request()
        assertThat(response.status()).isEqualTo(Status.BAD_REQUEST_400)
    }

    companion object {

        // todo:
        // createEndpoint(extras: Map<>): Endpoint
        //  --> calls initEndpoint(endpoint)
        // destroyEndpoint(endpoint: Endpoint, extras: Map<>)

        @JvmStatic
        @WebserverTest.ConfigRoutes
        fun configureRoutes(routes: HttpRouting.Builder) {
            val endpoint = ExceptionEndpoint()
            routes.get("/bad-client-1", endpoint::badClientException)
            routes.get("/bad-client-2", endpoint::viaHttpException)
        }

    }

}