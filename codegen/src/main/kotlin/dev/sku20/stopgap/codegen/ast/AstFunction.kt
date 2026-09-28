package dev.sku20.stopgap.codegen.ast

data class AstFunction (
    val name: String,
    val visibility: AstVisibility,
    val params: List<AstParam>,
    val returnType: AstType,
    val content: List<AstStatement>,
) : AstDeclarable, AstStatement
