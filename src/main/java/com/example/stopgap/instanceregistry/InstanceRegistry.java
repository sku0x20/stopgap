package com.example.stopgap.instanceregistry;

import java.util.HashMap;
import java.util.Map;

/**
 * A registry for managing and retrieving singleton-like instances. This class facilitates the creation
 * and reuse of instances identified by a qualifier. Instances are created lazily and can be accessed
 * using their qualifiers and expected type.
 * <p>
 * This registry is thread-unsafe and should be used carefully in a multithreaded environment.
 */
public final class InstanceRegistry {

    private final Map<String, Object> instances = new HashMap<>();
    private final Map<String, InstanceCreator> creators = new HashMap<>();

    public void register(
        final String qualifier,
        final InstanceCreator creator
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
            if (creator == null) throw new NoCreatorException(qualifier);
            return creator.create(this);
        });
        return (T) instance;
    }
}
