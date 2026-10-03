package dev.sku20.stopgap.codegen.ast

data class AstIf(
    val condition: AstExpression,
    val content: List<AstExpression>,
    val astElse: List<AstElse> = emptyList(),
) : AstExpression

data class AstElse(
    val condition: AstExpression? = null,
    val content: List<AstExpression> = emptyList(),
) : AstExpression