package com.ownstd.project.profile.di

import com.ownstd.project.network.api.di.networkModule
import com.ownstd.project.profile.data.ProfileRepositoryImpl
import com.ownstd.project.profile.domain.ProfileRepository
import com.ownstd.project.profile.domain.ProfileUseCase
import com.ownstd.project.profile.presentation.ProfileViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val profileModule = module {
    includes(networkModule)

    factory<ProfileRepository> {
        ProfileRepositoryImpl(
            networkRepository = get(),
            tokenStorage = get()
        )
    }
    single { ProfileUseCase(repository = get()) }

    viewModel<ProfileViewModel> { ProfileViewModel(profileUseCase = get()) }
}