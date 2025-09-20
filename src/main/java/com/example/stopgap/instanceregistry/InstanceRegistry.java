package com.example.stopgap.instanceregistry;

import java.util.HashMap;
import java.util.Map;

/**
 * A registry for managing singleton instances of objects by their associated qualifiers.
 * This class allows registering instance creators and retrieving or creating instances as needed.
 * Once a creator is registered for a specific qualifier, it cannot be re-registered.
 * Instances are lazily created upon the first request and are cached for subsequent requests.
 * <p>
 * This registry supports dependency injection by allowing instance creators to request other instances
 * from the registry while creating their own instance.
 */
public final class InstanceRegistry {

    private final Map<String, Object> instances = new HashMap<>();
    private final Map<String, InstanceCreator<?>> creators = new HashMap<>();

    public <T> void registerType(
        final Class<T> clazz,
        final InstanceCreator<T> creator
    ) {
        registerQualifier(clazz.getName(), creator);
    }

    // todo: rename to forQualifier
    public void registerQualifier(
        final String qualifier,
        final InstanceCreator<?> creator
    ) {
        if (creators.containsKey(qualifier)) throw new CreatorExistsException(qualifier);
        creators.put(qualifier, creator);
    }

    public <T> T getInstanceForType(
        final Class<T> clazz
    ) {
        return getInstance(clazz.getName(), clazz);
    }

    // todo: rename to forType
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
