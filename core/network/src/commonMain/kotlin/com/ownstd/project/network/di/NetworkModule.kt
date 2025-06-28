package com.ownstd.project.network.di

import com.ownstd.project.network.AuthClient
import com.ownstd.project.network.AuthService
import org.koin.core.module.Module
import org.koin.dsl.module


fun networkModule(baseUrl: String = "http://192.168.50.108:8080"): Module = module {
    single<AuthService> { AuthClient(baseUrl) }
}