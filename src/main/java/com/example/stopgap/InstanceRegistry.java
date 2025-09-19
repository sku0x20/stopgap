package com.example.stopgap;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * A registry for managing and retrieving singleton-like instances. This class facilitates the creation
 * and reuse of instances identified by a qualifier. Instances are created lazily and can be accessed
 * using their qualifiers and expected type.
 * <p>
 * This registry is thread-unsafe and should be used carefully in a multithreaded environment.
 */
public final class InstanceRegistry {

    private final Map<String, Object> instances = new HashMap<>();
    private final Map<String, Function<InstanceRegistry, Object>> creators = new HashMap<>();

    public void register(
        final String qualifier,
        final Function<InstanceRegistry, Object> creator
    ) {
        creators.put(qualifier, creator);
    }

    @SuppressWarnings("unchecked")
    public <T> T getInstance(
        final String qualifier,
        final Class<T> clazz
    ) {
        final var instance = instances.computeIfAbsent(qualifier, (key) -> {
            final var creator = creators.get(key);
            if (creator == null) {
                throw new IllegalArgumentException("No instance registered for qualifier: " + qualifier);
            }
            return creator.apply(this);
        });
        return (T) instance;
    }
}
