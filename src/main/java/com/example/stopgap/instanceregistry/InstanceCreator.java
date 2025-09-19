package com.example.stopgap.instanceregistry;

@FunctionalInterface
public interface InstanceCreator {
    Object create(InstanceRegistry registry);
}
