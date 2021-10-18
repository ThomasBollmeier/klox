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
    private val closure: Environment,
    private val isInitializer: Boolean = false
) : Value(), Callable {

    private val instanceRefName = "this"
    private val superInstanceRefName = "super"

    fun bind(instance: Instance): Function {
        val newClosure = Environment(closure)
        newClosure.define(instanceRefName, instance)
        if (instance.cls.superClass != null) {
            newClosure.define(superInstanceRefName, instance.superInstance())
        }
        return Function(parameters, block, newClosure, isInitializer)
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
                if (!isInitializer) {
                    Nil()
                } else {
                    closure.getValue(instanceRefName) ?: Nil()
                }
            } catch (ret: ReturnEvent) {
                if (!isInitializer) {
                    ret.value
                } else {
                    closure.getValue(instanceRefName) ?: Nil()
                }
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

class Class(
    val name: String,
    val superClass: Class?,
    private val methods: Map<String, Pair<Function, MethodCategory>>
    ) : Value(), Callable {

    fun getClassMethod(name: String) = getMethod(name, MethodCategory.CLASS_METHOD)

    fun getInstanceMethod(name: String) = getMethod(name, MethodCategory.INSTANCE_METHOD)

    fun getGetterMethod(name: String) = getMethod(name, MethodCategory.GETTER)

    private fun getMethod(name: String, category: MethodCategory): Function? {
        val entry = methods[name]
        return if (entry != null) {
            val (method, catg) = entry
            if (catg == category) {
                method
            } else {
                null
            }
        } else superClass?.getMethod(name, category)
    }

    override fun isEqual(other: Value): Bool {
        return if (other is Class) {
            Bool(this == other)
        } else {
            Bool(false)
        }
    }

    override fun arity(): Int {
        val initializer = getInstanceMethod("init")
        return initializer?.arity() ?: 0
    }

    override fun call(interpreter: Interpreter, arguments: List<Value>): Value {
        val instance = Instance(this)
        getInstanceMethod("init")?.bind(instance)?.call(interpreter, arguments)
        return instance
    }

    override fun toString(): String {
        return "<class $name>"
    }

}

class Instance(val cls: Class) : Value() {

    private val fields = mutableMapOf<String, Value>()

    fun superInstance(): Value {
        return if (cls.superClass != null) {
            val ret = Instance(cls.superClass)
            for ((key, value) in fields) {
                ret.fields[key] = value
            }
            ret
        } else {
            Nil()
        }
    }

    fun get(name: Token): Value {
        return if (name.lexeme in fields) {
            fields[name.lexeme]!!
        } else {
            val method = cls.getInstanceMethod(name.lexeme)
            if (method != null) {
                method.bind(this)
            } else {
                val accessMethod = cls.getGetterMethod(name.lexeme)?.bind(this)
                if (accessMethod != null) {
                    Getter(accessMethod)
                } else {
                    throw InterpreterError(name, "Undefined property '${name.lexeme}.")
                }
            }
        }
    }

    fun set(name: Token, value: Value) {
        fields[name.lexeme] = value
    }

    override fun toString(): String {
        return "<instance ${cls.name}>"
    }

}

class Getter(private val accessFn: Function): Value(), Callable by accessFn

