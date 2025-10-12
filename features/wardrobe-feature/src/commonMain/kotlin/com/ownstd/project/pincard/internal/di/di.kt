package com.ownstd.project.pincard.internal.di

import com.ownstd.project.pincard.internal.data.repository.LookRepositoryImpl
import com.ownstd.project.pincard.internal.presentation.viewmodel.WardrobeViewModel
import com.ownstd.project.pincard.internal.data.repository.WardrobeRepositoryImpl
import com.ownstd.project.pincard.internal.domain.repository.LookRepository
import com.ownstd.project.pincard.internal.domain.repository.WardrobeRepository
import com.ownstd.project.pincard.internal.domain.usecase.LookUseCase
import com.ownstd.project.pincard.internal.domain.usecase.WardrobeUseCase
import com.ownstd.project.pincard.internal.presentation.viewmodel.ConstructorViewModel
import com.ownstd.project.pincard.internal.presentation.viewmodel.LooksViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val pinCardModule = module {
    viewModel { ConstructorViewModel(wardrobeUseCase = get(), lookUseCase = get()) }
    viewModel { WardrobeViewModel(useCase = get()) }
    single { WardrobeUseCase(wardrobeRepository = get()) }
    factory<WardrobeRepository> {
        WardrobeRepositoryImpl(
            networkRepository = get(),
            storage = get()
        )
    }

    viewModel { LooksViewModel(useCase = get()) }
    single { LookUseCase(lookRepository = get()) }
    factory<LookRepository> {
        LookRepositoryImpl(
            networkRepository = get(),
            storage = get()
        )
    }
}
