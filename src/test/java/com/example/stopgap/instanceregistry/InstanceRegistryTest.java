package com.example.stopgap.instanceregistry;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

final class InstanceRegistryTest {

    final InstanceRegistry registry = new InstanceRegistry();

    @Test
    void returnsSameInstance() {
        final var qualifier = TestClass.class.getSimpleName();
        registry.register(qualifier, InstanceRegistryTest::createTestInstance);

        final var i1 = registry.getInstance(qualifier, TestClass.class);
        final var i2 = registry.getInstance(qualifier, TestClass.class);

        assertThat(i1).isInstanceOf(TestClass.class);
        assertThat(i1).isSameAs(i2);
    }

    @Test
    void onlyRegisterOnce() {
        final var qualifier = TestClass.class.getSimpleName();
        registry.register(qualifier, InstanceRegistryTest::createTestInstance);
        assertThatExceptionOfType(CreatorExistsException.class).isThrownBy(() -> {
            registry.register(qualifier, InstanceRegistryTest::createTestInstance);
        });
    }

    // multilevel

    private static TestClass createTestInstance(InstanceRegistry registry) {
        return new TestClass();
    }

    private static class TestClass {
    }

}