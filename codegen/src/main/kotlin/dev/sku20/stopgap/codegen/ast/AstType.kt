package dev.sku20.stopgap.codegen.ast

data class AstType(
    val fqn: String,
    val nullable: Boolean = false,
) : AstExpression
