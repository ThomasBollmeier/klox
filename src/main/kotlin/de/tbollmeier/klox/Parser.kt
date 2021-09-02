package de.tbollmeier.klox

import de.tbollmeier.klox.TokenType.*

class Parser(private val tokens: List<Token>) {

    private var current = 0

    fun parse() : Expr {
        return expression()
    }

    // expression -> equality
    private fun expression(): Expr {
        return equality()
    }

    // equality -> comparison (("!=" | "==) comparison)*
    private fun equality(): Expr {
        var expr = comparison()
        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            val operator = previous()
            val right = comparison()
            expr = Binary(operator, expr, right)
        }
        return expr
    }

    private fun comparison(): Expr {
        TODO()
    }

    private fun match(vararg types: TokenType): Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }
        return false
    }

    private fun check(type: TokenType): Boolean {
        if (isAtEnd()) {
            return false
        }
        return peek().tokenType == type
    }

    private fun isAtEnd() = peek().tokenType == EOF

    private fun advance(): Token {
        if (!isAtEnd()) {
            current++
        }
        return previous()
    }

    private fun peek() = tokens[current]

    private fun previous() = tokens[current - 1]

}