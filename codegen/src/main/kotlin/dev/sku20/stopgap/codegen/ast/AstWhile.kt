package dev.sku20.stopgap.codegen.ast

data class AstWhile(
    val condition: AstExpression,
    val content: List<AstExpression>,
    val label: AstExpression? = null,
) : AstExpression
