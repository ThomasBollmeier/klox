package de.tbollmeier.klox

import kotlin.math.abs

abstract class Value {

    open fun isTruthy() = true

    open fun isEqual(other: Value) = Bool(this == other)

    fun isNotEqual(other: Value) = Bool(!isEqual(other).isTruthy())

}

class Nil : Value() {

    override fun isTruthy() = false

    override fun isEqual(other: Value) = Bool(other is Nil)

    override fun toString(): String = "nil"

}

class Bool(private val value: Boolean) : Value() {

    override fun isTruthy() = value

    override fun isEqual(other: Value): Bool {
        return if (other is Bool) {
            Bool(value == other.value)
        } else {
            Bool(false)
        }
    }

    override fun toString() = value.toString()

}

class Number(private val value: Double) : Value() {

    companion object {
        const val EPS = 1E-12
    }

    fun negate(): Number = Number(-value)

    fun add(other: Number) = Number(value + other.value)

    fun subtract(other: Number) = Number(value - other.value)

    fun multiply(other: Number) = Number(value * other.value)

    fun divide(other: Number) = Number(value / other.value)

    fun isGreater(other: Number) = Bool(value > other.value)

    fun isGreaterOrEq(other: Number) = Bool(value >= other.value)

    fun isLess(other: Number) = Bool(value < other.value)

    fun isLessOrEq(other: Number) = Bool(value <= other.value)

    override fun isEqual(other: Value): Bool {
        return if (other is Number) {
            Bool(abs(value - other.value) < EPS)
        } else {
            Bool(false)
        }
    }

    override fun toString(): String {
        var ret = value.toString()
        if (ret.endsWith(".0")) {
            ret = ret.substring(0, ret.length - 2)
        }
        return ret
    }
}

class Str(private val value: String) : Value() {

    fun concat(other: Str) = Str(value + other.value)

    override fun isEqual(other: Value): Bool {
        return if (other is Str) {
            Bool(value == other.value)
        } else {
            Bool(false)
        }
    }

    override fun toString() = value
}

interface Callable {
    fun arity(): Int
    fun call(interpreter: Interpreter, arguments: List<Value>): Value
}

class Function(
    private val parameters: List<Token>,
    private val block: BlockStmt,
    private val closure: Environment
) : Value(), Callable {

    fun bind(instance: Instance): Function {
        val newClosure = Environment(closure)
        newClosure.define("this", instance)
        return Function(parameters, block, newClosure)
    }

    override fun arity(): Int {
        return parameters.size
    }

    override fun call(interpreter: Interpreter, arguments: List<Value>): Value {
        val oldEnv = interpreter.environment
        try {
            interpreter.environment = Environment(closure)
            val env = interpreter.newScope()
            parameters.zip(arguments).forEach {
                env.define(it.first.lexeme, it.second)
            }
            return try {
                block.accept(interpreter)
                Nil()
            } catch (ret: ReturnEvent) {
                ret.value
            }
        } finally {
            interpreter.environment = oldEnv
        }
    }

    override fun isEqual(other: Value): Bool {
        return if (other is Function) {
            Bool(this === other)
        } else {
            Bool(false)
        }
    }

    override fun toString(): String {
        val paramsStr = parameters.joinToString(", ") { it.lexeme }
        return "<fun ($paramsStr)>"
    }

}

class Class(val name: String, private val methods: Map<String, Function>) : Value(), Callable {

    fun hasMethod(name: String) = name in methods

    fun getMethod(name: String) = methods[name]

    override fun isEqual(other: Value): Bool {
        return if (other is Class) {
            Bool(this == other)
        } else {
            Bool(false)
        }
    }

    override fun arity(): Int {
        return if (hasMethod("init")) {
            getMethod("init")!!.arity()
        } else {
            0
        }
    }

    override fun call(interpreter: Interpreter, arguments: List<Value>): Value {
        val instance = Instance(this)

        if (hasMethod("init")) {
            getMethod("init")?.bind(instance)?.call(interpreter, arguments)
        }

        return instance
    }

    override fun toString(): String {
        return "<class $name>"
    }

}

class Instance(private val cls: Class) : Value() {

    private val fields = mutableMapOf<String, Value>()

    fun get(name: Token): Value {
        return if (name.lexeme in fields) {
            fields[name.lexeme]!!
        } else if (cls.hasMethod(name.lexeme)) {
            val method = cls.getMethod(name.lexeme)!!
            method.bind(this)
        } else {
            throw InterpreterError(name, "Undefined property '${name.lexeme}.")
        }
    }

    fun set(name: Token, value: Value) {
        fields[name.lexeme] = value
    }

    override fun toString(): String {
        return "<instance ${cls.name}>"
    }

}

