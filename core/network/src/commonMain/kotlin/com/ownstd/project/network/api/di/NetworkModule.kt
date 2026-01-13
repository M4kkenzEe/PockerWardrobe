package com.ownstd.project.network.api.di

import com.ownstd.project.network.api.NetworkRepository
import com.ownstd.project.network.api.ServerConfig
import com.ownstd.project.network.internal.NetworkRepositoryImpl
import org.koin.dsl.module

val networkModule = module {
    single<NetworkRepository> { NetworkRepositoryImpl(ServerConfig.BASE_URL) }
}