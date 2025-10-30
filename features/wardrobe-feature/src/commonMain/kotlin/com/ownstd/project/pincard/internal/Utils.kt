package com.ownstd.project.pincard.internal

fun String.replaceFragment(
    before: String = "localhost",
    after: String = "192.168.0.20"
): String {
    return this.replace(before, after)
}
