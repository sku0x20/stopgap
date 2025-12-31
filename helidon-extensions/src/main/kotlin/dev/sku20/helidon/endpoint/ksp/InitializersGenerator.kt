package dev.sku20.helidon.endpoint.ksp

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.symbol.KSAnnotation
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
        writeFunInitEndpointsRoutes()
        w.close()
    }

    private fun writeFunInitEndpointsRoutes() = w.withRelativeIndent {
        writeLine("fun initEndpointsRoutes(routes: HttpRouting.Builder, registry: InstanceRegistry) {")
        withRelativeIndent(4) {
            for (endpointClazz in symbols) {
                val endpointInstance = endpointClazz.simpleName.asString()
                val qualifiedName = endpointClazz.qualifiedName!!.asString()
                writeLine("val $endpointInstance = registry.getInstanceForType<$qualifiedName>()")
                val functions = endpointClazz.getDeclaredFunctions()
                val endpointAnnotation =
                    endpointClazz.annotations.first { it.shortName.asString() == Endpoint::class.simpleName }
                val endpointPath = endpointAnnotation.arguments.first { it.name?.getShortName() == "path" }
                val endpointPathValue = endpointPath.value as String
                registerEndpoint(endpointPathValue, endpointInstance, functions)
            }
        }
        writeLine("}")
    }

    private fun registerEndpoint(
        endpointPath: String,
        endpointInstance: String,
        functions: Sequence<KSFunctionDeclaration>
    ) = w.withRelativeIndent {
        writeLine("routes.register(\"${endpointPath}\", { rules ->")
        withRelativeIndent(4) {
            writeLine("rules")
            withRelativeIndent(4) {
                writeRulesFromFunctions(endpointInstance, functions)
            }
        }
        writeLine("})")
    }

    // todo: add route support; which takes in Method

    private fun writeRulesFromFunctions(
        endpointInstance: String,
        functions: Sequence<KSFunctionDeclaration>
    ) = w.withRelativeIndent {
        for (function in functions) {
            if (!function.isPublic()) continue
            val handler = "$endpointInstance::${function.simpleName.asString()}"
            writeRoute(function.annotations, handler)
        }
    }

    /**
     * fine with switch here.
     * - internal impl; decoupled from client annotations
     * - flexible; enable easy change later if required
     * - mapping needs to be somewhere, either
     *  - raw switch;
     *  - hashmap lookup;
     *  - or some kind of indirection, via factory or other techniques.
     */
    fun writeRoute(
        annotations: Sequence<KSAnnotation>,
        handler: String
    ) {
        for (annotation in annotations) {
            when (annotation.shortName.asString()) {
                Get::class.simpleName -> writeDirectMethodCall("get", annotation, handler)
                Post::class.simpleName -> writeDirectMethodCall("post", annotation, handler)
            }
        }
    }

    fun writeDirectMethodCall(
        method: String,
        annotation: KSAnnotation,
        handler: String
    ) = w.withRelativeIndent {
        val path = annotation.arguments.first { it.name?.getShortName() == "path" }
        val pathValue = path.value as String
        writeLine(".${method}(\"${pathValue}\", ${handler})")
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