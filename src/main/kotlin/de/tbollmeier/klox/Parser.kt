package de.tbollmeier.klox

import de.tbollmeier.klox.TokenType.*

fun parse(code: String) = Parser(Scanner(code).scanTokens()).parse()

fun parseExpr(code: String): Expr? {
    val parser = Parser(Scanner(code).scanTokens())
    return try {
        parser.expression()
    } catch (error: ParseError) {
        null
    }
}

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

    // statement -> varDeclStmt | nonDeclStatement
    private fun statement(): Stmt? {
        return try {
            when {
                match(VAR) -> varDeclStmt()
                else -> nonDeclStatement()
            }
        } catch (error: ParseError) {
            synchronize()
            null
        }
    }

    // nonDeclStatement -> expressionStmt | printStmt | blockStnt | ifStmt
    private fun nonDeclStatement(): NonDeclStmt {
        return when {
            match(PRINT) -> printStmt()
            match(LEFT_BRACE) -> blockStmt()
            match(IF) -> ifStmt()
            else -> expressionStmt()
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

    // blockStmt -> "{" statement* "}"
    private fun blockStmt(): BlockStmt {
        val statements = mutableListOf<Stmt>()

        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            val stmt = statement()
            if (stmt != null) {
                statements.add(stmt)
            }
        }

        consume(RIGHT_BRACE, "Expected '}' after block.")

        return BlockStmt(statements)
    }

    // ifStmt -> "if" "(" expression ")" nonDeclStatement ("else" nonDeclStatement)?
    fun ifStmt(): IfStmt {

        val ifToken = previous()

        consume(LEFT_PAREN, "Expected '(' after 'if'.")
        val condition = expression()
        consume(RIGHT_PAREN, "Expected ')' after condition.")

        val thenBranch = nonDeclStatement()

        if (thenBranch is BlockStmt && thenBranch.hasDeclarations) {
            throw error(ifToken, "Declarations in if statements are not allowed.")
        }

        val elseBranch = if (match(ELSE)) {
            nonDeclStatement()
        } else {
            null
        }

        if (elseBranch is BlockStmt && elseBranch.hasDeclarations) {
            throw error(ifToken, "Declarations in if statements are not allowed.")
        }

        return IfStmt(condition, thenBranch, elseBranch)
    }

    // expression -> assignment
    fun expression() = assignment()

    // assignment -> equality ("=" assignment)?
    private fun assignment(): Expr {

        val expr = equality()

        if (match(EQUAL)) {
            val equals = previous()
            when (expr) {
                is Variable -> {
                    val rhs = assignment()
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