package com.ownstd.project.authorization.di

import com.ownstd.project.authorization.internal.data.repository.AuthServiceImpl
import com.ownstd.project.authorization.internal.data.repository.AuthorizationRepositoryImpl
import com.ownstd.project.authorization.internal.domain.AuthService
import com.ownstd.project.authorization.internal.domain.AuthorizationRepository
import com.ownstd.project.authorization.internal.domain.LogoutUseCase
import com.ownstd.project.authorization.internal.presentation.AuthorizationViewModel
import com.ownstd.project.network.api.di.networkModule
import com.ownstd.project.storage.di.storageModule
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

fun authorizationModule(): Module = module {
    includes(networkModule)
    includes(storageModule)

    factory<AuthorizationRepository> {
        AuthorizationRepositoryImpl(
            authService = get(),
            tokenStorage = get()
        )
    }

    single { LogoutUseCase(authorizationRepository = get()) }

    viewModel<AuthorizationViewModel> { AuthorizationViewModel(authorizationRepository = get()) }

    factory<AuthService> { AuthServiceImpl(networkRepository = get()) }
}

