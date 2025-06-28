package com.ownstd.project.authorization.di

import com.ownstd.project.authorization.internal.data.AuthorizationRepositoryImpl
import com.ownstd.project.authorization.internal.domain.AuthorizationRepository
import com.ownstd.project.authorization.internal.presentation.AuthorizationViewModel
import com.ownstd.project.authorization.internal.storage.TokenStorage
import com.ownstd.project.authorization.internal.storage.TokenStorageImpl
import com.ownstd.project.network.di.networkModule
import com.russhwolf.settings.Settings
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

fun authorizationModule(): Module = module {
    includes(networkModule())
    single { Settings() }
    single<TokenStorage> { TokenStorageImpl(settings = get()) }
    factory<AuthorizationRepository> {
        AuthorizationRepositoryImpl(
            authService = get(),
            tokenStorage = get()
        )
    }

    viewModel<AuthorizationViewModel> { AuthorizationViewModel(authorizationRepository = get()) }
}

