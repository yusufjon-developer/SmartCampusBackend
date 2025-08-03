package com.smartcampus.domain.utils

sealed class Either<out L, out R> {
    class Left<out L>(val value: L) : Either<L, Nothing>()
    class Right<out R>(val value: R) : Either<Nothing, R>()
}