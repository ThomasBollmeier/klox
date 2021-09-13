package de.tbollmeier.klox

class Environment {

    private val values: MutableMap<String, Value> = mutableMapOf()

    fun define(name: String, value: Value) {
        values[name] = value
    }

    fun getValue(name: Token): Value {
        return values[name.lexeme] ?: throw InterpreterError(name, "Undefined variable '${name.lexeme}'.")
    }

    fun assign(name: Token, value: Value) {
        if (name.lexeme in values) {
            values[name.lexeme] = value
        } else {
            throw InterpreterError(name, "Undefined variable '${name.lexeme}'.")
        }
    }

}