package dev.sku20.stopgap.codegen.ast

data class AstType(
    val fqn: String,
    val typeArguments: List<AstExpression> = emptyList(),
    val nullable: Boolean = false,
) : AstExpression
