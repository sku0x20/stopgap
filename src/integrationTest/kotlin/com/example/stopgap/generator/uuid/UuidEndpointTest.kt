package com.example.stopgap.generator.uuid

import com.example.stopgap.generator.uuid.web.UuidEndpoint
import com.example.stopgap.instanceregistry.Config
import com.example.stopgap.instanceregistry.InstanceRegistry
import extension.InjectInstance
import extension.WebserverTest
import io.helidon.http.HttpMediaTypes
import io.helidon.http.Status
import io.helidon.webclient.api.WebClient
import io.helidon.webserver.WebServer
import io.helidon.webserver.WebServerConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@WebserverTest
class UuidEndpointTest {


    @InjectInstance
    lateinit var config: Config

    @InjectInstance
    lateinit var registry: InstanceRegistry

    @InjectInstance
    lateinit var server: WebServer

    @InjectInstance
    lateinit var client: WebClient

    @Test
    fun injectFields() {
        assertThat(config).isNotNull()
        assertThat(registry).isNotNull()
        assertThat(server).isNotNull()
    }

    @Test
    fun testUuidGeneration() {
        val response = client.get().uri("/").request()
        assertThat(response.status()).isEqualTo(Status.OK_200)
        val contentType = response.headers().contentType().get()
        assertThat(contentType as Any).isEqualTo(HttpMediaTypes.PLAINTEXT_UTF_8)
        val body = response.`as`(String::class.java)
        assertThat(body).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")
    }

    companion object {

        @JvmStatic
        @WebserverTest.SetupInstanceRegistry(UuidEndpoint::class)
        fun setupInstanceRegistry(registry: InstanceRegistry) {
            registry.registerForType { UuidGen() }
            registry.registerForType { UuidEndpoint(it.getInstanceForType()) }
        }

        @JvmStatic
        @WebserverTest.ConfigServer
        fun configServer(builder: WebServerConfig.Builder) {
            // config server
        }

    }
}