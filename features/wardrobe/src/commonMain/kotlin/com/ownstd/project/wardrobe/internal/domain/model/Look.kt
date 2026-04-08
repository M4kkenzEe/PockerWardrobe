package com.ownstd.project.wardrobe.internal.domain.model

/**
 * Краткое представление образа для использования на экране деталки вещи.
 * Полная модель образа будет определена в features/outfit при его создании.
 */
data class Look(
    val id: Int,
    val name: String,
    val imageUrl: String? = null,
)
