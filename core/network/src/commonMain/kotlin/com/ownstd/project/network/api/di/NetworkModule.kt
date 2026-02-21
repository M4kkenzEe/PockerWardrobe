package com.ownstd.project.network.api.di

import com.ownstd.project.network.api.NetworkRepository
import com.ownstd.project.network.api.ServerConfig
import com.ownstd.project.network.internal.NetworkRepositoryImpl
import com.ownstd.project.storage.di.storageModule
import org.koin.dsl.module

val networkModule = module {
    includes(storageModule)
    single<NetworkRepository> {
        NetworkRepositoryImpl(
            baseUrl      = ServerConfig.BASE_URL,
            tokenStorage = get()
        )
    }
}
