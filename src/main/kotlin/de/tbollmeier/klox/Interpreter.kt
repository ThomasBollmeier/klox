package de.tbollmeier.klox

import de.tbollmeier.klox.TokenType.*

class InterpreterError(message: String) : RuntimeException(message)

class Interpreter : Visitor<Value> {

    fun evaluate(expression: Expr) = expression.accept(this)

    override fun visitBinaryExpr(binary: Binary): Value {
        val left = evaluate(binary.left)
        val right = evaluate(binary.right)

        return when (binary.operator.tokenType) {
            PLUS -> {
                if (left is Number && right is Number) {
                    left.add(right)
                } else if (left is Str && right is Str) {
                    left.concat(right)
                } else {
                    throw InterpreterError("Unsupported operand types")
                }
            }
            MINUS -> {
                if (left is Number && right is Number) {
                    left.subtract(right)
                } else {
                    throw InterpreterError("Unsupported operand types")
                }
            }
            STAR -> {
                if (left is Number && right is Number) {
                    left.multiply(right)
                } else {
                    throw InterpreterError("Unsupported operand types")
                }
            }
            SLASH -> {
                if (left is Number && right is Number) {
                    left.divide(right)
                } else {
                    throw InterpreterError("Unsupported operand types")
                }
            }
            GREATER -> {
                if (left is Number && right is Number) {
                    left.isGreater(right)
                } else {
                    throw InterpreterError("Unsupported operand types")
                }
            }
            GREATER_EQUAL -> {
                if (left is Number && right is Number) {
                    left.isGreaterOrEq(right)
                } else {
                    throw InterpreterError("Unsupported operand types")
                }
            }
            LESS -> {
                if (left is Number && right is Number) {
                    left.isLess(right)
                } else {
                    throw InterpreterError("Unsupported operand types")
                }
            }
            LESS_EQUAL -> {
                if (left is Number && right is Number) {
                    left.isLessOrEq(right)
                } else {
                    throw InterpreterError("Unsupported operand types")
                }
            }
            EQUAL_EQUAL -> left.isEqual(right)
            BANG_EQUAL -> left.isNotEqual(right)
            else -> throw InterpreterError("Unsupported operator '${binary.operator.lexeme}'")
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
                    throw InterpreterError("Operator '-' can only be used with numbers")
                }
            }
            else -> throw InterpreterError("Unknown unary operator '${op.lexeme}'")
        }
    }
}