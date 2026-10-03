package dev.sku20.stopgap.codegen.ast

data class AstIf(
    val condition: AstExpression,
    val content: List<AstExpression>,
    val elseContent: List<AstExpression> = emptyList(),
) : AstExpression
