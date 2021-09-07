package de.tbollmeier.klox

class Interpreter : Visitor<Value> {

    fun evaluate(expression: Expr) = expression.accept(this)

    override fun visitBinaryExpr(binary: Binary): Value {
        TODO("Not yet implemented")
    }

    override fun visitGroupingExpr(grouping: Grouping): Value {
        TODO("Not yet implemented")
    }

    override fun visitLiteralExpr(literal: Literal): Value {
        TODO("Not yet implemented")
    }

    override fun visitUnaryExpr(unary: Unary): Value {
        TODO("Not yet implemented")
    }
}