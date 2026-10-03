package dev.sku20.stopgap.codegen

import dev.sku20.stopgap.codegen.ast.*
import java.io.OutputStream

class AstPrinter(
    private val out: OutputStream,
) : AstVisitor {

    override fun visitAssignment(node: AstAssignment) {
        TODO("Not yet implemented")
    }

    override fun visitCall(node: AstCall) {
        TODO("Not yet implemented")
    }

    override fun visitFile(node: AstFile) {
        TODO("Not yet implemented")
    }

    override fun visitFunction(node: AstFunction) {
        TODO("Not yet implemented")
    }

    override fun visitLambda(node: AstLambda) {
        TODO("Not yet implemented")
    }

    override fun visitLiteral(node: AstLiteral) {
        TODO("Not yet implemented")
    }

    override fun visitParam(node: AstParam) {
        TODO("Not yet implemented")
    }

    override fun visitReturn(node: AstReturn) {
        TODO("Not yet implemented")
    }

    override fun visitStringLiteral(node: AstStringLiteral) {
        TODO("Not yet implemented")
    }

    override fun visitType(node: AstType) {
        TODO("Not yet implemented")
    }

}