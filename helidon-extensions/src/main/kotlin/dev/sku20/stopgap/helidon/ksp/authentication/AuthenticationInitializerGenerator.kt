package dev.sku20.stopgap.helidon.ksp.authentication

import dev.sku20.stopgap.helidon.ksp.CustomWriter
import java.io.OutputStream

class AuthenticationInitializerGenerator(
    file: OutputStream,
    private val packageName: String = GeneratedNames.PACKAGE
) {
    private val w = CustomWriter(file)

    fun write() {
        writePackage()
        writeImports()
        writeFunction()
        w.close()
    }

    private fun writePackage() = w.withRelativeIndent {
        writeLine("package $packageName")
    }

    private fun writeImports() = w.withRelativeIndent {
        writeLine()
        writeLine("import dev.sku20.stopgap.helidon.authentication.AuthenticationFilter")
        writeLine("import dev.sku20.stopgap.helidon.authentication.AuthenticationResolver")
        writeLine("import io.helidon.webserver.http.HttpRouting")
        writeLine()
    }

    private fun writeFunction() = w.withRelativeIndent {
        writeLine("fun initAuthentication(")
        withRelativeIndent(4) {
            writeLine("${GeneratedNames.RESOLVER}: AuthenticationResolver,")
            writeLine("${GeneratedNames.ROUTES}: HttpRouting.Builder,")
        }
        writeLine(") {")
        withRelativeIndent(4) {
            writeLine("${GeneratedNames.ROUTES}.addFilter(AuthenticationFilter(${GeneratedNames.RESOLVER}))")
        }
        writeLine("}")
    }
}
