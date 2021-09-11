package de.tbollmeier.klox

interface StmtVisitor<R> {
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

class ExpressionStmt(val expression: Expr) : Stmt() {

    override fun <R> accept(visitor: StmtVisitor<R>): R {
        return visitor.visitExpressionStmt(this)
    }

}

class PrintStmt(val expression: Expr) : Stmt() {

    override fun <R> accept(visitor: StmtVisitor<R>): R {
        return visitor.visitPrintStmt(this)
    }

}
