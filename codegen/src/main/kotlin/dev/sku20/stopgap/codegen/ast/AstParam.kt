package dev.sku20.stopgap.codegen.ast

data class AstParam(
    val name: AstExpression,
    val type: AstType? = null,
) : AstExpression
