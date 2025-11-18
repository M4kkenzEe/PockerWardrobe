package com.ownstd.project.pincard.internal

fun String.replaceFragment(
    before: String = "localhost",
    after: String = "192.168.0.29"
): String {
    return this.replace(before, after)
}
