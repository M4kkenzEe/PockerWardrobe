package com.ownstd.project.storage.di

import com.ownstd.project.storage.SessionManager
import com.ownstd.project.storage.SessionManagerImpl
import com.ownstd.project.storage.TokenStorage
import com.ownstd.project.storage.TokenStorageImpl
import com.ownstd.project.storage.createSecureSettings
import org.koin.dsl.module

val storageModule = module {
    single { createSecureSettings() }
    single<TokenStorage> { TokenStorageImpl(settings = get()) }
    single<SessionManager> { SessionManagerImpl() }
}
