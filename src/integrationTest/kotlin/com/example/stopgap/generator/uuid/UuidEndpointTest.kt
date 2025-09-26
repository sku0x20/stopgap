package com.example.stopgap.generator.uuid

import com.example.stopgap.generator.uuid.web.UuidEndpoint
import com.example.stopgap.instanceregistry.Config
import com.example.stopgap.instanceregistry.InstanceRegistry
import extension.WebserverTestExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.mock

@ExtendWith(WebserverTestExtension::class)
class UuidEndpointTest {

    @Test
    fun returnsUuid() {
        assertThat(10).isEqualTo(20)
    }

    companion object {

        @JvmStatic
        fun setUpRoute() {
            val config: Config = mock<Config>()
            val registry = InstanceRegistry(config)
            val endpoint = UuidEndpoint(UuidGen())
        }

    }
}