package dev.sku20.stopgap.codegen.ast

data class AstFor(
    val item: AstExpression,
    val iterable: AstExpression,
    val content: List<AstExpression>,
) : AstExpression
