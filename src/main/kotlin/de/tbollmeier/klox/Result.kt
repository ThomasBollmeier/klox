package de.tbollmeier.klox

sealed class Result<T, E>
class Ok<T, E>(val result: T) : Result<T, E>()
class Err<T, E>(val message: E) : Result<T, E>()