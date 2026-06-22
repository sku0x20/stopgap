package dev.sku20.helidon.ksp.endpoint

import com.google.devtools.ksp.symbol.KSClassDeclaration
import dev.sku20.helidon.ksp.CustomWriter
import dev.sku20.helidon.ksp.Utils
import java.io.InputStream
import java.io.OutputStream

class InitializersGenerator(
    file: OutputStream,
    private val endpointClazzes: List<KSClassDeclaration>,
    private val packageName: String
) {
    private val w = CustomWriter(file)

    fun write() {
        val endpointsRoutes = captureEndpointRoutes()

        writePackage()
        writeImports()
        w.withRelativeIndent(4) {
            w.write(endpointsRoutes)
        }
        w.close()
    }

    private fun captureEndpointRoutes(): InputStream = Utils.capturing { writer ->
        for (endpointClazz in endpointClazzes) {
            RoutesGenerator(endpointClazz, imports, writer).write()
        }
    }

    private fun writePackage() = w.withRelativeIndent {
        writeLine("package $packageName")
    }

    private val imports = mutableSetOf<String>()
    private fun writeImports() = w.withRelativeIndent {
        writeLine()
        for (import in imports) writeLine("import $import")
        writeLine()
    }
}