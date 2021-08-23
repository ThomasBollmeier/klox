package de.tbollmeier.klox

import java.io.File
import kotlin.system.exitProcess

object Lox {

    private var hadError = false

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
    }

    private fun run(source: String) {
        hadError = false
        val tokens = Scanner(source).scanTokens()

        for (token in tokens) {
            println(token)
        }
    }

    fun error(line: Int, message: String) {
        report(line, "", message)
    }

    fun report(line: Int, where: String, message: String) {
        System.err.println("[$line] Error $where: $message")
        hadError = true
    }

}