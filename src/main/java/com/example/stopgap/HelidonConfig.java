package com.example.stopgap;

import com.example.stopgap.instanceregistry.Config;

public final class HelidonConfig implements Config {

    private final io.helidon.config.Config source;

    public HelidonConfig(final io.helidon.config.Config source) {
        this.source = source;
    }

    @Override
    public String get(final String key) {
        return source.get(key).asString().orElseThrow();
    }

}
