package com.example.stopgap.instanceregistry;

import org.junit.jupiter.api.Test;

final class UtilsTest {

    @Test
    void getGenericType() throws NoSuchMethodException {
        final var creator = new InstanceCreator<UtilsTest>() {
            @Override
            public UtilsTest create(final InstanceRegistry registry) {
                return new UtilsTest();
            }
        };

        Utils.getCreatorTypeViaGenericInterface(creator); // works
//        Utils.getCreatorTypeViaGenericInterface(UtilsTest::uc); // does not work

        Utils.getCreatorTypeViaMethod(creator);
//        Utils.getCreatorTypeViaMethod(UtilsTest::uc); // does not work
    }

    public static UtilsTest uc(final InstanceRegistry registry) {
        return new UtilsTest();
    }

}