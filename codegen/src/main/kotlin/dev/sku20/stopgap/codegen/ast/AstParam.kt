package dev.sku20.stopgap.codegen.ast

data class AstParam(
    val name: AstExpression,
    val type: AstExpression? = null,
    val annotations: List<AstExpression> = emptyList(),
) : AstExpression
