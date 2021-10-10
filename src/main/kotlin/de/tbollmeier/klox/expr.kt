package de.tbollmeier.klox

interface ExprVisitor<R> {
    fun visitBinaryExpr(binary: Binary): R
    fun visitAssignExpr(assign: Assign): R
    fun visitGroupingExpr(grouping: Grouping): R
    fun visitLiteralExpr(literal: Literal): R
    fun visitVariable(variable: Variable): R
    fun visitUnaryExpr(unary: Unary): R
    fun visitLogicalExpr(logical: Logical): R
    fun visitCallExpr(call: Call): R
    fun visitFunExpr(fn: FunExpr): R
    fun visitGet(get: Get): R
    fun visitSet(set: Set): R
}

sealed class Expr() {
    abstract fun <R> accept(exprVisitor: ExprVisitor<R>): R
}

class Assign(val name: Token, val value: Expr) : Expr() {
    override fun <R> accept(exprVisitor: ExprVisitor<R>): R {
        return exprVisitor.visitAssignExpr(this)
    }
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

class Logical(val operator: Token, val left: Expr, val right: Expr) : Expr() {
    override fun <R> accept(exprVisitor: ExprVisitor<R>): R {
        return exprVisitor.visitLogicalExpr(this)
    }
}

class Call(val callee: Expr, val closingParen: Token, val arguments: List<Expr>) : Expr() {
    override fun <R> accept(exprVisitor: ExprVisitor<R>): R {
        return exprVisitor.visitCallExpr(this)
    }
}

class FunExpr(val parameters: List<Token>, val block: BlockStmt) : Expr() {
    override fun <R> accept(exprVisitor: ExprVisitor<R>): R {
        return exprVisitor.visitFunExpr(this)
    }
}

class Get(val obj: Expr, val name: Token) : Expr() {
    override fun <R> accept(exprVisitor: ExprVisitor<R>): R {
        return exprVisitor.visitGet(this)
    }
}

class Set(val obj: Expr, val name: Token, val value: Expr) : Expr() {
    override fun <R> accept(exprVisitor: ExprVisitor<R>): R {
        return exprVisitor.visitSet(this)
    }
}