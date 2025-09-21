package com.example.stopgap.instanceregistry;

import java.util.HashMap;
import java.util.Map;

/**
 * A registry for managing instance creation and retrieval. This class allows for registering
 * instance creators by type or qualifier and retrieving instances as needed. Instances are
 * instantiated lazily and cached for subsequent access.
 * <p>
 * Thread-safety is not guaranteed, and it is up to the caller to ensure appropriate usage in
 * multithreaded environments.
 */
public final class InstanceRegistry {

    private final Map<String, Object> instances = new HashMap<>();
    private final Map<String, InstanceCreator<?>> creators = new HashMap<>();

    private final Config config;

    public InstanceRegistry(final Config config) {
        this.config = config;
    }

    public Config getConfig() {
        return config;
    }

    public <T> void registerForType(
        final Class<T> clazz,
        final InstanceCreator<T> creator
    ) {
        registerForQualifier(clazz.getName(), creator);
    }

    public void registerForQualifier(
        final String qualifier,
        final InstanceCreator<?> creator
    ) {
        if (creators.containsKey(qualifier)) throw new CreatorExistsException(qualifier);
        creators.put(qualifier, creator);
    }

    public <T> T getInstanceForType(
        final Class<T> clazz
    ) {
        return getInstanceForQualifier(clazz.getName(), clazz);
    }

    @SuppressWarnings("unchecked")
    public <T> T getInstanceForQualifier(
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
