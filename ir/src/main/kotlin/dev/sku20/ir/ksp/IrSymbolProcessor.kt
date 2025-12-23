package dev.sku20.ir.ksp

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import dev.sku20.ir.Creates
import java.time.Instant

class IrSymbolProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    private val packageName = "dev.sku20.ir.generated"
    private val fileName = "IrInitCreators"
    private val file = codeGenerator.createNewFile(
        Dependencies(true),
        packageName,
        fileName,
        "txt"
    )

    @Suppress("UNCHECKED_CAST")
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver
            .getSymbolsWithAnnotation(Creates::class.qualifiedName!!)
            .toList()
        if (symbols.isEmpty()) return emptyList()

        codeGenerator.associateWithFunctions(
            symbols as List<KSFunctionDeclaration>,
            packageName,
            fileName,
            "txt"
        )

        val writer = file.bufferedWriter()

        for (symbol in symbols) {
            logger.info("Processing symbol: ${symbol.location}")
            writer.write("${Instant.now()} Processing symbol: ${symbol.location} \n")
        }

        writer.close()

        return emptyList()
    }

}