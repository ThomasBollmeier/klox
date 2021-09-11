package de.tbollmeier.klox

import org.testng.Assert.*
import org.testng.annotations.Test

class ParserTest {

    @Test
    fun `parses program successfully`() {

        val code = "2 + 20 * (4 - 2);"

        val scanner = Scanner(code)
        val tokens = scanner.scanTokens()
        val parser = Parser(tokens)
        val program = parser.parse()

        assertNotNull(program)

        val exprStmt = program!!.statements[0] as? ExpressionStmt

        assertNotNull(exprStmt)

        val expected = "(+ 2.0 (* 20.0 (group (- 4.0 2.0))))"
        val actual = AstPrinter().print(exprStmt!!.expression)

        assertEquals(actual, expected)
    }

}