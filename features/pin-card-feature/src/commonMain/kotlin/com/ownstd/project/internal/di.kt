package com.ownstd.project.internal

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val pinCardModule = module {
    viewModel { ConstructorViewModel() }
}