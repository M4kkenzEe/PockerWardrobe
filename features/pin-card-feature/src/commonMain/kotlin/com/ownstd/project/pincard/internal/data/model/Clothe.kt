package com.ownstd.project.pincard.internal.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Clothe(
    val id: Int?,
    val name: String,
    val storeUrl: String,
    val imageUrl: String,
) {
    companion object {
        fun empty(): Clothe {
            return Clothe(
                id = null,
                name = "",
                storeUrl = "",
                imageUrl = ""
            )
        }
    }
}