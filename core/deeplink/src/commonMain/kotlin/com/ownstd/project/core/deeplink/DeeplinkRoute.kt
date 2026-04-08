package com.ownstd.project.core.deeplink

sealed class DeeplinkRoute {
    data class ItemDetail(val clotheId: Int) : DeeplinkRoute()
    data class LookDetail(val lookId: Int) : DeeplinkRoute()
    data object Profile : DeeplinkRoute()
    data object Unknown : DeeplinkRoute()
}
