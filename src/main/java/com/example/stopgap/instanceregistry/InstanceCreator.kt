package com.example.stopgap.instanceregistry;

@FunctionalInterface
public interface InstanceCreator<T> {
    T create(InstanceRegistry registry);
}
