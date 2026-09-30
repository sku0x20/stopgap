package dev.sku20.stopgap.codegen.ast

data class AstLiteral<T>(
    val value: T,
) : AstExpression
