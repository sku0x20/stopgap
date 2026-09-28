package dev.sku20.stopgap.codegen.ast

data class AstFile(
    val packageName: String,
    val imports: List<AstImport>,
    val content: List<AstDeclarable>
) : AstElement
