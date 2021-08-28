package de.tbollmeier.klox

import de.tbollmeier.klox.TokenType.*

class Scanner(private val source: String) {

    private var current = 0
    private var start = 0
    private var line = 1

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
        return when (ch) {
            '(' -> Ok(createToken(LEFT_PAREN))
            ')' -> Ok(createToken(RIGHT_PAREN))
            '{' -> Ok(createToken(LEFT_BRACE))
            '}' -> Ok(createToken(RIGHT_BRACE))
            ',' -> Ok(createToken(COMMA))
            '.' -> Ok(createToken(DOT))
            '-' -> Ok(createToken(MINUS))
            '+' -> Ok(createToken(PLUS))
            ';' -> Ok(createToken(SEMICOLON))
            '*' -> Ok(createToken(STAR))
            '!' -> Ok(createToken(if (match('=')) BANG_EQUAL else BANG))
            '=' -> Ok(createToken(if (match('=')) EQUAL_EQUAL else EQUAL))
            '<' -> Ok(createToken(if (match('=')) LESS_EQUAL else LESS))
            '>' -> Ok(createToken(if (match('=')) GREATER_EQUAL else GREATER))
            '/' -> if (match('/')) {
                while (peek() != '\n' && !isAtEnd()) {
                    advance()
                }
                Ok(null)
            } else {
                Ok(createToken(SLASH))
            }
            ' ', '\r', '\t' -> Ok(null)
            '\n' -> {
                line++
                Ok(null)
            }
            '"' -> scanString()
            else -> Err("Unexpected character $ch.")
        }
    }

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

    private fun peek(): Char {
        return if (!isAtEnd()) source[current] else '\u0000'
    }

    private fun isAtEnd() = current >= source.length
}