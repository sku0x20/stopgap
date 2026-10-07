package dev.sku20.stopgap.codegen.ast

data class AstAnnotation(
    val name: AstExpression,
    val arguments: List<AstExpression> = emptyList(),
) : AstExpression
