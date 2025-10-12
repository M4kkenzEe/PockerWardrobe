package com.ownstd.project.pincard.internal.data.model

sealed class LookRepositoryResult<out T> {
    data class Success<out T>(val data: T) : LookRepositoryResult<T>()
    data class BadRequest(val message: String) : LookRepositoryResult<Nothing>()
    data class InternalServerError(val message: String) : LookRepositoryResult<Nothing>()
    data class NetworkError(val exception: Throwable) : LookRepositoryResult<Nothing>()
    // Возможны другие ошибки
}
