package com.example.stopgap.generator.uuid

import com.example.stopgap.generator.uuid.web.UuidEndpoint
import extension.InjectInstance
import extension.webservertest.WebserverTest
import io.helidon.http.HttpMediaTypes
import io.helidon.http.Status
import io.helidon.webclient.api.WebClient
import io.helidon.webserver.WebServer
import io.helidon.webserver.WebServerConfig
import io.helidon.webserver.http.HttpRouting
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@WebserverTest
class UuidEndpointTest {

    @InjectInstance
    lateinit var client: WebClient

    @InjectInstance
    lateinit var server: WebServer

    @InjectInstance
    lateinit var uuidGen: UuidGen

    @Test
    fun injectFields() {
        assertThat(uuidGen).isNotNull()
        assertThat(server).isNotNull()
    }

    @Test
    fun notFound() {
        val response = client.get().uri("/generate/uuid/not-found").request()
        assertThat(response.status()).isEqualTo(Status.NOT_FOUND_404)
    }

    @Test
    fun testUuidGeneration() {
        val response = client.get().uri("/generate/uuid/").request()
        assertThat(response.status()).isEqualTo(Status.OK_200)
        val contentType = response.headers().contentType().get()
        assertThat(contentType as Any).isEqualTo(HttpMediaTypes.PLAINTEXT_UTF_8)
        val body = response.`as`(String::class.java)
        assertThat(body).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")
    }

    companion object {

        @JvmStatic
        @WebserverTest.CreateEndpoint
        fun createEndpoint(instances: MutableMap<Class<*>, Any>): UuidEndpoint {
            val uuidGen = UuidGen()
            instances[UuidGen::class.java] = uuidGen
            val endpoint = UuidEndpoint(uuidGen)
            return endpoint
        }

        @JvmStatic
        @WebserverTest.ConfigServer
        fun configServer(builder: WebServerConfig.Builder) {
            // config server
        }

        @JvmStatic
        @WebserverTest.Cleanup
        fun destroyInstances(instances: MutableMap<Class<*>, Any>) {
            val uuidGen = instances[UuidGen::class.java] as UuidGen
            // call close,clean,etc.
        }

    }
}