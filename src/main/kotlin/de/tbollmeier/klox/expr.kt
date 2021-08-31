package de.tbollmeier.klox

interface Visitor<R> {
    fun visitBinaryExpr(binary: Binary): R
    fun visitGroupingExpr(grouping: Grouping): R
    fun visitLiteralExpr(literal: Literal): R
    fun visitUnaryExpr(unary: Unary): R
}

abstract class Expr() {
    abstract fun <R> accept(visitor: Visitor<R>): R
}

class Binary(val operator: Token, val left: Expr, val right: Expr) : Expr() {
    override fun <R> accept(visitor: Visitor<R>): R {
        return visitor.visitBinaryExpr(this)
    }
}

class Grouping(val expression: Expr) : Expr() {
    override fun <R> accept(visitor: Visitor<R>): R {
        return visitor.visitGroupingExpr(this)
    }
}

class Literal(val value: Any) : Expr() {
    override fun <R> accept(visitor: Visitor<R>): R {
        return visitor.visitLiteralExpr(this)
    }
}

class Unary(val operator: Token, val right: Expr) : Expr() {
    override fun <R> accept(visitor: Visitor<R>): R {
        return visitor.visitUnaryExpr(this)
    }
}

