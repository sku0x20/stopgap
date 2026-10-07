package dev.sku20.stopgap.helidon.ksp.registry

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import dev.sku20.stopgap.codegen.AstPrinter
import dev.sku20.stopgap.helidon.ksp.route.GeneratedNames as EndpointGeneratedNames

class RegistrySymbolProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (!isEnabled()) return emptyList()
        val endpointGeneratedFile = findEndpointRoutesFile(resolver.getNewFiles())
            ?: return emptyList()
        generateFile(endpointGeneratedFile)
        return emptyList()
    }

    private fun isEnabled(): Boolean =
        options["stopgap.codegen.endpoint.registry.enabled"]?.toBoolean() ?: true

    private fun findEndpointRoutesFile(files: Sequence<KSFile>): KSFile? = files.find {
        it.packageName.asString() == EndpointGeneratedNames.PACKAGE &&
                it.fileName == "${EndpointGeneratedNames.FILE_NAME}.${EndpointGeneratedNames.EXTENSION}"
    }

    private fun generateFile(originatingFile: KSFile) {
        val functions = originatingFile.declarations.filterIsInstance<KSFunctionDeclaration>().toList()
        val model = RegistryModelParser(functions).parse()
        val astFile = RegistryModelAstGen(model, GeneratedNames.PACKAGE).file()
        val file = codeGenerator.createNewFile(
            Dependencies(false, originatingFile),
            GeneratedNames.PACKAGE,
            GeneratedNames.FILE_NAME,
            GeneratedNames.EXTENSION
        )
        file.buffered().use { out ->
            AstPrinter(out).visitFile(astFile)
        }
    }
}
