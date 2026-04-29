package dev.sku20.helidon.ksp.registry

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import dev.sku20.helidon.ksp.CustomWriter
import dev.sku20.helidon.ksp.Utils
import java.io.InputStream
import java.io.OutputStream

class RegistryInitializerGenerator(
    file: OutputStream,
    private val functions: List<KSFunctionDeclaration>,
    private val packageName: String
) {
    private val w = CustomWriter(file)
    private val imports = mutableSetOf<String>()
    private lateinit var cw: CustomWriter

    fun write() {
        val content = captureContent()
        writePackage()
        writeImports()
        w.write(content)
        w.close()
    }

    private fun captureContent(): InputStream = Utils.capturing { writer ->
        cw = writer
        addImports()
        writeFunctionDefinition()
        cw.withRelativeIndent(4) {
            for (function in functions) writeRegisterCallFor(function)
        }
        cw.writeLine("}")
    }

    private fun addImports() {
        imports.add("dev.sku20.ir.InstanceRegistry")
        imports.add("io.helidon.webserver.http.HttpRouting")
    }

    private val registry = "registry"
    private val routes = "routes"

    private fun writeFunctionDefinition() = cw.withRelativeIndent {
        writeLine("fun initEndpointsRoutesViaRegistry(")
        withRelativeIndent(4) {
            writeLine("$registry: InstanceRegistry,")
            writeLine("$routes: HttpRouting.Builder,")
        }
        writeLine(") {")
    }

    private fun writeRegisterCallFor(function: KSFunctionDeclaration) = cw.withRelativeIndent {
        val endpointQualifiedName = function.parameters.first().type.resolve().declaration.qualifiedName!!.asString()
        writeLine("registerRoutesFor(")
        withRelativeIndent(4) {
            writeLine("$registry.getInstanceForType<$endpointQualifiedName>(),")
            writeLine(routes)
        }
        writeLine(")")
    }

    private fun writePackage() = w.withRelativeIndent {
        writeLine("package $packageName")
    }

    private fun writeImports() = w.withRelativeIndent {
        writeLine()
        for (import in imports) writeLine("import $import")
        writeLine()
    }
}
