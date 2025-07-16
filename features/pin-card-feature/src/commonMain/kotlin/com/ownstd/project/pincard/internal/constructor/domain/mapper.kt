package com.ownstd.project.pincard.internal.constructor.domain

import com.ownstd.project.pincard.internal.constructor.data.model.LookItem
import com.ownstd.project.pincard.internal.data.model.Clothe

fun Clothe.toLookItem() = LookItem(
    size = 100,
    x = 0f,
    y = 0f,
    rotation = 0f,
    clothe = this
)
