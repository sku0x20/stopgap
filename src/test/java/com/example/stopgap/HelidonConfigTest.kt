package com.example.stopgap

import io.helidon.config.Config
import io.helidon.config.ConfigSources
import io.helidon.config.spi.ConfigNode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class HelidonConfigTest {

    @Test
    fun get() {
        val objectNode = ConfigNode.ObjectNode.builder()
            .addValue("a", "a")
            .addObject(
                "b", ConfigNode.ObjectNode.builder()
                    .addValue("b1", "b1")
                    .build()
            )
            .build()
        val configSource = ConfigSources.create(objectNode)
        val source = Config.create(configSource)

        val config = HelidonConfig(source)
        assertThat(config.get("a")).isEqualTo("a")
        assertThat(config.get("b.b1")).isEqualTo("b1")

        assertThat(config.getConfig("b")).isInstanceOf(Config::class.java)
    }

}