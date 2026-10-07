package dev.sku20.stopgap.ir.ksp.creator

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSValueParameter
import dev.sku20.stopgap.ir.annotation.Creates
import dev.sku20.stopgap.ir.annotation.Qualifier

class CreatorModelParser(private val function: KSFunctionDeclaration) {

    fun parse(): CreatorModel {
        val parameters = mutableListOf<CreatorModel.Parameter>()
        for (param in function.parameters) {
            parameters.add(parseParameter(param))
        }
        return CreatorModel(
            function.qualifiedName!!.asString(),
            qualifiedTypeName(function.returnType!!),
            qualifierValue(function.annotations),
            eagerly(),
            parameters
        )
    }

    private fun parseParameter(param: KSValueParameter): CreatorModel.Parameter =
        CreatorModel.Parameter(
            qualifiedTypeName(param.type),
            qualifierValue(param.annotations)
        )

    private fun eagerly(): Boolean {
        val annotation = function.annotations.first { qualifiedName(it) == Creates::class.qualifiedName }
        return annotation.arguments.first { it.name?.getShortName() == "eagerly" }.value as Boolean
    }

    private fun qualifierValue(annotations: Sequence<KSAnnotation>): String? {
        val qualifier = annotations.firstOrNull { qualifiedName(it) == Qualifier::class.qualifiedName }
        if (qualifier == null) return null
        return qualifier.arguments.first { it.name?.getShortName() == "value" }.value as String
    }

    private fun qualifiedTypeName(type: KSTypeReference): String =
        type.resolve().declaration.qualifiedName!!.asString()

    private fun qualifiedName(annotation: KSAnnotation): String? =
        annotation.annotationType.resolve().declaration.qualifiedName?.asString()
}
