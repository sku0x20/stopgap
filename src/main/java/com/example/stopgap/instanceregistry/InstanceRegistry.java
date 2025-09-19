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
        if (creators.containsKey(qualifier)) throw new CreatorExistsException(qualifier);
        creators.put(qualifier, creator);
    }

    @SuppressWarnings("unchecked")
    public <T> T getInstance(
        final String qualifier,
        final Class<T> clazz
    ) {
        final var instance = instances.get(qualifier);
        return instance == null ? (T) createInstance(qualifier) : (T) instance;
    }

    private Object createInstance(final String qualifier) {
        final var creator = creators.get(qualifier);
        if (creator == null) throw new NoCreatorException(qualifier);
        final var instance = creator.create(this);
        instances.put(qualifier, instance);
        return instance;
    }
}
