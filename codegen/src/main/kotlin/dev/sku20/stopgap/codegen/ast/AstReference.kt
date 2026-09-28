package dev.sku20.stopgap.codegen.ast

data class AstReference(
    val receiver: AstExpression?,
    val name: String,
) : AstExpression
