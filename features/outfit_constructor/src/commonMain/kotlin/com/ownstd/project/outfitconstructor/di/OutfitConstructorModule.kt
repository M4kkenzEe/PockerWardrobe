package com.ownstd.project.outfitconstructor.di

import com.ownstd.project.network.api.NetworkRepository
import com.ownstd.project.outfitconstructor.internal.data.api.OutfitConstructorApi
import com.ownstd.project.outfitconstructor.internal.data.repository.OutfitConstructorRepositoryImpl
import com.ownstd.project.outfitconstructor.internal.domain.repository.OutfitConstructorRepository
import com.ownstd.project.outfitconstructor.internal.domain.usecase.AddLookUseCase
import com.ownstd.project.outfitconstructor.internal.domain.usecase.GetClothesUseCase
import com.ownstd.project.outfitconstructor.internal.presentation.root.OutfitConstructorViewModel
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module

val outfitConstructorModule = module {
    single {
        val networkRepository: NetworkRepository = get()
        OutfitConstructorApi(
            client = networkRepository.getClient(),
            baseUrl = networkRepository.baseUrl,
        )
    }

    single<OutfitConstructorRepository> {
        OutfitConstructorRepositoryImpl(api = get())
    }

    single { GetClothesUseCase(repository = get()) }
    single { AddLookUseCase(repository = get()) }

    viewModel {
        OutfitConstructorViewModel(
            getClothesUseCase = get(),
            addLookUseCase = get(),
        )
    }
}
