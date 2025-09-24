package com.example.stopgap.instanceregistry

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock

class InstanceRegistryTest {

    private val config = mock(Config::class.java)
    private val registry = InstanceRegistry(config)

    @Test
    fun forType() {
        registry.registerForType(
            T1::class.java,
            ::createT1
        )
        val t1 = registry.getInstanceForType(T1::class.java)
        assertThat(t1).isInstanceOf(T1::class.java)
    }

    @Test
    fun returnsSameInstance() {
        val qualifier = T1::class.java.getSimpleName()
        registry.registerForQualifier(qualifier, ::createT1)

        val i1 = registry.getInstanceForQualifier(qualifier, T1::class.java)
        val i2 = registry.getInstanceForQualifier(qualifier, T1::class.java)

        assertThat(i1).isInstanceOf(T1::class.java)
        assertThat(i1).isSameAs(i2)
    }

    @Test
    fun onlyRegisterOnce() {
        val qualifier = T1::class.java.getSimpleName()
        registry.registerForQualifier(qualifier, ::createT1)
        assertThatExceptionOfType(CreatorExistsException::class.java).isThrownBy {
            registry.registerForQualifier(
                qualifier,
                ::createT1
            )
        }
    }

    @Test
    fun dependencyHandling() {
        registry.registerForQualifier("t2", ::createT2)
        registry.registerForQualifier("t1", ::createT1)

        val t2 = registry.getInstanceForQualifier("t2", T2::class.java)
        assertThat(t2).isInstanceOf(T2::class.java)
        val t1 = registry.getInstanceForQualifier("t1", T1::class.java)
        assertThat(t1).isSameAs(t2.t1)
    }

    private class T1

    @JvmRecord
    private data class T2(val t1: T1)

    companion object {
        private fun createT1(registry: InstanceRegistry): T1 {
            return T1()
        }

        private fun createT2(registry: InstanceRegistry): T2 {
            val t1 = registry.getInstanceForQualifier<T1>("t1", T1::class.java)
            return T2(t1)
        }
    }
}