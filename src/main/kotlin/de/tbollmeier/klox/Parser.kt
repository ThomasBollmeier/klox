package de.tbollmeier.klox

import de.tbollmeier.klox.TokenType.*

class ParseError() : RuntimeException()

class Parser(private val tokens: List<Token>) {

    private var current = 0

    fun parse() : Expr? {
        return try {
            expression()
        } catch (error: ParseError) {
            null
        }
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

    // comparison -> term ((">" | ">=" | "<" | "<=") term)*
    private fun comparison(): Expr {
        var expr = term()
        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            val operator = previous()
            val right = term()
            expr = Binary(operator, expr, right)
        }
        return expr
    }

    // term -> factor (("+" | "-") factor)*
    private fun term(): Expr {
        var expr = factor()
        while (match(PLUS, MINUS)) {
            val operator = previous()
            val right = factor()
            expr = Binary(operator, expr, right)
        }
        return expr
    }

    // factor -> unary (("*"| "/") unary)*
    private fun factor(): Expr {
        var expr = unary()
        while (match(STAR, SLASH)) {
            val operator = previous()
            val right = unary()
            expr = Binary(operator, expr, right)
        }
        return expr
    }

    // unary -> ("!" | "-") unary | primary
    private fun unary(): Expr {
        return if (match(BANG, MINUS)) {
            val operator = previous()
            val right = unary()
            Unary(operator, right)
        } else {
            primary()
        }
    }

    // primary -> NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")"
    private fun primary(): Expr {
        return when {
            match(NUMBER, STRING) -> Literal(previous().literal)
            match(TRUE) -> Literal(true)
            match(FALSE) -> Literal(false)
            match(NIL) -> Literal(null)
            match(LEFT_PAREN) -> {
                val expr = expression()
                consume(RIGHT_PAREN, "Expected closing ')' after expression")
                Grouping(expr)
            }
            else -> throw error(peek(), "Unknown token")
        }
    }

    private fun consume(type: TokenType, errorMessage: String): Token {
        return if (check(type)) {
            advance()
        } else {
            throw error(peek(), errorMessage)
        }

    }

    private fun error(token: Token, errorMessage: String): ParseError {
        Lox.error(token, errorMessage)
        return ParseError()
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