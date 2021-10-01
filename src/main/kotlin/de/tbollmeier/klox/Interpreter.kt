package de.tbollmeier.klox

import de.tbollmeier.klox.TokenType.*
import java.util.*

class InterpreterError(val token: Token, message: String) : RuntimeException(message)

class BreakEvent : RuntimeException()

class ContinueEvent : RuntimeException()

class ReturnEvent(val value: Value) : RuntimeException()

class Interpreter : ExprVisitor<Value>, StmtVisitor {

    private val globals = Environment()
    private val locals = mutableMapOf<Expr, Int>()
    var environment = globals

    private val whileBodies = Stack<NonDeclStmt>()

    init {
        initBuiltinFunctions(environment)
    }

    fun interpret(program: Program) {
        program.accept(Resolver(this))
        program.accept(this)
    }

    fun evaluate(expression: Expr) = expression.accept(this)

    fun newScope(): Environment {
        environment = Environment(environment)
        return environment
    }

    override fun visitBinaryExpr(binary: Binary): Value {
        val left = evaluate(binary.left)
        val right = evaluate(binary.right)
        val numbersExpectedMsg = "Number operands expected"

        return when (binary.operator.tokenType) {
            PLUS -> {
                if (left is Number && right is Number) {
                    left.add(right)
                } else if (left is Str && right is Str) {
                    left.concat(right)
                } else {
                    throw InterpreterError(binary.operator, "Number or string operands expected")
                }
            }
            MINUS -> {
                if (left is Number && right is Number) {
                    left.subtract(right)
                } else {
                    throw InterpreterError(binary.operator, numbersExpectedMsg)
                }
            }
            STAR -> {
                if (left is Number && right is Number) {
                    left.multiply(right)
                } else {
                    throw InterpreterError(binary.operator, numbersExpectedMsg)
                }
            }
            SLASH -> {
                if (left is Number && right is Number) {
                    if (right.isNotEqual(Number(0.0)).isTruthy())
                        left.divide(right)
                    else
                        throw InterpreterError(binary.operator, "division by zero")
                } else {
                    throw InterpreterError(binary.operator, numbersExpectedMsg)
                }
            }
            GREATER -> {
                if (left is Number && right is Number) {
                    left.isGreater(right)
                } else {
                    throw InterpreterError(binary.operator, numbersExpectedMsg)
                }
            }
            GREATER_EQUAL -> {
                if (left is Number && right is Number) {
                    left.isGreaterOrEq(right)
                } else {
                    throw InterpreterError(binary.operator, numbersExpectedMsg)
                }
            }
            LESS -> {
                if (left is Number && right is Number) {
                    left.isLess(right)
                } else {
                    throw InterpreterError(binary.operator, numbersExpectedMsg)
                }
            }
            LESS_EQUAL -> {
                if (left is Number && right is Number) {
                    left.isLessOrEq(right)
                } else {
                    throw InterpreterError(binary.operator, numbersExpectedMsg)
                }
            }
            EQUAL_EQUAL -> left.isEqual(right)
            BANG_EQUAL -> left.isNotEqual(right)
            else -> throw InterpreterError(binary.operator, "Unsupported operator '${binary.operator.lexeme}'")
        }
    }

    override fun visitGroupingExpr(grouping: Grouping) = evaluate(grouping.expression)

    override fun visitLiteralExpr(literal: Literal): Value {
        return when(literal.value) {
            is Double -> Number(literal.value)
            is String -> Str(literal.value)
            is Boolean -> Bool(literal.value)
            else -> Nil()
        }
    }

    override fun visitUnaryExpr(unary: Unary): Value {
        val op = unary.operator
        val expr = unary.right
        return when(op.tokenType) {
            BANG -> Bool(!(evaluate(expr).isTruthy()))
            MINUS -> {
                val value = evaluate(expr)
                if (value is Number) {
                    value.negate()
                } else {
                    throw InterpreterError(op, "Operator '-' can only be used with numbers")
                }
            }
            else -> throw InterpreterError(op, "Unknown unary operator '${op.lexeme}'")
        }
    }

    override fun visitExpressionStmt(expressionStmt: ExpressionStmt) {
        evaluate(expressionStmt.expression)
    }

    override fun visitPrintStmt(printStmt: PrintStmt) {
        val value = evaluate(printStmt.expression)
        println("$value")
    }

    override fun visitBlockStmt(blockStmt: BlockStmt) {
        environment = Environment(environment)
        try {
            for (stmt in blockStmt.statements) {
                try {
                    stmt.accept(this)
                } catch (evt: ContinueEvent) {
                    if (whileBodies.peek() == blockStmt) {
                        return
                    } else {
                        throw evt
                    }
                }
            }
        } finally {
            environment = environment.enclosing!!
        }
    }

    override fun visitIfStmt(ifStmt: IfStmt) {
        val condValue = evaluate(ifStmt.condition)
        if (condValue.isTruthy()) {
            ifStmt.thenBranch.accept(this)
        } else if (ifStmt.elseBranch != null) {
            ifStmt.elseBranch.accept(this)
        }
    }

    override fun visitWhileStmt(whileStmt: WhileStmt) {
        try {
            whileBodies.push(whileStmt.statement)
            while (evaluate(whileStmt.condition).isTruthy()) {
                try {
                    whileStmt.statement.accept(this)
                } catch (evt: BreakEvent) {
                    break
                }
            }
        } finally {
            whileBodies.pop()
        }
    }

    override fun visitForStmt(forStmt: ForStmt) {

        try {
            environment = Environment(environment)
            whileBodies.push(forStmt.statement)

            forStmt.initializer?.accept(this)

            while (true) {
                if (forStmt.condition != null && !evaluate(forStmt.condition).isTruthy()) {
                    break
                }

                try {
                    forStmt.statement.accept(this)
                } catch (evt: BreakEvent) {
                    break
                }

                if (forStmt.increment != null) {
                    evaluate(forStmt.increment)
                }
            }

        } finally {
            environment = environment.enclosing!!
            whileBodies.pop()
        }
    }

    override fun visitBreakStmt(breakStmt: BreakStmt) {
        throw BreakEvent()
    }

    override fun visitContinueStmt(continueStmt: ContinueStmt) {
        throw ContinueEvent()
    }

    override fun visitReturnStmt(returnStmt: ReturnStmt) {
        val value = if (returnStmt.expr != null) {
            evaluate(returnStmt.expr)
        } else {
            Nil()
        }
        throw ReturnEvent(value)
    }

    override fun visitVariable(variable: Variable): Value {
        return getEnvironment(variable).getValue(variable.name)
    }

    private fun getEnvironment(expr: Expr): Environment {
        val distance = locals[expr]
        return if(distance != null) {
            getEnvironmentAt(distance)
        } else {
            globals
        }
    }

    private fun getEnvironmentAt(distance: Int): Environment {
        var ret = environment
        var i = distance
        while (i > 0) {
            ret = ret.enclosing!!
            i--
        }
        return ret
    }

    override fun visitVarDeclStmt(varDeclStmt: VarDeclStmt) {
        val name = varDeclStmt.name.lexeme
        val value = if (varDeclStmt.initializer != null) {
            evaluate(varDeclStmt.initializer)
        } else {
            Nil()
        }
        environment.define(name, value)
    }

    override fun visitAssignExpr(assign: Assign): Value {
        val value = evaluate(assign.value)
        val env = getEnvironment(assign)
        env.assign(assign.name, value)
        return value
    }

    override fun visitLogicalExpr(logical: Logical): Value {
        val left = logical.left.accept(this)
        return when (logical.operator.tokenType) {
            OR -> {
                if (left.isTruthy()) {
                    left
                } else {
                    logical.right.accept(this)
                }
            }
            else -> {
                if (!left.isTruthy()) {
                    left
                } else {
                    logical.right.accept(this)
                }
            }
        }
    }

    override fun visitCallExpr(call: Call): Value {
        val callee = evaluate(call.callee)
        val arguments = call.arguments.map { evaluate(it) }

        if (callee is Callable) {
            val numParameters = callee.arity()
            val numArgs = arguments.size
            if (numArgs != numParameters) {
                val message = "Expected $numParameters arguments but got $numArgs."
                throw InterpreterError(call.closingParen, message)
            }
            return callee.call(this, arguments)
        } else {
            throw InterpreterError(call.closingParen, "Can only call functions and classes.")
        }
    }

    override fun visitFunExpr(fn: FunExpr): Value {
        return Function(fn.parameters, fn.block, environment)
    }

    fun resolve(expr: Expr, distance: Int) {
        locals[expr] = distance
    }

}