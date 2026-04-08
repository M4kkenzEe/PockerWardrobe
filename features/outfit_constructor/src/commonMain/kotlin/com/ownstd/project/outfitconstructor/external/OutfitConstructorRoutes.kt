package com.ownstd.project.outfitconstructor.external

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class OutfitConstructorRoute(val lookId: Int? = null) : NavKey
