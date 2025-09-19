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
