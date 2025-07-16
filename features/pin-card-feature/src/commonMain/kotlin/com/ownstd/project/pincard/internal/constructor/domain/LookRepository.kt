package com.ownstd.project.pincard.internal.constructor.domain

import com.ownstd.project.pincard.internal.constructor.data.model.Look

internal interface LookRepository {
    suspend fun addLookItem()
    suspend fun addLook(look: Look)
}