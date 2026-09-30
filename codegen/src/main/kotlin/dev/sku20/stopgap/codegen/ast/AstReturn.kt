package dev.sku20.stopgap.codegen.ast

data class AstReturn(
    val value: AstExpression? = null,
    val label: String? = null,
) : AstExpression
