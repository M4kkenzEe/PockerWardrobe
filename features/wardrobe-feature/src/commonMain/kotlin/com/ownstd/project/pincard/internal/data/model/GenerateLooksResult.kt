package com.ownstd.project.pincard.internal.data.model

sealed class GenerateLooksResult {
    data class Success(val lookIds: List<Int>) : GenerateLooksResult()
    data object NotEnoughClothes : GenerateLooksResult()
    data class NetworkError(val exception: Throwable) : GenerateLooksResult()
}
