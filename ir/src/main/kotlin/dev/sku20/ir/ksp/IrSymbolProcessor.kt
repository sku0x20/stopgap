package dev.sku20.ir.ksp

import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import dev.sku20.ir.Creates

class IrSymbolProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    private val packageName = "dev.sku20.ir.generated"

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(Creates::class.qualifiedName!!)
        val symbolsList = symbols.toList()
        if (symbolsList.isEmpty()) return emptyList()

        val file = codeGenerator.createNewFile(
            Dependencies(
                true,
                *symbolsList.map { it.containingFile!! }.toTypedArray()
            ),
            packageName,
            "IrInitCreators",
            "kt"
        )
        val writer = file.bufferedWriter()

        for (symbol in symbols) {
            logger.info("Processing symbol: ${symbol.location}")
        }

        writer.close()

        return emptyList()
    }

}