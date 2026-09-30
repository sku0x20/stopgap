package dev.sku20.stopgap.codegen.ast

data class AstCall(
    val name: AstExpression,
    val arguments: List<AstExpression>,
    val receiver: AstExpression? = null,
) : AstExpression
