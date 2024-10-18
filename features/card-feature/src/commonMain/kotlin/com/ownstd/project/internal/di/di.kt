package com.ownstd.project.internal.di

import com.ownstd.project.internal.presentation.viewmodel.FirstViewModel
import com.ownstd.project.internal.presentation.viewmodel.SecondViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val cardModule = module {
    viewModel { FirstViewModel() }
    viewModel { SecondViewModel() }
}