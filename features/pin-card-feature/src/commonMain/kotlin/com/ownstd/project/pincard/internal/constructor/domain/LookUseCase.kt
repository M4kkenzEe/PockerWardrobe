package com.ownstd.project.pincard.internal.constructor.domain

import com.ownstd.project.pincard.internal.constructor.data.model.Look
import com.ownstd.project.pincard.internal.constructor.data.model.LookItem
import com.ownstd.project.pincard.internal.data.model.Clothe
import kotlinx.coroutines.flow.MutableStateFlow

internal class LookUseCase(private val lookRepository: LookRepository) {


    fun addLookItem(clothe: Clothe) {

    }

    suspend fun addLook(name: String, lookItems: List<LookItem>) {
        val look = Look(
            name = name,
            lookItems = lookItems
        )
        lookRepository.addLook(look)
    }

    suspend fun removeLook() {

    }
}