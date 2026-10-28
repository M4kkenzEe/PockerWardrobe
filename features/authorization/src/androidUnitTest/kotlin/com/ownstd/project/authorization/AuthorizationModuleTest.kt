package com.ownstd.project.authorization

import com.ownstd.project.authorization.di.authorizationModule
import com.ownstd.project.authorization.internal.data.repository.AuthorizationRepositoryImpl
import com.ownstd.project.authorization.internal.domain.AuthorizationRepository
import com.ownstd.project.authorization.internal.domain.LogoutUseCase
import com.ownstd.project.authorization.internal.presentation.AuthorizationViewModel
import com.ownstd.project.network.api.NetworkRepository
import com.ownstd.project.storage.TokenStorage
import io.ktor.client.engine.mock.MockEngine
import org.junit.After
import org.junit.Test
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.assertIs

// UI tests register their own view model module, so only this test covers the production wiring.
class AuthorizationModuleTest {

    // Token storage and the base URL live in device settings (keystore, androidx-startup) that the JVM lacks,
    // so the core:storage / core:network edges are swapped; every authorization definition stays the production one.
    private val platformEdges = module {
        single<TokenStorage> { InMemoryTokenStorage() }
        single<NetworkRepository> { networkRepositoryOver(MockEngine { error("wiring test makes no HTTP calls") }) }
    }

    private val koin = koinApplication { modules(authorizationModule(), platformEdges) }.koin

    @After
    fun tearDown() {
        koin.close()
    }

    @Test
    fun `production module builds the real repository`() {
        assertIs<AuthorizationRepositoryImpl>(koin.get<AuthorizationRepository>())
    }

    @Test
    fun `production module builds the logout use case`() {
        koin.get<LogoutUseCase>()
    }

    @Test
    fun `production module builds the authorization view model`() {
        koin.get<AuthorizationViewModel>()
    }
}
