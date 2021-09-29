package de.tbollmeier.klox

class Environment(val enclosing: Environment? = null) {

    private val values: MutableMap<String, Value> = mutableMapOf()

    fun define(name: String, value: Value) {
        values[name] = value
    }

    fun getValue(name: Token): Value {
        return values[name.lexeme] ?: (enclosing?.getValue(name)
            ?: throw InterpreterError(name, "Undefined variable '${name.lexeme}'."))
    }

    fun assign(name: Token, value: Value) {
        when {
            name.lexeme in values -> values[name.lexeme] = value
            enclosing != null -> enclosing.assign(name, value)
            else -> throw InterpreterError(name, "Undefined variable '${name.lexeme}'.")
        }
    }
}