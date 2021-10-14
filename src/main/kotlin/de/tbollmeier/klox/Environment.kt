package de.tbollmeier.klox

class Environment(val enclosing: Environment? = null) {

    private val values: MutableMap<String, Value> = mutableMapOf()

    fun define(name: String, value: Value) {
        values[name] = value
    }

    fun getValue(name: Token) =
        getValue(name.lexeme) ?:
            throw InterpreterError(name, "Undefined variable '${name}'.")

    fun getValue(name: String): Value? {
        return values[name] ?: (enclosing?.getValue(name))
    }

    fun assign(name: Token, value: Value) {
        when {
            name.lexeme in values -> values[name.lexeme] = value
            enclosing != null -> enclosing.assign(name, value)
            else -> throw InterpreterError(name, "Undefined variable '${name.lexeme}'.")
        }
    }
}