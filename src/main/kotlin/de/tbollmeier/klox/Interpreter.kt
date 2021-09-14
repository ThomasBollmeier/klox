package de.tbollmeier.klox

import de.tbollmeier.klox.TokenType.*

class InterpreterError(val token: Token, message: String) : RuntimeException(message)

class Interpreter : ExprVisitor<Value>, StmtVisitor {

    private var environment = Environment()

    fun interpret(program: Program) {
        program.accept(this)
    }

    fun evaluate(expression: Expr) = expression.accept(this)

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
            blockStmt.statements.forEach { it.accept(this) }
        } finally {
            environment = environment.enclosing!!
        }
    }

    override fun visitVariable(variable: Variable): Value {
        return environment.getValue(variable.name)
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
        environment.assign(assign.name, value)
        return value
    }
}