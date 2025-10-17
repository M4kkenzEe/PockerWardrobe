package com.ownstd.project.network.api.di

import com.ownstd.project.network.api.NetworkRepository
import com.ownstd.project.network.internal.NetworkRepositoryImpl
import org.koin.dsl.module

val networkModule = module {
    single<NetworkRepository> { NetworkRepositoryImpl("http://192.168.0.18:8080/") }
}