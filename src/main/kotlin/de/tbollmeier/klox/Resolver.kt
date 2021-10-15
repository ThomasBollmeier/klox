package de.tbollmeier.klox

import java.util.*

class Resolver(private val interpreter: Interpreter) : ExprVisitor<Unit>, StmtVisitor {

    private val scopes = Stack<Scope>()
    private var inInstanceContext = false
    private var isInitializer = false

    override fun visitBinaryExpr(binary: Binary) {
        binary.left.accept(this)
        binary.right.accept(this)
    }

    override fun visitAssignExpr(assign: Assign) {
        assign.value.accept(this)
        resolveLocal(assign, assign.name)
    }

    override fun visitGroupingExpr(grouping: Grouping) {
        grouping.expression.accept(this)
    }

    override fun visitLiteralExpr(literal: Literal) {
        // Nothing to do...
    }

    override fun visitVariable(variable: Variable) {
        if (scopes.isEmpty()) {
            return
        }

        val isDefined = scopes.peek()[variable.name.lexeme]
        if (isDefined != null && !isDefined) {
            Lox.error(variable.name, "Can't read local variable in its own initializer")
        }

        resolveLocal(variable, variable.name)
    }

    private fun resolveLocal(expr: Expr, name: Token) {
        for (i in scopes.size - 1 downTo 0) {
            val scope = scopes[i]
            if (name.lexeme in scope) {
                interpreter.resolve(expr, scopes.size - 1 - i)
                return
            }
        }
    }

    override fun visitUnaryExpr(unary: Unary) {
        unary.right.accept(this)
    }

    override fun visitLogicalExpr(logical: Logical) {
        logical.left.accept(this)
        logical.right.accept(this)
    }

    override fun visitCallExpr(call: Call) {
        call.callee.accept(this)
        call.arguments.forEach { it.accept(this) }
    }

    override fun visitFunExpr(fn: FunExpr) {
        beginScope()
        fn.parameters.forEach {
            setVarDefDone(it, true)
        }
        fn.block.accept(this)
        endScope()
    }

    override fun visitVarDeclStmt(varDeclStmt: VarDeclStmt) {
        val name = varDeclStmt.name
        setVarDefDone(name, false)
        val initializer = varDeclStmt.initializer
        if (initializer != null) {
            if (initializer is FunExpr) {
                // Functions might be recursive => the function name must be in scope while
                // resolving the body
                setVarDefDone(name, true)
            }
            varDeclStmt.initializer.accept(this)
        }
        if (initializer !is FunExpr) {
            setVarDefDone(name, true)
        }
    }

    override fun visitClassStmt(classStmt: ClassStmt) {
        val prevInClass = inInstanceContext
        setVarDefDone(classStmt.name, true)
        beginScope()
        scopes.peek()["this"] = true
        classStmt.methods.forEach {
            val (name, funExpr, isClassMethod) = it
            inInstanceContext = !isClassMethod
            isInitializer = (name.lexeme == "init") && !isClassMethod
            visitFunExpr(funExpr)
            inInstanceContext = false
            isInitializer = false
        }
        endScope()
        inInstanceContext = prevInClass
    }

    override fun visitExpressionStmt(expressionStmt: ExpressionStmt) {
        expressionStmt.expression.accept(this)
    }

    override fun visitPrintStmt(printStmt: PrintStmt) {
        printStmt.expression.accept(this)
    }

    override fun visitBlockStmt(blockStmt: BlockStmt) {
        beginScope()
        blockStmt.statements.forEach { it.accept(this) }
        endScope()
    }

    override fun visitIfStmt(ifStmt: IfStmt) {
        ifStmt.condition.accept(this)
        ifStmt.thenBranch.accept(this)
        ifStmt.elseBranch?.accept(this)
    }

    override fun visitWhileStmt(whileStmt: WhileStmt) {
        whileStmt.condition.accept(this)
        whileStmt.statement.accept(this)
    }

    override fun visitForStmt(forStmt: ForStmt) {
        beginScope()
        forStmt.initializer?.accept(this)
        forStmt.condition?.accept(this)
        forStmt.increment?.accept(this)
        forStmt.statement.accept(this)
        endScope()
    }

    override fun visitBreakStmt(breakStmt: BreakStmt) {
        // Nothing to do...
    }

    override fun visitContinueStmt(continueStmt: ContinueStmt) {
        // Nothing to do...
    }

    override fun visitReturnStmt(returnStmt: ReturnStmt) {
        if (isInitializer && returnStmt.expr != null) {
            Lox.error(returnStmt.keyword, "Can't return a value from an initializer.")
        }
        returnStmt.expr?.accept(this)
    }

    override fun visitGet(get: Get) {
        get.obj.accept(this)
    }

    override fun visitSet(set: Set) {
        set.obj.accept(this)
        set.value.accept(this)
    }

    override fun visitThis(self: This) {
        if (inInstanceContext) {
            resolveLocal(self, self.token)
        } else {
            Lox.error(self.token, "Can't use 'this' outside of a class.")
        }
    }

    private fun beginScope() {
        scopes.push(mutableMapOf())
    }

    private fun endScope() {
        scopes.pop()
    }

    private fun setVarDefDone(name: Token, done: Boolean) {
        if (scopes.isEmpty()) {
            return
        }
        val scope = scopes.peek()

        if (!done && name.lexeme in scope) {
            Lox.error(name, "Already a variable with this name in scope.")
        }
        scope[name.lexeme] = done
    }

}

private typealias Scope = MutableMap<String, Boolean>