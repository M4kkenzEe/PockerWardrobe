package com.ownstd.project

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun getOSVersion(): String