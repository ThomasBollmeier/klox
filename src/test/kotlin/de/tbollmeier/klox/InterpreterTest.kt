package de.tbollmeier.klox

import org.testng.annotations.Test

import org.testng.Assert.*

class InterpreterTest {

    @Test
    fun `executes successfully`() {

        val code = "print (1 + 2) * 3 == 9; print 7 * 6;"
        val program = parse(code)

        assertNotNull(program)

        Interpreter().interpret(program)

    }

    @Test
    fun `execute variable declaration`() {

        val code = """
            var a = 7;
            var b = 6;
            var answer = a * b;
            print answer;
        """.trimIndent()
        val program = parse(code)

        assertNotNull(program)

        Interpreter().interpret(program)

    }
}