package de.tbollmeier.klox

import kotlin.math.abs

sealed class Value {

    open fun isTruthy() = true

    abstract fun isEqual(other: Value): Bool

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
    fun call (interpreter: Interpreter, arguments: List<Value>): Value
}

class Function : Value(), Callable {

    override fun call(interpreter: Interpreter, arguments: List<Value>): Value {
        val args = arguments.joinToString(", ") { it.toString() }
        println("Calling function with $args")
        return Nil()
    }

    override fun isEqual(other: Value): Bool {
        return if (other is Function) {
            Bool(this === other)
        } else {
            Bool(false)
        }
    }

}

