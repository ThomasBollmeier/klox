package de.tbollmeier.klox

interface ExprVisitor<R> {
    fun visitBinaryExpr(binary: Binary): R
    fun visitGroupingExpr(grouping: Grouping): R
    fun visitLiteralExpr(literal: Literal): R
    fun visitVariable(variable: Variable): R
    fun visitUnaryExpr(unary: Unary): R
}

sealed class Expr() {
    abstract fun <R> accept(exprVisitor: ExprVisitor<R>): R
}

class Binary(val operator: Token, val left: Expr, val right: Expr) : Expr() {
    override fun <R> accept(exprVisitor: ExprVisitor<R>): R {
        return exprVisitor.visitBinaryExpr(this)
    }
}

class Grouping(val expression: Expr) : Expr() {
    override fun <R> accept(exprVisitor: ExprVisitor<R>): R {
        return exprVisitor.visitGroupingExpr(this)
    }
}

class Literal(val value: Any?) : Expr() {
    override fun <R> accept(exprVisitor: ExprVisitor<R>): R {
        return exprVisitor.visitLiteralExpr(this)
    }
}

class Variable(val name: Token) : Expr() {
    override fun <R> accept(exprVisitor: ExprVisitor<R>): R {
        return exprVisitor.visitVariable(this)
    }
}

class Unary(val operator: Token, val right: Expr) : Expr() {
    override fun <R> accept(exprVisitor: ExprVisitor<R>): R {
        return exprVisitor.visitUnaryExpr(this)
    }
}

