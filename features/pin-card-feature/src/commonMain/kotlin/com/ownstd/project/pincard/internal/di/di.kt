package com.ownstd.project.pincard.internal.di

import com.ownstd.project.pincard.internal.presentation.viewmodel.WardrobeViewModel
import com.ownstd.project.pincard.internal.data.repository.WardrobeRepositoryImpl
import com.ownstd.project.pincard.internal.domain.WardrobeRepository
import com.ownstd.project.pincard.internal.domain.WardrobeUseCase
import com.ownstd.project.pincard.internal.presentation.viewmodel.ConstructorViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val pinCardModule = module {
    viewModel { ConstructorViewModel() }
    viewModel { WardrobeViewModel(useCase = get()) }
    single { WardrobeUseCase(wardrobeRepository = get()) }
    factory<WardrobeRepository> {
        WardrobeRepositoryImpl(
            networkRepository = get(),
            storage = get()
        )
    }
}
