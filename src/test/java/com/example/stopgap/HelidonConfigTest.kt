package com.example.stopgap;

import io.helidon.config.Config;
import io.helidon.config.ConfigSources;
import io.helidon.config.spi.ConfigNode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

final class HelidonConfigTest {

    @Test
    void get() {
        final var objectNode = ConfigNode.ObjectNode.builder()
            .addValue("a", "a")
            .addObject("b", ConfigNode.ObjectNode.builder()
                .addValue("b1", "b1")
                .build())
            .build();
        final var configSource = ConfigSources.create(objectNode);
        final var source = Config.create(configSource);

        final var config = new HelidonConfig(source);
        assertThat(config.get("a")).isEqualTo("a");
        assertThat(config.get("b.b1")).isEqualTo("b1");

        assertThat(config.getConfig("b")).isInstanceOf(Config.class);
    }

}