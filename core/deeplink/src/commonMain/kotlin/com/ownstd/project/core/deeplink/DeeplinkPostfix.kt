package com.ownstd.project.core.deeplink

enum class DeeplinkPostfix(val postfix: String) {
    ITEM("item"),
    LOOK("look"),
    PROFILE("profile"),
    NONE("");

    companion object {
        fun from(postfix: String?): DeeplinkPostfix =
            entries.find { it.postfix == postfix } ?: NONE
    }
}
