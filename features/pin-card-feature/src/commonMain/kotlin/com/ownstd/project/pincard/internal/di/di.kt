package com.ownstd.project.pincard.internal.di

import com.ownstd.project.pincard.internal.presentation.viewmodel.ConstructorViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val pinCardModule = module {
    viewModel { ConstructorViewModel() }
}