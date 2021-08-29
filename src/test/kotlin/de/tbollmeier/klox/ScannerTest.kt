package de.tbollmeier.klox

import org.testng.annotations.Test
import kotlin.test.assertEquals
import de.tbollmeier.klox.TokenType.*

internal class ScannerTest {

    @Test
    fun scanTokens() {

        val source = """
            // A comment
            + = !=  ! > <= // end + /
            "Thomas"
            -3.1415
            trueLove
            true
        """.trimIndent()

        val scanner = Scanner(source)
        val tokens = scanner.scanTokens()

        assertEquals(12, tokens.size)
        assertEquals(PLUS, tokens[0].tokenType)
        assertEquals(EQUAL, tokens[1].tokenType)
        assertEquals(BANG_EQUAL, tokens[2].tokenType)
        assertEquals(BANG, tokens[3].tokenType)
        assertEquals(GREATER, tokens[4].tokenType)
        assertEquals(LESS_EQUAL, tokens[5].tokenType)
        assertEquals(STRING, tokens[6].tokenType)
        assertEquals("Thomas", tokens[6].literal)
        assertEquals(MINUS, tokens[7].tokenType)
        assertEquals(NUMBER, tokens[8].tokenType)
        assertEquals(3.1415, tokens[8].literal)
        assertEquals(IDENTIFIER, tokens[9].tokenType)
        assertEquals("trueLove", tokens[9].literal)
        assertEquals(TRUE, tokens[10].tokenType)
        assertEquals(EOF, tokens[11].tokenType)
    }
}