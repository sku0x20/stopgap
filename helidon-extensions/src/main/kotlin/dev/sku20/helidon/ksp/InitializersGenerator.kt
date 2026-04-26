package dev.sku20.helidon.ksp

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import dev.sku20.helidon.endpoint.*
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
            val endpointAnnotation =
                endpointClazz.annotations.first { it.shortName.asString() == Endpoint::class.simpleName }
            val endpointQualifiedName = endpointClazz.qualifiedName!!.asString()
            val endpointInstance = "endpoint"
            writeLine("fun registerRoutesFor(")
            withRelativeIndent(4) {
                writeLine("$endpointInstance: ${endpointQualifiedName},")
                writeLine("routes: HttpRouting.Builder,")
                writeLine("serde: Serde = NopSerde,")
            }
            writeLine("){")
            withRelativeIndent(4) {
                registerEndpoint(
                    path(endpointAnnotation),
                    endpointInstance,
                    endpointClazz.getDeclaredFunctions()
                )
            }
            writeLine("}")
        }
    }

    @Suppress("SameParameterValue")
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

    private fun writeRulesFromFunctions(
        endpointInstance: String,
        functions: Sequence<KSFunctionDeclaration>
    ) = w.withRelativeIndent {
        for (function in functions) {
            if (!function.isPublic()) continue
            writeRoute(function, endpointInstance)
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
    // @formatter:off
    fun writeRoute(
        function: KSFunctionDeclaration,
        endpointInstance: String
    ) {
        for (annotation in function.annotations) {
            when (annotation.shortName.asString()) {
                Get::class.simpleName -> writeViaFunctionName("get", annotation, function, endpointInstance)
                Post::class.simpleName -> writeViaFunctionName("post", annotation, function, endpointInstance)
                Delete::class.simpleName -> writeViaFunctionName("delete", annotation, function, endpointInstance)
                Put::class.simpleName -> writeViaFunctionName("put", annotation, function, endpointInstance)
                Patch::class.simpleName -> writeViaFunctionName("patch", annotation, function, endpointInstance)
                Head::class.simpleName -> writeViaFunctionName("head", annotation, function, endpointInstance)
                Options::class.simpleName -> writeViaFunctionName("options", annotation, function, endpointInstance)
                Trace::class.simpleName -> writeViaFunctionName("trace", annotation, function, endpointInstance)
            }
        }
    }
    // @formatter:on

    fun writeViaFunctionName(
        functionName: String,
        annotation: KSAnnotation,
        endpointFunction: KSFunctionDeclaration,
        endpointInstance: String
    ) = w.withRelativeIndent {
        writeLine(".${functionName}(\"${path(annotation)}\", {req, res ->")
        withRelativeIndent(4) {
            writeHandler(endpointFunction, endpointInstance)
        }
        writeLine("})")
    }

    fun writeHandler(
        function: KSFunctionDeclaration,
        endpointInstance: String
    ) = HandlerGenerator(function, endpointInstance, w)
        .write()

    private fun path(annotation: KSAnnotation): String {
        val path = annotation.arguments.first { it.name?.getShortName() == "path" }
        val pathValue = path.value as String
        return pathValue
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