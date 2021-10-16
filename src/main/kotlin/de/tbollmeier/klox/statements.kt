package de.tbollmeier.klox

interface StmtVisitor {
    fun visitVarDeclStmt(varDeclStmt: VarDeclStmt)
    fun visitClassStmt(classStmt: ClassStmt)
    fun visitExpressionStmt(expressionStmt: ExpressionStmt)
    fun visitPrintStmt(printStmt: PrintStmt)
    fun visitBlockStmt(blockStmt: BlockStmt)
    fun visitIfStmt(ifStmt: IfStmt)
    fun visitWhileStmt(whileStmt: WhileStmt)
    fun visitForStmt(forStmt: ForStmt)
    fun visitBreakStmt(breakStmt: BreakStmt)
    fun visitContinueStmt(continueStmt: ContinueStmt)
    fun visitReturnStmt(returnStmt: ReturnStmt)
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

enum class MethodCategory {
    CLASS_METHOD,
    INSTANCE_METHOD,
    GETTER
}

data class Method(val name: Token, val funExpr: FunExpr, val category: MethodCategory)

class ClassStmt(val name: Token, val methods: List<Method>) : Stmt() {
    override fun accept(visitor: StmtVisitor) {
        visitor.visitClassStmt(this)
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

class ForStmt(
    val initializer: Stmt?,
    val condition: Expr?,
    val increment: Expr?,
    val statement: NonDeclStmt
) : NonDeclStmt() {

    override fun accept(visitor: StmtVisitor) {
        visitor.visitForStmt(this)
    }

}

class BreakStmt: NonDeclStmt() {
    override fun accept(visitor: StmtVisitor) {
        visitor.visitBreakStmt(this)
    }
}

class ContinueStmt: NonDeclStmt() {
    override fun accept(visitor: StmtVisitor) {
        visitor.visitContinueStmt(this)
    }
}

class ReturnStmt(val keyword: Token, val expr: Expr? = null): NonDeclStmt() {
    override fun accept(visitor: StmtVisitor) {
        visitor.visitReturnStmt(this)
    }
}
