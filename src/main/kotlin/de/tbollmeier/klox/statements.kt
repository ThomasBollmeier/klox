package de.tbollmeier.klox

interface StmtVisitor {
    fun visitVarDeclStmt(varDeclStmt: VarDeclStmt)
    fun visitExpressionStmt(expressionStmt: ExpressionStmt)
    fun visitPrintStmt(printStmt: PrintStmt)
    fun visitBlockStmt(blockStmt: BlockStmt)
    fun visitIfStmt(ifStmt: IfStmt)
    fun visitWhileStmt(whileStmt: WhileStmt)
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

    private var _hasDecl = false

    val hasDeclarations: Boolean
        get() = _hasDecl

    init {
        for (stmt in statements) {
            if (stmt is VarDeclStmt) {
                _hasDecl = true
                break
            }
        }
    }

    override fun accept(visitor: StmtVisitor) {
        visitor.visitBlockStmt(this)
    }

}

class IfStmt(val condition: Expr, val thenBranch: NonDeclStmt, val elseBranch: NonDeclStmt?) : NonDeclStmt() {

    override fun accept(visitor: StmtVisitor) {
        visitor.visitIfStmt(this)
    }

}

class WhileStmt(val condition: Expr, val statement: NonDeclStmt) : NonDeclStmt() {

    override fun accept(visitor: StmtVisitor) {
        visitor.visitWhileStmt(this)
    }

}
