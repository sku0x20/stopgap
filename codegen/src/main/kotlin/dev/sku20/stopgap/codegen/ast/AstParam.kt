package dev.sku20.stopgap.codegen.ast

data class AstParam(
    val name: String,
    val type: AstType,
) : AstExpression
