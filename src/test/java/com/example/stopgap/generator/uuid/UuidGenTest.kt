package com.example.stopgap.generator.uuid

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class UuidGenTest {
    @Test
    fun differentUuids() {
        val uuidGen = UuidGen()
        val uuid1 = uuidGen.generate()
        val uuid2 = uuidGen.generate()
        assertThat(uuid1).isNotEqualTo(uuid2)
    }
}