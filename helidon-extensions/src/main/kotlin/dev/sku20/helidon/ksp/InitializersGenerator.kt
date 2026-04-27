package dev.sku20.helidon.ksp

import com.google.devtools.ksp.symbol.KSClassDeclaration
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
        val registryFunction = captureRegistryFunction()

        writePackage()
        writeImports()
        w.write(registryFunction)
        w.withRelativeIndent(4) {
            w.write(endpointsRoutes)
        }
        w.close()
    }

    private fun captureRegistryFunction(): InputStream = Utils.capturing { writer ->
        if (!useRegistry) return@capturing
        val rg = RegistryInitializerGenerator(endpointClazzez)
        imports.addAll(rg.imports())
        rg.write(writer)
    }

    private fun captureEndpointRoutes(): InputStream = Utils.capturing { writer ->
        for (endpointClazz in endpointClazzez) {
            val rg = RoutesGenerator(endpointClazz)
            rg.write(writer)
            imports.addAll(rg.imports())
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