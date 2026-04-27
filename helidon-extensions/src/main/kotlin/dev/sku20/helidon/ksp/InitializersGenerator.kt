package dev.sku20.helidon.ksp

import com.google.devtools.ksp.symbol.KSClassDeclaration
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

class InitializersGenerator(
    file: OutputStream,
    private val endpointClazzez: List<KSClassDeclaration>,
    private val packageName: String,
    private val useRegistry: Boolean
) {
    private val w = CustomWriter(file)

    fun write() {
        val endpointsRoutes = captureEndpointRoutes()

        writePackage()
        writeImports()
        if (useRegistry) writeInitEndpointsRoutesViaRegistry()
        w.withRelativeIndent(4) {
            w.write(endpointsRoutes)
        }
        w.close()
    }

    private fun writeInitEndpointsRoutesViaRegistry() = w.withRelativeIndent {
        writeLine("fun initEndpointsRoutesViaRegistry(registry: InstanceRegistry, routes: HttpRouting.Builder) {")
        withRelativeIndent(4) {
            for (endpointClazz in endpointClazzez) {
                val endpointQualifiedName = endpointClazz.qualifiedName!!.asString()
                writeLine("registerRoutesFor(")
                withRelativeIndent(4) {
                    writeLine("registry.getInstanceForType<$endpointQualifiedName>(),")
                    writeLine("routes")
                }
                writeLine(")")
            }
        }
        writeLine("}")
    }

    private fun captureEndpointRoutes(): InputStream {
        val buffer = ByteArrayOutputStream()
        val writer = CustomWriter(buffer)
        for (endpointClazz in endpointClazzez) {
            val rg = RoutesGenerator(endpointClazz)
            rg.write(writer)
            imports.addAll(rg.imports())
        }
        writer.close()
        return buffer.toByteArray().inputStream()
    }

    private fun writePackage() = w.withRelativeIndent {
        writeLine("package $packageName")
    }

    private val imports = mutableSetOf<String>()
    private fun writeImports() = w.withRelativeIndent {
        writeLine()
        writeLine("import dev.sku20.ir.InstanceRegistry")
        writeLine("import dev.sku20.helidon.serde.NopSerde")
        writeLine("import dev.sku20.helidon.serde.Serde")
        writeLine("import io.helidon.webserver.http.HttpRouting")
        writeLine("import io.helidon.webserver.http.HttpRules")
        writeLine()
    }
}