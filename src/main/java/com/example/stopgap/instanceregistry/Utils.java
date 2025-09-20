package com.example.stopgap.instanceregistry;

import java.lang.reflect.ParameterizedType;

public final class Utils {

    private Utils() {
    }

    public static void getCreatorTypeViaGenericInterface(
        final InstanceCreator<?> creator
    ) {
        final var clazz = creator.getClass();
        final var interfaces = clazz.getGenericInterfaces();
        final var parameterizedType = (ParameterizedType) interfaces[0];
        final var typeArguments = parameterizedType.getActualTypeArguments();
        final var typeClazz = (Class<?>) typeArguments[0];
        System.err.println(typeClazz.toGenericString());
    }

    public static void getCreatorTypeViaMethod(
        final InstanceCreator<?> creator
    ) throws NoSuchMethodException {
        final var clazz = creator.getClass();
        final var method = clazz.getMethod("create", InstanceRegistry.class);
        final var type = (Class<?>) method.getGenericReturnType();
        System.err.println(type.toGenericString());
    }

}
