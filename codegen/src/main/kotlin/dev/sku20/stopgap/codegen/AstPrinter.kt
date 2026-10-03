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
        writeTypeArguments(node.typeArguments)
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
        if (node.typeArguments.isNotEmpty()) {
            writeTypeArguments(node.typeArguments)
            write(" ")
        }
        write(node.name)
        write("(")
        writeCommaSeparated(node.parameters)
        write(")")
        if (node.returnType != null) {
            write(": ")
            visit(node.returnType)
        }
        write(" ")
        writeBlock(node.content)
    }

    override fun visitIf(node: AstIf) {
        write("if (")
        visit(node.condition)
        write(") ")
        writeBlock(node.content)
        for (astElse in node.astElse) {
            write(" ")
            visit(astElse)
        }
    }

    override fun visitElse(node: AstElse) {
        write("else ")
        if (node.condition != null) {
            write("if (")
            visit(node.condition)
            write(") ")
        }
        writeBlock(node.content)
    }

    private fun writeBlock(content: List<AstExpression>) {
        write("{")
        writeln()
        writeLnSeparated(content)
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
        writeTypeArguments(node.typeArguments)
        if (node.nullable) {
            write("?")
        }
    }

    override fun visitWhen(node: AstWhen) {
        write("when ")
        if (node.subject != null) {
            write("(")
            visit(node.subject)
            write(") ")
        }
        write("{")
        writeln()
        writeLnSeparated(node.branches)
        if (node.elseContent.isNotEmpty()) {
            if (node.branches.isNotEmpty()) {
                writeln()
            }
            write("else")
            writeBranchContent(node.elseContent)
        }
        writeln()
        write("}")
    }

    override fun visitWhenBranch(node: AstWhenBranch) {
        writeCommaSeparated(node.conditions)
        writeBranchContent(node.content)
    }

    private fun writeBranchContent(content: List<AstExpression>) {
        write(" -> ")
        if (content.size == 1) {
            visit(content[0])
        } else {
            writeBlock(content)
        }
    }

    private fun writeTypeArguments(items: List<AstExpression>) {
        if (items.isNotEmpty()) {
            write("<")
            writeCommaSeparated(items)
            write(">")
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
