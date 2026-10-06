package dev.sku20.stopgap.codegen.ast

data class AstType(
    val fqn: String,
    val typeArguments: List<AstExpression> = emptyList(),
    val isNullable: Boolean = false,
) : AstExpression
