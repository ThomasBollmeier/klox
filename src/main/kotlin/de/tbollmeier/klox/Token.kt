package de.tbollmeier.klox

class Token(
    val tokenType: TokenType,
    val lexeme: String,
    val line: Int,
    val literal: Any? = null
    ) {

    override fun toString() = "$tokenType $lexeme $literal"

}