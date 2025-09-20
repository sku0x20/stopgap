package com.example.stopgap.instanceregistry;

import org.junit.jupiter.api.Test;

final class UtilsTest {

    @Test
    void getCreatorType() {
        final var creator = new InstanceCreator<UtilsTest>() {
            @Override
            public UtilsTest create(final InstanceRegistry registry) {
                return new UtilsTest();
            }
        };

        Utils.getCreatorType(creator); // works
//        Utils.getCreatorType(UtilsTest::uc); // does not work
    }

    public static UtilsTest uc(final InstanceRegistry registry) {
        return new UtilsTest();
    }

}