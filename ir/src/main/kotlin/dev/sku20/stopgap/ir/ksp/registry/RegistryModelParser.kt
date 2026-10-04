package dev.sku20.stopgap.ir.ksp.registry

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import dev.sku20.stopgap.ir.ksp.creator.CreatorModel
import dev.sku20.stopgap.ir.ksp.creator.CreatorModelParser

class RegistryModelParser(private val functions: List<KSFunctionDeclaration>) {

    fun parse(): RegistryModel {
        val creators = mutableListOf<CreatorModel>()
        for (function in functions) {
            creators.add(CreatorModelParser(function).parse())
        }
        return RegistryModel(creators)
    }
}
