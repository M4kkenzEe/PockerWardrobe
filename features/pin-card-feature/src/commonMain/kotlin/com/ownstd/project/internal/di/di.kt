package com.ownstd.project.internal.di

import com.ownstd.project.internal.presentation.viewmodel.ConstructorViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val pinCardModule = module {
    viewModel { ConstructorViewModel() }
}