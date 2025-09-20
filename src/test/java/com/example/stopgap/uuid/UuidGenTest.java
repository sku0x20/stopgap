package com.example.stopgap.uuid;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

final class UuidGenTest {

    @Test
    void differentUuids() {
        final var uuidGen = new UuidGen();
        final var uuid1 = uuidGen.generate();
        final var uuid2 = uuidGen.generate();
        assertThat(uuid1).isNotEqualTo(uuid2);
    }

}