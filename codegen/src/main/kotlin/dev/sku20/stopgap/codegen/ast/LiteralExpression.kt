package dev.sku20.stopgap.codegen.ast

data class LiteralExpression<T>(
    val value: T,
) : AstExpression
