package com.example.stopgap.generator.uuid

import com.example.stopgap.generator.uuid.web.UuidEndpoint
import com.example.stopgap.instanceregistry.Config
import com.example.stopgap.instanceregistry.InstanceRegistry
import io.helidon.http.Status
import io.helidon.webclient.http1.Http1Client
import io.helidon.webserver.http.HttpRouting
import io.helidon.webserver.testing.junit5.RoutingTest
import io.helidon.webserver.testing.junit5.SetUpRoute
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

@RoutingTest
class UuidEndpointTest(
    private val client: Http1Client
) {

    @Test
    fun returnsUuid() {
        client.get("/").request().use { response ->
            assertThat(response.status()).isEqualTo(Status.OK_200)
        }
    }

    companion object {
        private val config: Config = mock<Config>()
        private val registry = InstanceRegistry(config)

        private val endpoint = UuidEndpoint(UuidGen())

        @SetUpRoute
        fun setUpRoute(builder: HttpRouting.Builder) {
            builder.register(endpoint.routes(registry))
        }
    }
}