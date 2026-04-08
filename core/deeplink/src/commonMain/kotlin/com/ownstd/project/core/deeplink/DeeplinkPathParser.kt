package com.ownstd.project.core.deeplink

class DeeplinkPathParser(private val path: String) {

    private var segments = emptyList<String>()
    private var cursor = 0

    fun parse(): DeeplinkRoute {
        cursor = 0
        segments = path.split("/").filter { it.isNotEmpty() }
        return parseRoute()
    }

    private fun parseRoute(): DeeplinkRoute = when (nextSegment()) {
        DeeplinkPostfix.ITEM.postfix    -> parseItemDetail()
        DeeplinkPostfix.LOOK.postfix    -> parseLookDetail()
        DeeplinkPostfix.PROFILE.postfix -> DeeplinkRoute.Profile
        else -> DeeplinkRoute.Unknown
    }

    private fun parseItemDetail(): DeeplinkRoute {
        val id = nextSegment()?.toIntOrNull() ?: return DeeplinkRoute.Unknown
        return DeeplinkRoute.ItemDetail(id)
    }

    private fun parseLookDetail(): DeeplinkRoute {
        val id = nextSegment()?.toIntOrNull() ?: return DeeplinkRoute.Unknown
        return DeeplinkRoute.LookDetail(id)
    }

    private fun nextSegment(): String? = segments.getOrNull(cursor++)
}
