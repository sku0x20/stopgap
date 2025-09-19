package com.example.stopgap;

import java.util.function.Function;

public final class InstanceRegistry {


    public void register(
        final String qualifier,
        final Function<InstanceRegistry, Object> creator
    ) {

    }

    public <T> T getInstance(
        final String qualifier,
        final Class<T> clazz
    ) {
        return null;
    }
}
