package dev.sku20.stopgap.codegen

import dev.sku20.stopgap.codegen.ast.AstAssignment
import dev.sku20.stopgap.codegen.ast.AstCall
import dev.sku20.stopgap.codegen.ast.AstFunction
import dev.sku20.stopgap.codegen.ast.AstImport
import dev.sku20.stopgap.codegen.ast.AstLambda
import dev.sku20.stopgap.codegen.ast.AstLiteral
import dev.sku20.stopgap.codegen.ast.AstParam
import dev.sku20.stopgap.codegen.ast.AstReturn
import dev.sku20.stopgap.codegen.ast.AstStringLiteral
import dev.sku20.stopgap.codegen.ast.AstType

interface AstVisitor {
    fun visitAssignment(node: AstAssignment)
    fun visitCall(node: AstCall)
    fun visitFunction(node: AstFunction)
    fun visitImport(node: AstImport)
    fun visitLambda(node: AstLambda)
    fun visitLiteral(node: AstLiteral)
    fun visitParam(node: AstParam)
    fun visitReturn(node: AstReturn)
    fun visitStringLiteral(node: AstStringLiteral)
    fun visitType(node: AstType)
}