package de.tbollmeier.klox

class AstPrinter : ExprVisitor<String> {

    fun print(expr: Expr): String {
        return expr.accept(this)
    }

    override fun visitBinaryExpr(binary: Binary): String {
        return "(${binary.operator.lexeme} ${print(binary.left)} ${print(binary.right)})"
    }

    override fun visitGroupingExpr(grouping: Grouping): String {
        return "(group ${print(grouping.expression)})"
    }

    override fun visitLiteralExpr(literal: Literal): String {
        return if (literal.value != null) {
            if (literal.value is String) {
                "\"${literal.value}\""
            } else {
                literal.value.toString()
            }
        } else {
            "nil"
        }
    }

    override fun visitUnaryExpr(unary: Unary): String {
        return "(${unary.operator.lexeme} ${print(unary.right)})"
    }

    override fun visitVariable(variable: Variable): String {
        return "(var ${variable.name.lexeme})"
    }

    override fun visitAssignExpr(assign: Assign): String {
        return "(assign ${assign.name.lexeme} ${print(assign.value)})"
    }

    override fun visitLogicalExpr(logical: Logical): String {
        return "(${logical.operator.lexeme} ${print(logical.left)} ${print(logical.right)})"
    }

    override fun visitCallExpr(call: Call): String {
        var ret = "(call ${print(call.callee)}"
        for (arg in call.arguments) {
            ret += " ${print(arg)}"
        }
        ret += ")"
        return ret
    }

    override fun visitFunExpr(fn: FunExpr): String {
        val params = fn.parameters.joinToString(" ") { it.lexeme }
        return "(fun ($params))"
    }

    override fun visitGet(get: Get): String {
        return "(get ${print(get.obj)} ${get.name.lexeme})"
    }

    override fun visitSet(set: Set): String {
        return "(set ${print(set.obj)} ${set.name.lexeme} ${print(set.value)})"
    }
}