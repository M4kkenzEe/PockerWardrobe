package com.ownstd.project.pincard.internal

import com.ownstd.project.network.api.ServerConfig

/**
 * Replaces incorrect server hostnames (0.0.0.0, localhost) with the correct server address.
 * Used for image URLs that come from the backend.
 */
fun String.replaceFragment(): String {
    return this
        .replace("0.0.0.0:8080", ServerConfig.SERVER_HOST)
        .replace("localhost:8080", ServerConfig.SERVER_HOST)
        .replace("0.0.0.0", ServerConfig.SERVER_HOST.substringBefore(":"))
        .replace("localhost", ServerConfig.SERVER_HOST.substringBefore(":"))
}
