package dev.sku20.stopgap.codegen.ast

data class AstBreak(
    val label: AstExpression? = null,
) : AstExpression
