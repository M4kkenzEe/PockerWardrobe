package com.ownstd.project.core.deeplink

interface IDeeplinkHandler {
    fun canHandle(uri: String): Boolean
    fun handle(uri: String)
}
