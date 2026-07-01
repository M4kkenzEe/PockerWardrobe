package com.ownstd.project.storage.di

import com.ownstd.project.storage.TokenStorage
import com.ownstd.project.storage.TokenStorageImpl
import com.ownstd.project.storage.createSecureSettings
import org.koin.dsl.module

val storageModule = module {
    single { createSecureSettings() }
    single<TokenStorage> { TokenStorageImpl(settings = get()) }
}
