package com.ownstd.project.main.di

import com.ownstd.project.authorization.di.authorizationModule
import com.ownstd.project.outfit.di.outfitModule
import com.ownstd.project.outfitconstructor.di.outfitConstructorModule
import com.ownstd.project.profile.di.profileModule
import com.ownstd.project.wardrobe.di.wardrobeModule
import org.koin.dsl.module

val mainModule = module {
    includes(
        authorizationModule(),
        wardrobeModule,
        outfitModule,
        outfitConstructorModule,
        profileModule,
    )
}
