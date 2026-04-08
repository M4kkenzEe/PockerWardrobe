package com.ownstd.project.core.compose.foundation

sealed class Outcome<out T> {
    data class Success<out T>(val data: T) : Outcome<T>()
    data object Empty : Outcome<Nothing>()
    data class Error(val message: String) : Outcome<Nothing>()
}
