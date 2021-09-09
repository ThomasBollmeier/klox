package de.tbollmeier.klox

import org.testng.annotations.Test

import org.testng.Assert.*

class InterpreterTest {

    @Test
    fun `evaluates successfully`() {

        val code = "(1 + 2) * 3 == 9"
        val expr = parse(code)

        assertNotNull(expr)

        val interpreter = Interpreter()
        val value = interpreter.evaluate(expr!!)

        assertTrue(value is Bool)
        assertTrue(value.isTruthy())
    }
}