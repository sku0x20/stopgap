package dev.sku20.helidon.ksp

import com.google.devtools.ksp.symbol.KSClassDeclaration
import java.io.OutputStream

class InitializersGenerator(
    file: OutputStream,
    private val endpointClazzez: List<KSClassDeclaration>,
    private val packageName: String,
    private val useRegistry: Boolean
) {
    private val w = CustomWriter(file)

    fun write() {
        writePackage()
        writeImports()
        if (useRegistry) writeInitEndpointsRoutesViaRegistry()
        writeRegisterRoutesFor()
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

    private fun writeRegisterRoutesFor() = w.withRelativeIndent {
        for (endpointClazz in endpointClazzez) {
            RoutesGenerator(endpointClazz)
                .write(w)
        }
    }

    private fun writePackage() = w.withRelativeIndent {
        writeLine("package $packageName")
    }

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