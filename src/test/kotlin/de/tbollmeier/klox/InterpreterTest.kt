package de.tbollmeier.klox

import org.testng.annotations.Test

import org.testng.Assert.*

class InterpreterTest {

    @Test
    fun `executes successfully`() {

        val code = "print (1 + 2) * 3 == 9; print 7 * 6;"
        val program = parse(code)

        assertNotNull(program)

        Interpreter().interpret(program!!)

    }
}