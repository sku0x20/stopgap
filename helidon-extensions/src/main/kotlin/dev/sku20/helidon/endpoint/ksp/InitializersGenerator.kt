package dev.sku20.helidon.endpoint.ksp

import com.google.devtools.ksp.symbol.KSClassDeclaration
import java.io.OutputStream

class InitializersGenerator(
    file: OutputStream,
    private val symbols: List<KSClassDeclaration>,
    private val packageName: String
) {
    private val w = CustomWriter(file)

    fun write() {
        writePackage()
        writeImports()
        w.close()
    }

    private fun writePackage() = w.withRelativeIndent {
        writeLine("package $packageName")
    }

    private fun writeImports() = w.withRelativeIndent {
        writeLine()
        writeLine("import dev.sku20.ir.InstanceRegistry")
        writeLine("import io.helidon.webserver.http.HttpRouting")
        writeLine("import io.helidon.webserver.http.HttpRules")
        writeLine()
    }
}