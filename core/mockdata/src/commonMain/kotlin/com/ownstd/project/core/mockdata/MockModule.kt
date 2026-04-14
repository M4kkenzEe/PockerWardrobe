package com.ownstd.project.core.mockdata

import com.ownstd.project.outfit.internal.domain.repository.OutfitRepository
import com.ownstd.project.outfitconstructor.internal.domain.repository.OutfitConstructorRepository
import com.ownstd.project.profile.internal.domain.repository.ProfileRepository
import com.ownstd.project.wardrobe.internal.domain.repository.WardrobeRepository
import org.koin.dsl.module

val mockModule = module {
    single<WardrobeRepository> { MockWardrobeRepository() }
    single<OutfitRepository> { MockOutfitRepository() }
    single<ProfileRepository> { MockProfileRepository() }
    single<OutfitConstructorRepository> { MockOutfitConstructorRepository() }
}
