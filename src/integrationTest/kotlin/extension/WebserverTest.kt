package extension

import com.example.stopgap.Endpoint
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ExtendWith(WebserverTestExtension::class)
annotation class WebserverTest {

    @Target(AnnotationTarget.FUNCTION)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class SetupInstanceRegistry(val endpoint: KClass<out Endpoint> = Endpoint::class)

    @Target(AnnotationTarget.FUNCTION)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class CreateConfig

    @Target(AnnotationTarget.FUNCTION)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class ConfigServer

}
