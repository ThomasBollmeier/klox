package de.tbollmeier.klox

import org.testng.annotations.Test

import org.testng.Assert.*
import org.testng.annotations.AfterSuite
import org.testng.annotations.BeforeSuite

class StringOut : InterpreterOutput {

    private var _output = ""
    val output
        get() = _output

    override fun writeln(text: String) {
        _output += text + '\n'
    }

}

class InterpreterTest {

    @BeforeSuite
    fun suiteSetup() {
        Lox.loggingOn = false
    }

    @AfterSuite
    fun suiteTeardown() {
        Lox.loggingOn = true
    }

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
                var a = 42;
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

    @Test
    fun `calls to custom functions work`() {

        testCode("""
            
            fun add(a, b) {
                var sum = a + b;
                print sum;
            }
            
            add(1, 41);
                        
        """.trimIndent())

    }
    @Test
    fun `custom functions with result work`() {

        testCode("""
             
             fun fib(n) {
                if (n <= 1) return n;
                return fib(n - 2) + fib(n - 1);
             }
             
             for (var i = 0; i < 20; i = i + 1) {
                print fib(i);
             }
                        
        """.trimIndent())

    }

    @Test
    fun `closures work`() {

        testCode("""
            fun makeCounter() {
                var i = 0;
                fun count() {
                    i = i + 1;
                    print i;
                }
                
                return count;
            }
            
            var cnt = makeCounter();
            cnt();
            cnt();
                        
        """.trimIndent())

    }

    @Test
    fun `anonymous functions work`() {

        testCode("""
            
            fun thrice(fn) {
                for (var i = 1; i <= 3; i = i + 1) {
                    fn(i);
                }
            }
            
            thrice(fun (i) { print i; });
                        
        """.trimIndent(), """
            1
            2
            3
            
        """.trimIndent())

    }

    @Test
    fun `scopes and closures work`() {

        testCode("""
            
            var a = "global";
            
            {
                fun showA() {
                    print a;
                }
                showA();
                var a = "block";
                showA();
            }
                        
        """.trimIndent(), """
            global
            global
            
            """.trimIndent())

    }

    @Test
    fun `No return at top level`() {

        testCode(code = """
            return "at top level";
        """.trimIndent(),
        successExpected = false)

    }

    @Test
    fun `class declarations work`() {

        testCode(
            code = """
                class DevonshireCream {
                    serveOn() {
                        return "Scones";
                    }
                }
                
                print DevonshireCream;
            """.trimIndent(),
        expectedOutput = """
            <class DevonshireCream>
            
        """.trimIndent())

    }

    @Test
    fun `object instantiation work`() {

        testCode(
            code = """
                class Bagel { }
                var bagel = Bagel();
                
                print bagel;
            """.trimIndent(),
            expectedOutput = """
            <instance Bagel>
            
        """.trimIndent())

    }

    @Test
    fun `setters and getters work`() {

        val code = """
            class Person {}
            
            var ego = Person();
            ego.first_name = "Herbert";
            ego.name = "Mustermann";
            
            print ego.first_name;
            print ego.name;
        """.trimIndent()

        val expectedOutput = """
            Herbert
            Mustermann
            
        """.trimIndent()

        testCode(code, expectedOutput)
    }

    @Test
    fun `method calls work`() {

        val code = """
            class Bacon {
                eat() {
                    print "Crunch crunch crunch!";
                }
            }
            
            Bacon().eat();
        """.trimIndent()

        val expectedOutput = """
            Crunch crunch crunch!
            
        """.trimIndent()

        testCode(code, expectedOutput)
    }

    @Test
    fun `this works`() {

        val code = """
            class Thing {
                getCallback() {
                    var answer = 42;
                    fun localFunction() {
                        print this;
                        print answer;
                    }
                    return localFunction;
                }
            }
            
            var callback = Thing().getCallback();
            callback();
            
            class Person
            { 
                fullName() 
                {
                    return this.firstName + " " + this.lastName;
                }
            }
            
            var ego = Person();
            ego.firstName = "Thomas";
            ego.lastName = "Bollmeier";
            
            print ego.fullName();
            
        """.trimIndent()

        val expectedOutput = """
            <instance Thing>
            42
            Thomas Bollmeier
            
        """.trimIndent()

        testCode(code, expectedOutput)
    }

    @Test
    fun `no this outside of methods`() {

        val code = """
            fun doSomething() {
                print this;
            }
            
        """.trimIndent()

        testCode(code = code, successExpected = false)

    }

    @Test
    fun `constructor works`() {

        val code = """
            class Person {
                
                init(firstName, lastName) {
                    this.firstName = firstName;
                    this.lastName = lastName;
                }
                
                fullName() {
                    return this.firstName + " " + this.lastName;
                }
                
            }
            
            var ego = Person("Thomas", "Bollmeier");
            
            print ego.fullName();
            
            var alterEgo = ego.init("Tom", "Ballmiller");
            
            print alterEgo;
            print ego.lastName;
            
        """.trimIndent()

        val expectedOutput = """
            Thomas Bollmeier
            <instance Person>
            Ballmiller
            
        """.trimIndent()

        testCode(code, expectedOutput)
    }

    @Test
    fun `no value return in constructor`() {

        val code = """
            class Test {
                init() {
                    return;
                }
            }
            
            print Test();
        """.trimIndent()

        val expectedOutput = """
            <instance Test>
            
        """.trimIndent()

        testCode(code, expectedOutput)

        val codeWithError = """
            class Test {
                init() {
                    return "something";
                }
            }
        """.trimIndent()

        testCode(codeWithError, successExpected = false)

    }

    @Test
    fun `class methods work`() {
        val code = """
            class Math {
                class square(n) {
                    return n * n;
                }
            }
            
            print Math.square(3);
        """.trimIndent()

        val expectedOutput = """
            9
            
        """.trimIndent()

        testCode(code, expectedOutput)
    }

    @Test
    fun `getters work`() {
        val code = """
            class Circle {
                
                init(radius) {
                    this.radius = radius;
                }
                
                area {
                    return 3.1415 * this.radius * this.radius;
                }
                                
            }
            
            print Circle(4).area;
        """.trimIndent()

        val expectedOutput = """
            50.264
            
        """.trimIndent()

        testCode(code, expectedOutput)
    }

    @Test
    fun `inheritance works`() {

        val code = """
            class Doughnut {
                cook() {
                    print "Fry until golden brown.";
                }
            }
            
            class BostonCream < Doughnut { }
            
            BostonCream().cook();
        """.trimIndent()

        val expectedOutput = """
            Fry until golden brown.
            
        """.trimIndent()

        testCode(code, expectedOutput)
    }

    private fun testCode(
        code: String,
        expectedOutput: String? = null,
        successExpected: Boolean = true)
    {
        Lox.reset()

        val program = parse(code)
        assertNotNull(program)

        val interpreter = Interpreter()
        if (expectedOutput != null) {
            val stringOut = StringOut()
            interpreter.output = stringOut
            interpreter.interpret(program)
            assertEquals(stringOut.output, expectedOutput)
        } else {
            interpreter.interpret(program)
        }

        assertEquals(Lox.isOk(), successExpected)
    }
}