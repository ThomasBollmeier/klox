package de.tbollmeier.klox

interface StmtVisitor<R> {
    fun visitVarDeclStmt(varDeclStmt: VarDeclStmt): R
    fun visitExpressionStmt(expressionStmt: ExpressionStmt): R
    fun visitPrintStmt(printStmt: PrintStmt): R
}

class Program(val statements: List<Stmt>) {

    fun <R> accept(visitor: StmtVisitor<R>): R? {
        var ret: R? = null
        for (stmt in statements) {
            ret = stmt.accept(visitor)
        }
        return ret
    }

}

sealed class Stmt {

    abstract fun <R> accept(visitor: StmtVisitor<R>): R

}

class VarDeclStmt(val name: Token, val initializer: Expr?) : Stmt() {
    override fun <R> accept(visitor: StmtVisitor<R>): R {
        return visitor.visitVarDeclStmt(this)
    }
}

abstract class NonDeclStmt(): Stmt()

class ExpressionStmt(val expression: Expr) : NonDeclStmt() {

    override fun <R> accept(visitor: StmtVisitor<R>): R {
        return visitor.visitExpressionStmt(this)
    }

}

class PrintStmt(val expression: Expr) : NonDeclStmt() {

    override fun <R> accept(visitor: StmtVisitor<R>): R {
        return visitor.visitPrintStmt(this)
    }

}
