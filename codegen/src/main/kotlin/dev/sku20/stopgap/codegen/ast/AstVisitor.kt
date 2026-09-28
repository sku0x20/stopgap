package dev.sku20.stopgap.codegen.ast

interface AstVisitor {
    fun visitFile(file: AstFile)
    fun visitImport(import: AstImport)
    fun visitFunction(function: AstFunction)
    fun visitParam(param: AstParam)
    fun visitType(type: AstType)
    fun visitAnnotation(annotation: AstAnnotation)
}
