package dev.sku20.stopgap.codegen.ast

data class AstContinue(
    val label: AstExpression? = null,
) : AstExpression
