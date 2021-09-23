package de.tbollmeier.klox

fun initBuiltinFunctions(global: Environment) {

    val clock = object: Value(), Callable {

        override fun arity() = 0

        override fun call(interpreter: Interpreter, arguments: List<Value>): Value {
            return Number(System.currentTimeMillis() / 1000.0)
        }

        override fun isEqual(other: Value): Bool {
            return Bool(other == this)
        }

        override fun toString(): String {
            return "<fun clock (builtin)>"
        }

    }

    global.define("clock", clock)

}