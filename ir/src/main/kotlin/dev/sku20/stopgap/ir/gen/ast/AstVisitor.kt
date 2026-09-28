package dev.sku20.stopgap.ir.gen.ast

interface AstVisitor<R> {
    fun visitFunction(function: AstFunction): R
    fun visitParam(param: AstParam): R
    fun visitType(type: AstType): R
    fun visitAnnotation(annotation: AstAnnotation): R
    fun visitVisibility(visibility: AstVisibility): R
}
