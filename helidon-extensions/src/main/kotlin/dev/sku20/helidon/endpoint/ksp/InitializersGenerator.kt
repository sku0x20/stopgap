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
        writeFunInitEndpointRoutes()
        w.close()
    }

    private fun writeFunInitEndpointRoutes() = w.withRelativeIndent {
        writeLine("fun initEndpointRoutes(routes: HttpRouting.Builder, registry: InstanceRegistry) {")
        withRelativeIndent(4) {
            writeRoutesRegister()
        }
        writeLine("}")
    }

    private fun writeRoutesRegister() = w.withRelativeIndent {
        for (symbol in symbols) {
            val variableName = symbol.simpleName.asString()
            val qualifiedName = symbol.qualifiedName!!.asString()
            writeLine("val $variableName = registry.getInstanceForType<$qualifiedName>()")
        }
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