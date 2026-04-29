package dev.sku20.helidon.ksp.registry

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFile
import dev.sku20.helidon.ksp.endpoint.EndpointSymbolProcessor

class RegistrySymbolProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val endpointGeneratedFile = resolver.getNewFiles().findEndpointInitializers()
            ?: return emptyList()
        generateFile(endpointGeneratedFile)
        return emptyList()
    }

    private fun Sequence<KSFile>.findEndpointInitializers(): KSFile? = find {
        it.packageName.asString() == EndpointSymbolProcessor.GENERATED_PACKAGE &&
                it.fileName == "${EndpointSymbolProcessor.GENERATED_FILE_NAME}.${EndpointSymbolProcessor.GENERATED_EXTENSION}"
    }

    companion object {
        const val GENERATED_PACKAGE = "dev.sku20.helidon.registry.generated"
        const val GENERATED_FILE_NAME = "RegistryInitializer"
        const val GENERATED_EXTENSION = "kt"
    }

    private fun generateFile(originatingFile: KSFile) {
        val file = try {
            codeGenerator.createNewFile(
                Dependencies(false, originatingFile),
                GENERATED_PACKAGE,
                GENERATED_FILE_NAME,
                GENERATED_EXTENSION
            )
        } catch (e: FileAlreadyExistsException) {
            return
        }
        file.close()
    }
}
