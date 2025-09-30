package extension

import org.junit.jupiter.api.extension.ExtendWith

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ExtendWith(WebserverTestExtension::class)
annotation class WebserverTest {

    @Target(AnnotationTarget.FUNCTION)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class SetupInstanceRegistry

    @Target
    @Retention(AnnotationRetention.RUNTIME)
    annotation class CreateConfig

}
