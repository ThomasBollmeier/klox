package de.tbollmeier.klox

import de.tbollmeier.klox.TokenType.*
import org.testng.annotations.Test
import org.testng.Assert.*
import org.testng.annotations.BeforeTest

class AstPrinterTest {

    private lateinit var printer: AstPrinter

    @BeforeTest
    fun setUp() {
        printer = AstPrinter()
    }

    @Test
    fun testPrint() {

        val expr = Binary(
            Token(STAR, "*", 1, null),
            Unary(
                Token(MINUS, "-", 1, null),
                Literal(123)),
            Grouping(Literal(45.67)))

        val expected = "(* (- 123) (group 45.67))"
        val actual = printer.print(expr)

        assertEquals(expected, actual)
    }
}