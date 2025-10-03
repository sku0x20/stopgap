package com.example.stopgap.generator.uuid

import com.example.stopgap.generator.uuid.web.UuidEndpoint
import com.example.stopgap.instanceregistry.Config
import com.example.stopgap.instanceregistry.InstanceRegistry
import extension.InjectInstance
import extension.WebserverTest
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

    @Test
    fun injectFields() {
        assertThat(config).isNotNull()
        assertThat(registry).isNotNull()
        assertThat(server).isNotNull()
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