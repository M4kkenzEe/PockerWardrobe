package com.ownstd.project.wardrobe.di

import com.ownstd.project.network.api.NetworkRepository
import com.ownstd.project.wardrobe.internal.data.api.WardrobeApi
import com.ownstd.project.wardrobe.internal.data.repository.WardrobeRepositoryImpl
import com.ownstd.project.wardrobe.internal.domain.repository.WardrobeRepository
import com.ownstd.project.wardrobe.internal.domain.usecase.DeleteClotheUseCase
import com.ownstd.project.wardrobe.internal.domain.usecase.GetClotheByIdUseCase
import com.ownstd.project.wardrobe.internal.domain.usecase.GetClotheOutfitsUseCase
import com.ownstd.project.wardrobe.internal.domain.usecase.GetClothesUseCase
import com.ownstd.project.wardrobe.internal.domain.usecase.UpdateClotheUseCase
import com.ownstd.project.wardrobe.internal.presentation.detail.itemDetail.ItemDetailViewModel
import com.ownstd.project.wardrobe.internal.presentation.detail.itemEdit.ItemEditViewModel
import com.ownstd.project.wardrobe.internal.presentation.list.WardrobeViewModel
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module

val wardrobeModule = module {
    single {
        val networkRepository: NetworkRepository = get()
        WardrobeApi(
            client = networkRepository.getClient(),
            baseUrl = networkRepository.baseUrl,
        )
    }

    single<WardrobeRepository> {
        WardrobeRepositoryImpl(api = get())
    }

    single { GetClothesUseCase(repository = get()) }
    single { GetClotheByIdUseCase(repository = get()) }
    single { GetClotheOutfitsUseCase(repository = get()) }
    single { DeleteClotheUseCase(repository = get()) }
    single { UpdateClotheUseCase(repository = get()) }

    viewModel {
        WardrobeViewModel(
            getClothesUseCase = get(),
            deleteClotheUseCase = get(),
        )
    }

    viewModel { (clotheId: Int) ->
        ItemDetailViewModel(
            getClotheByIdUseCase = get(),
            getClotheOutfitsUseCase = get(),
            deleteClotheUseCase = get(),
            clotheId = clotheId,
        )
    }

    viewModel { (clotheId: Int) ->
        ItemEditViewModel(
            getClotheByIdUseCase = get(),
            updateClotheUseCase = get(),
            clotheId = clotheId,
        )
    }
}
