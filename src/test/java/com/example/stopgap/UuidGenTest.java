package com.example.stopgap;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

final class UuidGenTest {

    @Test
    void differentUuids() {
        final var uuid1 = UuidGen.generate();
        final var uuid2 = UuidGen.generate();
        assertThat(uuid1).isNotEqualTo(uuid2);
    }

}