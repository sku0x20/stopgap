package dev.sku20.stopgap.ir.gen.ast

interface AstVisitor {
    fun visitFunction(function: AstFunction)
    fun visitParam(param: AstParam)
    fun visitType(type: AstType)
    fun visitAnnotation(annotation: AstAnnotation)
}
