package dev.sku20.stopgap.codegen

import dev.sku20.stopgap.codegen.ast.*
import java.io.OutputStream

class AstPrinter(
    private val out: OutputStream,
) : AstVisitor {

    override fun visitAssignment(node: AstAssignment) {
        when (node.variableType) {
            VariableType.VAL -> write("val ")
            VariableType.VAR -> write("var ")
            VariableType.LATEINIT_VAR -> write("lateinit var ")
            null -> {}
        }
        visit(node.name)
        if (node.type != null) {
            write(": ")
            visit(node.type)
        }
        if (node.value != null) {
            write(" = ")
            visit(node.value)
        }
    }

    override fun visitCall(node: AstCall) {
        if (node.receiver != null) {
            visit(node.receiver)
            write(".")
        }
        visit(node.name)
        write("(")
        writeCommaSeparated(node.arguments)
        write(")")
    }

    override fun visitFile(node: AstFile) {
        write("package ")
        visit(node.packageName)
        writeln()
        writeln()
        writeSeparated(node.content, "\n\n")
        writeln()
    }

    override fun visitFunction(node: AstFunction) {
        write("fun ")
        write(node.name)
        write("(")
        writeCommaSeparated(node.parameters)
        write(")")
        if (node.returnType != null) {
            write(": ")
            visitType(node.returnType)
        }
        write(" {")
        writeln()
        writeLnSeparated(node.content)
        writeln()
        write("}")
    }

    override fun visitLambda(node: AstLambda) {
        write("{")
        if (node.parameters.isNotEmpty()) {
            write(" ")
            writeCommaSeparated(node.parameters)
            write(" ->")
        }
        writeln()
        writeLnSeparated(node.content)
        writeln()
        write("}")
    }

    override fun visitLiteral(node: AstLiteral) {
        write(node.value)
    }

    override fun visitParam(node: AstParam) {
        visit(node.name)
        if (node.type != null) {
            write(": ")
            visit(node.type)
        }
    }

    override fun visitReturn(node: AstReturn) {
        write("return")
        if (node.label != null) {
            write("@")
            visit(node.label)
        }
        if (node.value != null) {
            write(" ")
            visit(node.value)
        }
    }

    override fun visitStringLiteral(node: AstStringLiteral) {
        write("\"")
        write(node.value)
        write("\"")
    }

    override fun visitType(node: AstType) {
        write(node.fqn)
        if (node.nullable) {
            write("?")
        }
    }

    private fun writeCommaSeparated(items: List<AstExpression>) = writeSeparated(items, ", ")

    private fun writeLnSeparated(items: List<AstExpression>) = writeSeparated(items, "\n")

    private fun writeSeparated(
        items: List<AstExpression>,
        separator: String
    ) {
        if (items.isNotEmpty()) {
            visit(items[0])
            for (index in 1 until items.size) {
                write(separator)
                visit(items[index])
            }
        }
    }

    private fun write(text: String) {
        out.write(text.toByteArray())
    }

    private fun writeln() {
        write("\n")
    }

}
