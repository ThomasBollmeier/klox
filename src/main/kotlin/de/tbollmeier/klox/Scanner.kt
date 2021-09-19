package de.tbollmeier.klox

import de.tbollmeier.klox.TokenType.*

class Scanner(private val source: String) {

    private var current = 0
    private var start = 0
    private var line = 1

    private val reservedWords = mapOf<String, TokenType>(
        "and" to AND,
        "break" to BREAK,
        "continue" to CONTINUE,
        "class" to CLASS,
        "else" to ELSE,
        "false" to FALSE,
        "fun" to FUN,
        "for" to FOR,
        "if" to IF,
        "nil" to NIL,
        "or" to OR,
        "print" to PRINT,
        "return" to RETURN,
        "super" to SUPER,
        "this" to THIS,
        "true" to TRUE,
        "var" to VAR,
        "while" to WHILE
    )

    fun scanTokens(): List<Token> {

        val ret = mutableListOf<Token>()

        while (!isAtEnd()) {
            start = current
            when (val res = scanToken()) {
                is Ok -> if (res.result != null) {
                    ret.add(res.result)
                }
                is Err -> {
                    Lox.error(line, res.message)
                }
            }
        }

        ret.add(Token(EOF, "", line))

        return ret
    }

    private fun scanToken(): Result<Token?, String> {
        val ch = advance()
        return when {
            ch == '(' -> Ok(createToken(LEFT_PAREN))
            ch == ')' -> Ok(createToken(RIGHT_PAREN))
            ch == '{' -> Ok(createToken(LEFT_BRACE))
            ch == '}' -> Ok(createToken(RIGHT_BRACE))
            ch == ',' -> Ok(createToken(COMMA))
            ch == '.' -> Ok(createToken(DOT))
            ch == '-' -> Ok(createToken(MINUS))
            ch == '+' -> Ok(createToken(PLUS))
            ch == ';' -> Ok(createToken(SEMICOLON))
            ch == '*' -> Ok(createToken(STAR))
            ch == '!' -> Ok(createToken(if (match('=')) BANG_EQUAL else BANG))
            ch == '=' -> Ok(createToken(if (match('=')) EQUAL_EQUAL else EQUAL))
            ch == '<' -> Ok(createToken(if (match('=')) LESS_EQUAL else LESS))
            ch == '>' -> Ok(createToken(if (match('=')) GREATER_EQUAL else GREATER))
            ch == '/' -> if (match('/')) {
                while (peek() != '\n' && !isAtEnd()) {
                    advance()
                }
                Ok(null)
            } else {
                Ok(createToken(SLASH))
            }
            ch == ' ' || ch == '\r' || ch == '\t' -> Ok(null)
            ch == '\n' -> {
                line++
                Ok(null)
            }
            ch == '"' -> scanString()
            isDigit(ch) -> scanNumber()
            isAlpha(ch) -> scanIdentifier()
            else -> Err("Unexpected character $ch.")
        }
    }

    private fun scanIdentifier(): Result<Token?, String> {
        while (isAlphaNumeric(peek())) {
            advance()
        }

        val lexeme = source.substring(start, current)
        val tokenType = reservedWords[lexeme] ?: IDENTIFIER
        val literal = if (tokenType == IDENTIFIER)
            lexeme
        else
            null

        return Ok(createToken(tokenType, literal))
    }

    private fun isAlphaNumeric(ch: Char) = isAlpha(ch) || isDigit(ch)

    private fun isAlpha(ch: Char) = ch in 'a'..'z' || ch in 'A'..'Z' || ch == '_'

    private fun scanNumber(): Result<Token?, String> {
        while (isDigit(peek())) {
            advance()
        }

        if (peek() == '.' && isDigit(peek(2))) {
            advance() // consume "."
            while (isDigit(peek())) {
                advance()
            }
        }

        val numValue = source.substring(start, current).toDouble()

        return Ok(createToken(NUMBER, numValue))
    }

    private fun isDigit(ch: Char) = ch in '0'..'9'

    private fun scanString(): Result<Token?, String> {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') {
                line++
            }
            advance()
        }

        if (isAtEnd()) {
            return Err("Unterminated string.")
        }

        advance() // Consume closing "

        val content = source.substring(start + 1, current - 1)

        return Ok(createToken(STRING, content))
    }

    private fun createToken(tokenType: TokenType, literal: Any? = null): Token {
        val lexeme = source.substring(start, current)
        return Token(tokenType, lexeme, line, literal)
    }

    private fun advance(): Char {
        return source[current++]
    }

    private fun match(expected: Char): Boolean {
        if (isAtEnd()) {
            return false
        }

        return if (source[current] == expected) {
            current++
            true
        } else {
            false
        }
    }

    private fun peek(lookahead: Int = 1): Char {
        return if (current + lookahead - 1 <= source.length - 1)
            source[current + lookahead - 1]
        else
            '\u0000'
    }

    private fun isAtEnd() = current >= source.length
}