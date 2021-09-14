package de.tbollmeier.klox

interface StmtVisitor {
    fun visitVarDeclStmt(varDeclStmt: VarDeclStmt)
    fun visitExpressionStmt(expressionStmt: ExpressionStmt)
    fun visitPrintStmt(printStmt: PrintStmt)
    fun visitBlockStmt(blockStmt: BlockStmt)
}

class Program(val statements: List<Stmt>) {

    fun accept(visitor: StmtVisitor) {
        statements.forEach { it.accept(visitor) }
    }

}

sealed class Stmt {

    abstract fun accept(visitor: StmtVisitor)

}

class VarDeclStmt(val name: Token, val initializer: Expr?) : Stmt() {

    override fun accept(visitor: StmtVisitor) {
        visitor.visitVarDeclStmt(this)
    }

}

abstract class NonDeclStmt : Stmt()

class ExpressionStmt(val expression: Expr) : NonDeclStmt() {

    override fun accept(visitor: StmtVisitor) {
        visitor.visitExpressionStmt(this)
    }

}

class PrintStmt(val expression: Expr) : NonDeclStmt() {

    override fun accept(visitor: StmtVisitor) {
        visitor.visitPrintStmt(this)
    }

}

class BlockStmt(val statements: List<Stmt>) : NonDeclStmt() {

    override fun accept(visitor: StmtVisitor) {
        visitor.visitBlockStmt(this)
    }

}
