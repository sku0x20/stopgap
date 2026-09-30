package dev.sku20.stopgap.codegen.ast

data class AstLambda(
    val parameters: List<String>,
    val content: List<AstExpression>,
) : AstExpression
