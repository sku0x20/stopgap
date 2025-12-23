package dev.sku20.ir.ksp

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import dev.sku20.ir.Creates

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
        "kt"
    )

//    private val initGen = InitCreatorsGen(
//        packageName,
//        fileName,
//        file
//    )

    @Suppress("UNCHECKED_CAST")
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver
            .getSymbolsWithAnnotation(Creates::class.qualifiedName!!)
            .toList() as List<KSFunctionDeclaration>

        if (symbols.isEmpty()) {
            val writer = file.bufferedWriter()
            writer.write("// no IrInitCreators")
//            writer.close()
//            initGen.finish()
            return emptyList()
        }

        codeGenerator.associateWithFunctions(
            symbols,
            packageName,
            fileName,
            "kt"
        )

        return emptyList()
    }

}