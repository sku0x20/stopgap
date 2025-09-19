package com.example.stopgap.instanceregistry;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

final class InstanceRegistryTest {

    private final InstanceRegistry registry = new InstanceRegistry();

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

    @Test
    void dependencyHandling() {
        registry.register("t2", InstanceRegistryTest::createT2);
        registry.register("t1", InstanceRegistryTest::createTestInstance);

        final var t2 = registry.getInstance("t2", T2.class);
        assertThat(t2).isInstanceOf(T2.class);
        final var t1 = registry.getInstance("t1", TestClass.class);
        assertThat(t1).isSameAs(t2.t1);
    }

    private static TestClass createTestInstance(final InstanceRegistry registry) {
        return new TestClass();
    }

    @SuppressWarnings("EmptyClass")
    private static class TestClass {
    }

    private static T2 createT2(final InstanceRegistry registry) {
        final var t1 = registry.getInstance("t1", TestClass.class);
        return new T2(t1);
    }

    private record T2(TestClass t1) {
    }

}