package com.example.stopgap.instanceregistry;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

final class InstanceRegistryTest {

    private final InstanceRegistry registry = new InstanceRegistry();

    @Test
    void registerByType() {
        registry.registerForType(T1.class, InstanceRegistryTest::createT1);
        final var t1 = registry.getInstanceForType(T1.class);
        assertThat(t1).isInstanceOf(T1.class);
    }

    @Test
    void returnsSameInstance() {
        final var qualifier = T1.class.getSimpleName();
        registry.registerForQualifier(qualifier, InstanceRegistryTest::createT1);

        final var i1 = registry.getInstanceForQualifier(qualifier, T1.class);
        final var i2 = registry.getInstanceForQualifier(qualifier, T1.class);

        assertThat(i1).isInstanceOf(T1.class);
        assertThat(i1).isSameAs(i2);
    }

    @Test
    void onlyRegisterOnce() {
        final var qualifier = T1.class.getSimpleName();
        registry.registerForQualifier(qualifier, InstanceRegistryTest::createT1);
        assertThatExceptionOfType(CreatorExistsException.class).isThrownBy(() -> {
            registry.registerForQualifier(qualifier, InstanceRegistryTest::createT1);
        });
    }

    @Test
    void dependencyHandling() {
        registry.registerForQualifier("t2", InstanceRegistryTest::createT2);
        registry.registerForQualifier("t1", InstanceRegistryTest::createT1);

        final var t2 = registry.getInstanceForQualifier("t2", T2.class);
        assertThat(t2).isInstanceOf(T2.class);
        final var t1 = registry.getInstanceForQualifier("t1", T1.class);
        assertThat(t1).isSameAs(t2.t1);
    }

    private static T1 createT1(final InstanceRegistry registry) {
        return new T1();
    }

    private record T1() {
    }

    private static T2 createT2(final InstanceRegistry registry) {
        final var t1 = registry.getInstanceForQualifier("t1", T1.class);
        return new T2(t1);
    }

    private record T2(T1 t1) {
    }

}