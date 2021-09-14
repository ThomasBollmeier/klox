package de.tbollmeier.klox

import org.testng.annotations.Test

import org.testng.Assert.*

class InterpreterTest {

    @Test
    fun `executes successfully`() {

        testCode("print (1 + 2) * 3 == 9; print 7 * 6;")

    }

    @Test
    fun `execute variable declaration`() {

        testCode("""
            var a = 7;
            var b = 6;
            var answer = a * b;
            print answer;
        """.trimIndent())

    }

    @Test
    fun `assignment works`() {

        testCode("""
            var answer = 21;
            var reply = 23;
            answer = reply = 2 * answer;
            print answer;
            print reply;
        """.trimIndent())

    }

    @Test
    fun `blocks work`() {

        testCode("""
            var x = "global x";
            {
                var x = "outer x";
                {
                    var x = "inner x";
                    x = "inner x 2";
                    print x;
                }
                print x;
            }
            print x;
        """.trimIndent())

    }

    @Test
    fun `shadowing works correctly`() {

        testCode("""
            var a = 40;
            {
                var a = a + 2;
                print a;
            }
            print a;
        """.trimIndent())

    }

    private fun testCode(code: String) {
        val program = parse(code)
        assertNotNull(program)
        Interpreter().interpret(program)
    }
}