package com.ownstd.project.storage.di

import com.ownstd.project.storage.TokenStorage
import com.ownstd.project.storage.TokenStorageImpl
import com.russhwolf.settings.Settings
import org.koin.dsl.module

val storageModule = module {
    single { Settings() }
    single<TokenStorage> { TokenStorageImpl(settings = get()) }
}