package dev.sku20.stopgap.codegen.ast

data class AstFunction(
    val name: String,
    val parameters: List<AstParam>,
    val content: List<AstExpression>,
    val returnType: AstType? = null,
) : AstExpression