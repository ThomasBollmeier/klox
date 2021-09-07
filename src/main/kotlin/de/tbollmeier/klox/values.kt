package de.tbollmeier.klox

sealed class Value()

class Nil() : Value()

class Bool(val value: Boolean) : Value()

class Number(val value: Double) : Value()

class Str(val value: String) : Value()

