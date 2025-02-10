package com.ownstd.project.card.internal.di

import com.ownstd.project.pincard.internal.di.pinCardModule
import com.ownstd.project.card.internal.presentation.viewmodel.FirstViewModel
import com.ownstd.project.card.internal.presentation.viewmodel.SecondViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val cardModule = module {
    includes(pinCardModule)
    viewModel { FirstViewModel() }
    viewModel { SecondViewModel() }
}