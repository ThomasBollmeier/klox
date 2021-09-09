package de.tbollmeier.klox

import de.tbollmeier.klox.TokenType.EOF
import java.io.File
import kotlin.system.exitProcess

object Lox {

    private var hadError = false
    private var hadRuntimeError = false

    private fun reset() {
        hadError = false
        hadRuntimeError = false
    }

    fun runPrompt() {
        while (true) {
            print("> ")
            val line = readLine() ?: break
            run(line)
        }
    }

    fun runFile(filePath: String) {
        val source = File(filePath).readText()
        run(source)
        if (hadError) {
            exitProcess(65)
        }
        if (hadRuntimeError) {
            exitProcess(70)
        }
    }

    private fun run(source: String) {
        reset()
        val expr = parse(source)
        if (expr == null) {
            hadError = true
        }
        if (hadError) {
            return
        }
        Interpreter().interpret(expr!!)
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
        System.err.println("[$line] Error $where: $message")
        hadError = true
    }

    fun runtimeError(error: InterpreterError) {
        val message = "${error.message} \n[line ${error.token.line}]"
        System.err.println(message)
        hadRuntimeError = true
    }

}