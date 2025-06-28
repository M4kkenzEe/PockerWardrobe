package com.ownstd.project.card.internal.di

import com.ownstd.project.authorization.di.authorizationModule
import com.ownstd.project.card.internal.presentation.viewmodel.RootScreenViewModel
import com.ownstd.project.pincard.internal.di.pinCardModule
import com.ownstd.project.wardrobe.di.wardrobeModule
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val cardModule = module {
    includes(pinCardModule, wardrobeModule, authorizationModule())
    viewModel<RootScreenViewModel> { RootScreenViewModel() }
}