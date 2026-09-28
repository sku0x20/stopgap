package dev.sku20.stopgap.codegen.ast

data class AstCall(
    val receiver: AstExpression?,
    val name: String,
    val args: List<AstExpression>,
) : AstExpression, AstStatement
