package dev.sku20.stopgap.helidon.authentication

import dev.sku20.stopgap.helidon.ksp.authentication.AuthenticationInitializerGenerator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream

class AuthenticationInitializerGeneratorTest {

    @Test
    fun writesInitAuthenticationFunction() {
        val output = ByteArrayOutputStream()
        val generator = AuthenticationInitializerGenerator(output)

        generator.write()

        val generated = output.toString()
        assertThat(generated).contains("package dev.sku20.stopgap.helidon.authentication.generated")
        assertThat(generated).contains("import dev.sku20.stopgap.helidon.authentication.AuthenticationFilter")
        assertThat(generated).contains("import dev.sku20.stopgap.helidon.authentication.AuthenticationResolver")
        assertThat(generated).contains("import io.helidon.webserver.http.HttpRouting")
        assertThat(generated).contains("fun initAuthentication(")
        assertThat(generated).contains("resolver: AuthenticationResolver,")
        assertThat(generated).contains("routes: HttpRouting.Builder,")
        assertThat(generated).contains("routes.addFilter(AuthenticationFilter(resolver))")
    }
}
