package dev.sku20.stopgap.codegen.ast

data class AstType(
    val short: String,
    val fqn: String,
) : AstExpression 