package de.tbollmeier.klox

import de.tbollmeier.klox.TokenType.EOF
import java.io.File
import kotlin.system.exitProcess

object Lox {

    private var hadError = false
    private var hadRuntimeError = false
    private var loggingOn = true

    private lateinit var interpreter: Interpreter

    private fun reset() {
        hadError = false
        hadRuntimeError = false
    }

    fun runPrompt() {
        interpreter = Interpreter()
        while (true) {
            print("> ")
            val line = readLine() ?: break
            run(line, exprAllowed = true)
        }
    }

    fun runFile(filePath: String) {
        interpreter = Interpreter()
        val source = File(filePath).readText()
        run(source)
        if (hadError) {
            exitProcess(65)
        }
        if (hadRuntimeError) {
            exitProcess(70)
        }
    }

    private fun run(source: String, exprAllowed: Boolean = false) {
        reset()
        loggingOn = false
        val program = parse(source)
        loggingOn = true
        if (!hadError) {
            interpreter.interpret(program)
        } else if (exprAllowed) {
            // check for expression
            reset()
            val expr = parseExpr(source)
            if (expr != null) {
                try {
                    println(interpreter.evaluate(expr))
                } catch (error: InterpreterError) {
                    runtimeError(error)
                }
            }
        }

    }

    fun error(line: Int, message: String) {
        report(line, "", message)
    }

    fun error(token: Token, message: String) {
        if (token.tokenType == EOF) {
            report(token.line, " at end", message)
        } else {
            report(token.line, "at '${token.lexeme}'", message)
        }
    }

    private fun report(line: Int, where: String, message: String) {
        if (loggingOn) {
            System.err.println("[$line] Error $where: $message")
        }
        hadError = true
    }

    fun runtimeError(error: InterpreterError) {
        val message = "${error.message} \n[line ${error.token.line}]"
        System.err.println(message)
        hadRuntimeError = true
    }

}