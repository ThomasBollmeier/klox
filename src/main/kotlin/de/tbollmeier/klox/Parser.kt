package de.tbollmeier.klox

import de.tbollmeier.klox.TokenType.*

fun parse(code: String) = Parser(Scanner(code).scanTokens()).parse()

class ParseError() : RuntimeException()

class Parser(private val tokens: List<Token>) {

    private var current = 0

    fun parse(): Program {
        return program()
    }

    // program -> statement* EOF
    private fun program(): Program {
        val statements = mutableListOf<Stmt>()
        while (!isAtEnd()) {
            val stmt = statement()
            if (stmt != null) {
                statements.add(stmt)
            }
        }
        return Program(statements)
    }

    // statement -> varDeclStmt | expressionStmt | printStmt
    private fun statement(): Stmt? {
        return try {
            when {
                match(VAR) -> varDeclStmt()
                match(PRINT) -> printStmt()
                else -> expressionStmt()
            }
        } catch (error: ParseError) {
            synchronize()
            null
        }
    }

    // varDeclStmt -> "VAR" IDENTIFIER ("=" expression)? ";"
    private fun varDeclStmt(): Stmt {
        val name = consume(IDENTIFIER, "Expected variable name.")
        val initializer = if (match(EQUAL)) {
            expression()
        } else {
            null
        }
        consume(SEMICOLON, "Expected ';' after variable declaration.")
        return VarDeclStmt(name, initializer)
    }

    // expressionStmt -> expression ";"
    private fun expressionStmt(): ExpressionStmt {
        val expr = expression()
        consume(SEMICOLON, "Expected ';' after value.")
        return ExpressionStmt(expr)
    }

    // printStmt -> "print" expression ";"
    private fun printStmt(): PrintStmt {
        val expr = expression()
        consume(SEMICOLON, "Expected ';' after expression.")
        return PrintStmt(expr)
    }

    // expression -> assignment | equality
    private fun expression(): Expr {

        val expr = equality()

        if (match(EQUAL)) {
            val equals = previous()
            when (expr) {
                is Variable -> {
                    val rhs = expression()
                    return Assign(expr.name, rhs)
                }
                else -> error(equals, "Invalid assignment target.")
            }
        }

        return expr
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

    // primary -> NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")" | IDENTIFIER
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
            match(IDENTIFIER) -> Variable(previous())
            else -> throw error(peek(), "Expected expression.")
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

    private fun synchronize() {
        advance()
        while (!isAtEnd()) {
            if (previous().tokenType == SEMICOLON) {
                return
            }
            when (peek().tokenType) {
                CLASS, FOR, FUN, IF, PRINT, RETURN, VAR, WHILE -> return
                else -> advance()
            }
        }
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