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

    @Test
    fun `if statement works`() {

        testCode("""
            var age = 4;
            
            if (age >= 4) {
                print "This movie is for you";
            } else {
                print "Sorry, you are much too young. Grow up a little!";
            }
            
            print "This is the end.";
        """.trimIndent())

    }

    @Test
    fun `shortcut evaluation works`() {

        testCode("""
            print "hi" or 2;
            print nil or "yes";
            print "hello" and 2;
            print nil and "yes";
        """.trimIndent())

    }

    @Test
    fun `while statement works`() {

        testCode("""
            var i = 0;
            var n = 5;
            
            while (i < n) {
                print i;
                i = i + 1;
            }
       
        """.trimIndent())

    }

    @Test
    fun `for loop works`() {

        testCode("""
            
            var a = 0;
            var temp;
            
            for (var b = 1; a < 10000; b = temp + b) {
                print a;
                temp = a;
                a = b;
            }
            
        """.trimIndent())

    }

    @Test
    fun `break and continue in loops works`() {

        testCode("""
            
            print "BREAK:";
            print "";
            
            for (var i = 0; i < 10; i = i + 1) {
                if (i == 5 or i == 7) break;
                print i;
            }
            
            print "";
            print "CONTINUE:";
            print "";
            
            for (var i = 0; i < 10; i = i + 1) {
                if (i == 5 or i == 7) continue;
                print i;
            }
            
            print "";
            print "NESTING:";
            
            for (var i = 0; i < 4; i = i + 1) {
                if (i == 1) continue;
                for (var j = i; j < 10; j = j + 1) {
                    if (i + j > 5) {
                        break;
                    }
                    print i;
                    print j;
                    print "";
                }
            }
            
        """.trimIndent())

    }

    @Test
    fun `calls to builtin functions work`() {

        testCode("""
            
            var start = clock();
            
            // Do something:
            for (var i = 0; i < 1000000; i = i + 1) { }
            
            var end = clock();
            
            print end - start;
            
        """.trimIndent())

    }

    private fun testCode(code: String) {
        val program = parse(code)
        assertNotNull(program)
        Interpreter().interpret(program)
    }
}