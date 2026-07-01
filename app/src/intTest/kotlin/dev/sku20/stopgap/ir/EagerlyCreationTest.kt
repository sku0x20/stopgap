package dev.sku20.stopgap.ir

import dev.sku20.stopgap.ir.generated.initRegistry
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Duration

class EagerlyCreationTest {

    @Test
    fun eagerly() {
        val registry = InstanceRegistry()
        initRegistry(registry)
        Thread.sleep(10)
        val eagerly = registry.getInstanceForType<EagerlyConfig.Eagerly>()
        assertThat(System.nanoTime() - eagerly.creationTime)
            .isGreaterThan(Duration.ofMillis(10).toNanos())
    }

}