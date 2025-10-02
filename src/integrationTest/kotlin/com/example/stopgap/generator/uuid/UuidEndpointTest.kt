package com.example.stopgap.generator.uuid

import com.example.stopgap.instanceregistry.InstanceRegistry
import extension.WebserverTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@WebserverTest
class UuidEndpointTest {

    @Test
    fun returnsUuid() {
        assertThat(10).isEqualTo(20)
    }

    companion object {

        @JvmStatic
        @WebserverTest.SetupInstanceRegistry
        fun setupInstanceRegistry(registry: InstanceRegistry) {
            System.err.println("Setting up instance registry")
        }

    }
}