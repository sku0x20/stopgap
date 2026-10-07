package dev.sku20.stopgap.codegen.ast

data class AstThrow(
    val value: AstExpression,
) : AstExpression
