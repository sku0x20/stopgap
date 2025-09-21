package com.example.stopgap;

import io.helidon.config.Config;
import io.helidon.config.ConfigSources;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

final class HelidonConfigTest {

    @SuppressWarnings("DuplicateStringLiteralInspection")
    @Test
    void get() {
        final var configSource = ConfigSources.create(Map.of("test", "some-value"));
        final var source = Config.create(configSource);
        final var config = new HelidonConfig(source);
        final var value = config.get("test");
        assertThat(value).isEqualTo("some-value");
    }

}