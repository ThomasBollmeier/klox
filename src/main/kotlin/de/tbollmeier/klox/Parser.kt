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

class ParseError : RuntimeException()

class Parser(private val tokens: List<Token>) {

    private var current = 0
    private var loopNesting = 0
    private var funcNesting = 0
    private val  maxNumArgs = 255

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

    // statement -> funDeclStmt | varDeclStmt | nonDeclStatement
    private fun statement(): Stmt? {
        return try {
            when {
                match(FUN) -> function("function")
                match(VAR) -> varDeclStmt()
                else -> nonDeclStatement()
            }
        } catch (error: ParseError) {
            synchronize()
            null
        }
    }

    // function -> IDENTIFIER "(" (IDENTIFIER ("," IDENTIFIER)* )? ")" block
    private fun function(kind: String): FunctionDeclStmt {

        val name = consume(IDENTIFIER, "Expected identifier as $kind name.")
        consume(LEFT_PAREN, "Expected '(' after $kind name")
        val parameters = mutableListOf<Token>()
        if (!check(RIGHT_PAREN)) {
            var parameter = consume(IDENTIFIER, "Parameter must be an identifier.")
            parameters.add(parameter)
            while (check(COMMA)) {
                consume(COMMA, "Expected comma.")
                parameter = consume(IDENTIFIER, "Parameter must be an identifier.")
                if (parameters.size > maxNumArgs) {
                    error(previous(), "Can't have more that $maxNumArgs parameters.")
                }
                parameters.add(parameter)
            }
        }
        consume(RIGHT_PAREN, "Expected ')' after parameter list.")

        consume(LEFT_BRACE, "Expected function block to start with '{'.")

        try {
            funcNesting++
            val block = blockStmt()
            return FunctionDeclStmt(name, parameters, block)
        } finally {
            funcNesting--
        }
    }

    // nonDeclStatement -> expressionStmt | printStmt | blockStnt | ifStmt |
    // whileStmt | forStmt | breakStmt | continueStmt | returnStmt
    private fun nonDeclStatement(): NonDeclStmt {
        return when {
            match(PRINT) -> printStmt()
            match(LEFT_BRACE) -> blockStmt()
            match(IF) -> ifStmt()
            match(WHILE) -> whileStmt()
            match(FOR) -> forStmt()
            match(BREAK) -> breakStmt()
            match(CONTINUE) -> continueStmt()
            match(RETURN) -> returnStmt()
            else -> expressionStmt()
        }
    }

    // varDeclStmt -> "var" IDENTIFIER ("=" expression)? ";"
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
    private fun ifStmt(): IfStmt {

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

    // whileStmt -> "while" "(" expression ")" statement;
    private fun whileStmt(): WhileStmt {

        val whileToken = previous()

        try {
            loopNesting++

            consume(LEFT_PAREN, "Expected '(' after 'while'.")
            val condition = expression()
            consume(RIGHT_PAREN, "Expected ')' after condition.")

            val statement = nonDeclStatement()

            if (statement is BlockStmt && statement.hasDeclarations) {
                throw error(whileToken, "Declarations in while blocks are not allowed.")
            }

            return WhileStmt(condition, statement)

        } finally {
            loopNesting--
        }

    }

    // forStmt -> "for" "(" initializer expression? ";" expression? ")" statement
    private fun forStmt(): ForStmt {

        val forToken = previous()

        try {
            loopNesting++

            consume(LEFT_PAREN, "Expected '(' after 'for'.")

            val initializer = initializerStmt()

            val condition = if (!check(SEMICOLON)) {
                expression()
            } else {
                Literal(true)
            }
            consume(SEMICOLON, "Expected ';' after condition")

            val increment = if (!check(RIGHT_PAREN)) {
                expression()
            } else {
                null
            }
            consume(RIGHT_PAREN, "Expected ')' after increment.")

            val statement = nonDeclStatement()

            if (statement is BlockStmt && statement.hasDeclarations) {
                throw error(forToken, "Declarations in for blocks are not allowed.")
            }

            return ForStmt(initializer, condition, increment, statement)

        } finally {
            loopNesting--
        }
    }

    private fun initializerStmt() = when {
        match(VAR) -> varDeclStmt()
        match(SEMICOLON) -> null
        else -> expressionStmt()
    }

    // breakStmt -> "break" ";"
    private fun breakStmt(): BreakStmt {
        val breakToken = previous()
        return if (loopNesting > 0) {
            consume(SEMICOLON, "Expected ;' after 'break'.")
            BreakStmt()
        } else {
            throw error(breakToken, "'break' must only be used within loops.")
        }
    }

    // continueStmt -> "continue" ";"
    private fun continueStmt(): NonDeclStmt {
        val continueToken = previous()
        return if (loopNesting > 0) {
            consume(SEMICOLON, "Expected ;' after 'continue'.")
            ContinueStmt()
        } else {
            throw error(continueToken, "'continue' must only be used within loops.")
        }
    }

    // returnStmt -> "return" expression? ";"
    private fun returnStmt(): ReturnStmt {
        val returnToken = previous()
        return if (funcNesting > 0) {
            val expr = if (!check(SEMICOLON)) {
                expression()
            } else {
                null
            }
            consume(SEMICOLON, "Expected ';' at end of return statement.")
            ReturnStmt(expr)
        } else {
            throw error(returnToken, "'return' must only be used within functions.")
        }
    }

    // expression -> assignment
    fun expression() = assignment()

    // assignment -> or ("=" assignment)?
    private fun assignment(): Expr {

        val expr = or()

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

    // or -> and ("or" and)*
    private fun or(): Expr {
        var expr = and()
        while (match(OR)) {
            val operator = previous()
            val right = and()
            expr = Logical(operator, expr, right)
        }
        return expr
    }

    // and -> equality ("and" equality)*
    private fun and(): Expr {
        var expr = equality()
        while (match(AND)) {
            val operator = previous()
            val right = equality()
            expr = Logical(operator, expr, right)
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

    // unary -> ("!" | "-") unary | call
    private fun unary(): Expr {
        return if (match(BANG, MINUS)) {
            val operator = previous()
            val right = unary()
            Unary(operator, right)
        } else {
            call()
        }
    }

    // call -> primary ( "(" arguments? ")" )*
    private fun call(): Expr {
        var expr = primary()

        while (true) {
            if (match(LEFT_PAREN)) {
                expr = finishCall(expr)
            } else {
                break
            }
        }

        return expr
    }

    private fun finishCall(callee: Expr): Expr {
        val arguments = mutableListOf<Expr>()
        if (!check(RIGHT_PAREN)) {
            do {
                if (arguments.size > maxNumArgs) {
                    error(previous(), "Can't have more that $maxNumArgs arguments.")
                }
                arguments.add(expression())
            } while (match(COMMA))
        }
        val closingParen = consume(RIGHT_PAREN, "Expected ')' after arguments.")
        return Call(callee, closingParen, arguments)
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