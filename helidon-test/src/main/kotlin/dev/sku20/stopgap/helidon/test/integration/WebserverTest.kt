package dev.sku20.stopgap.helidon.test.integration

import org.junit.jupiter.api.extension.ExtendWith

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ExtendWith(WebserverExtension::class, ClientExtension::class)
annotation class WebserverTest {

    @Target(AnnotationTarget.FUNCTION)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class ConfigServer

    @Target(AnnotationTarget.FUNCTION)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Setup

    @Target(AnnotationTarget.FUNCTION)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Cleanup
}
