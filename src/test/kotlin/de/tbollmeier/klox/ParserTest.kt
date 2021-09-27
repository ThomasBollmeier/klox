package de.tbollmeier.klox

import org.testng.Assert.*
import org.testng.annotations.Test

class ParserTest {

    @Test
    fun `parses program successfully`() {
        testExpression(
            "2 + 20 * (4 - 2)",
            "(+ 2.0 (* 20.0 (group (- 4.0 2.0))))"
        )
    }

    @Test
    fun `parses logical expression`() {

        testExpression(
            "a == 1 and b == 2 or c < 3",
            "(or (and (== (var a) 1.0) (== (var b) 2.0)) (< (var c) 3.0))"
        )

    }

    @Test
    fun `parses call expression`() {

        testExpression(
            expr = "f(0)(g(x))",
            expectedAst = "(call (call (var f) 0.0) (call (var g) (var x)))"
        )

    }

    @Test
    fun `function expression`() {

        testExpression(
            expr = "fun (a, b) { return a + b; }",
            expectedAst = "(fun (a b))"
        )

    }

    private fun testExpression(expr: String, expectedAst: String) {

        val source = "$expr;"
        val scanner = Scanner(source)
        val tokens = scanner.scanTokens()
        val parser = Parser(tokens)
        val program = parser.parse()

        assertNotNull(program)

        val exprStmt = program.statements[0] as? ExpressionStmt

        assertNotNull(exprStmt)

        assertEquals(AstPrinter().print(exprStmt!!.expression), expectedAst)

    }

}