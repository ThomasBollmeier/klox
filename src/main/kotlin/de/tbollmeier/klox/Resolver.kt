package de.tbollmeier.klox

import java.util.*

class Resolver(private val interpreter: Interpreter) : ExprVisitor<Unit>, StmtVisitor {

    private val scopes = Stack<Scope>()

    override fun visitBinaryExpr(binary: Binary) {
        TODO("Not yet implemented")
    }

    override fun visitAssignExpr(assign: Assign) {
        TODO("Not yet implemented")
    }

    override fun visitGroupingExpr(grouping: Grouping) {
        TODO("Not yet implemented")
    }

    override fun visitLiteralExpr(literal: Literal) {
        TODO("Not yet implemented")
    }

    override fun visitVariable(variable: Variable) {
        TODO("Not yet implemented")
    }

    override fun visitUnaryExpr(unary: Unary) {
        TODO("Not yet implemented")
    }

    override fun visitLogicalExpr(logical: Logical) {
        TODO("Not yet implemented")
    }

    override fun visitCallExpr(call: Call) {
        TODO("Not yet implemented")
    }

    override fun visitFunExpr(fn: FunExpr) {
        TODO("Not yet implemented")
    }

    override fun visitVarDeclStmt(varDeclStmt: VarDeclStmt) {
        TODO("Not yet implemented")
    }

    override fun visitExpressionStmt(expressionStmt: ExpressionStmt) {
        TODO("Not yet implemented")
    }

    override fun visitPrintStmt(printStmt: PrintStmt) {
        TODO("Not yet implemented")
    }

    override fun visitBlockStmt(blockStmt: BlockStmt) {
        beginScope()
        blockStmt.statements.forEach { it.accept(this) }
        endScope()
    }

    override fun visitIfStmt(ifStmt: IfStmt) {
        TODO("Not yet implemented")
    }

    override fun visitWhileStmt(whileStmt: WhileStmt) {
        TODO("Not yet implemented")
    }

    override fun visitForStmt(forStmt: ForStmt) {
        TODO("Not yet implemented")
    }

    override fun visitBreakStmt(breakStmt: BreakStmt) {
        TODO("Not yet implemented")
    }

    override fun visitContinueStmt(continueStmt: ContinueStmt) {
        TODO("Not yet implemented")
    }

    override fun visitReturnStmt(returnStmt: ReturnStmt) {
        TODO("Not yet implemented")
    }

    private fun beginScope() {
        scopes.push(HashMap())
    }

    private fun endScope() {
        scopes.pop()
    }
}

private typealias Scope = Map<String, Boolean>