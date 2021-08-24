package de.tbollmeier.klox

class Scanner(private val source: String) {

    private var current = 0
    private var start = 0
    private var line = 1

    fun scanTokens(): List<Token> {

        val ret = mutableListOf<Token>()

        while (!isAtEnd()) {
            start = current
            val token: Token = scanToken()
            ret.add(token)
        }

        ret.add(Token(TokenType.EOF, "", line))

        return ret
    }

    private fun scanToken(): Token {
        TODO()
    }

    private fun isAtEnd() = current >= source.length
}