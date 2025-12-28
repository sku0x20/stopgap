package dev.sku20.helidon.endpoint.ksp

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import dev.sku20.helidon.endpoint.Endpoint
import dev.sku20.helidon.endpoint.Get
import dev.sku20.helidon.endpoint.Post
import java.io.OutputStream

class InitializersGenerator(
    file: OutputStream,
    private val symbols: List<KSClassDeclaration>,
    private val packageName: String
) {
    private val w = CustomWriter(file)

    fun write() {
        writeFileSuppressors()
        writePackage()
        writeImports()
        writeFunInitEndpointRoutes()
        w.close()
    }

    private fun writeFunInitEndpointRoutes() = w.withRelativeIndent {
        writeLine("fun initEndpointRoutes(routes: HttpRouting.Builder, registry: InstanceRegistry) {")
        withRelativeIndent(4) {
            writeRegisterRoutes()
        }
        writeLine("}")
    }

    private fun writeRegisterRoutes() = w.withRelativeIndent {
        for (clazz in symbols) {
            val variableName = clazz.simpleName.asString()
            val qualifiedName = clazz.qualifiedName!!.asString()
            val endpointAnnotation = clazz.annotations.first { it.shortName.asString() == Endpoint::class.simpleName }
            val path = endpointAnnotation.arguments.first { it.name?.getShortName() == "path" }
            val pathValue = path.value as String
            writeLine("val $variableName = registry.getInstanceForType<$qualifiedName>()")
            writeLine("routes.register(\"${pathValue}\", { rules ->")
            withRelativeIndent(4) {
                writeLine("rules")
                withRelativeIndent(4) {
                    writeRules(variableName, clazz.getDeclaredFunctions())
                }
            }
            writeLine("})")
        }
    }

    private fun writeRules(
        variableName: String,
        functions: Sequence<KSFunctionDeclaration>
    ) = w.withRelativeIndent {
        for (function in functions) {
            if (!function.isPublic()) continue
            val httpMethod = getHttpMethodMappingViaAnnotation(function) ?: continue
            writeLine(".${httpMethod.method}(\"${httpMethod.path}\", $variableName::${function.simpleName.asString()})")
        }
    }


    // todo: make it more generic with inherited/meta annotation, etc.
    // try to avoid updating here also when adding new method.
    private val mapping = mapOf(
        Get::class.simpleName!! to "get",
        Post::class.simpleName!! to "post"
    )

    data class HttpMethodMapping(
        val method: String,
        val path: String
    )

    private fun getHttpMethodMappingViaAnnotation(function: KSFunctionDeclaration): HttpMethodMapping? {
        for (annotation in function.annotations) {
            val shortName = annotation.shortName.asString()
            val method = mapping[shortName] ?: continue
            val path = annotation.arguments.first { it.name?.getShortName() == "path" }
            val pathValue = path.value as String
            HttpMethodMapping(method, pathValue)
        }
        return null
    }

    private fun writeFileSuppressors() = w.withRelativeIndent {
        writeLine("@file:Suppress(\"LocalVariableName\")")
        writeLine()
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