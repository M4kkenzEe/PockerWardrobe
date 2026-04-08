package com.ownstd.project.outfit.di

import com.ownstd.project.network.api.NetworkRepository
import com.ownstd.project.outfit.internal.data.api.OutfitApi
import com.ownstd.project.outfit.internal.data.repository.OutfitRepositoryImpl
import com.ownstd.project.outfit.internal.domain.repository.OutfitRepository
import com.ownstd.project.outfit.internal.domain.usecase.DeleteLookUseCase
import com.ownstd.project.outfit.internal.domain.usecase.GetLookByIdUseCase
import com.ownstd.project.outfit.internal.domain.usecase.GetLooksUseCase
import com.ownstd.project.outfit.internal.domain.usecase.ShareLookUseCase
import com.ownstd.project.outfit.internal.presentation.detail.OutfitDetailViewModel
import com.ownstd.project.outfit.internal.presentation.list.OutfitViewModel
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module

val outfitModule = module {
    single {
        val networkRepository: NetworkRepository = get()
        OutfitApi(
            client = networkRepository.getClient(),
            baseUrl = networkRepository.baseUrl,
        )
    }

    single<OutfitRepository> {
        OutfitRepositoryImpl(api = get())
    }

    single { GetLooksUseCase(repository = get()) }
    single { GetLookByIdUseCase(repository = get()) }
    single { DeleteLookUseCase(repository = get()) }
    single { ShareLookUseCase(repository = get()) }

    viewModel {
        OutfitViewModel(
            getLooksUseCase = get(),
            deleteLookUseCase = get(),
            shareLookUseCase = get(),
        )
    }

    viewModel { (lookId: Int) ->
        OutfitDetailViewModel(
            getLookByIdUseCase = get(),
            deleteLookUseCase = get(),
            shareLookUseCase = get(),
            lookId = lookId,
        )
    }
}
