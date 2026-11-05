package com.ownstd.project.pincard.internal.data.model

sealed class GenerateLooksResult {
    data class Success(val lookIds: List<Int>) : GenerateLooksResult()
    data class NotEnoughClothes(val missing: List<MissingCategory> = emptyList()) : GenerateLooksResult()
    data class NetworkError(val exception: Throwable) : GenerateLooksResult()
}
