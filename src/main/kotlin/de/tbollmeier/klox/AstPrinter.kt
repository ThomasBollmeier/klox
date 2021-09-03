package de.tbollmeier.klox

class AstPrinter : Visitor<String> {

    fun print(expr: Expr): String {
        return expr.accept(this)
    }

    override fun visitBinaryExpr(binary: Binary): String {
        return "(${binary.operator.lexeme} ${print(binary.left)} ${print(binary.right)})"
    }

    override fun visitGroupingExpr(grouping: Grouping): String {
        return "(group ${print(grouping.expression)})"
    }

    override fun visitLiteralExpr(literal: Literal): String {
        return literal.value?.toString() ?: "nil"
    }

    override fun visitUnaryExpr(unary: Unary): String {
        return "(${unary.operator.lexeme} ${print(unary.right)})"
    }
}