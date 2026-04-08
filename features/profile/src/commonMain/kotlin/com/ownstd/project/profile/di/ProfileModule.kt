package com.ownstd.project.profile.di

import com.ownstd.project.profile.internal.data.api.ProfileApi
import com.ownstd.project.profile.internal.data.repository.ProfileRepositoryImpl
import com.ownstd.project.profile.internal.domain.repository.ProfileRepository
import com.ownstd.project.profile.internal.domain.usecase.GetProfileUseCase
import com.ownstd.project.profile.internal.domain.usecase.GetUserSizesUseCase
import com.ownstd.project.profile.internal.domain.usecase.ProfileLogoutUseCase
import com.ownstd.project.profile.internal.domain.usecase.UpdateProfileUseCase
import com.ownstd.project.profile.internal.domain.usecase.UpdateUserSizesUseCase
import com.ownstd.project.profile.internal.presentation.detail.editProfile.EditProfileViewModel
import com.ownstd.project.profile.internal.presentation.detail.sizes.SizesViewModel
import com.ownstd.project.profile.internal.presentation.root.ProfileViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val profileModule = module {
    single { ProfileApi(get()) }
    single<ProfileRepository> { ProfileRepositoryImpl(get(), get()) }

    single { GetProfileUseCase(get()) }
    single { UpdateProfileUseCase(get()) }
    single { ProfileLogoutUseCase(get()) }
    single { GetUserSizesUseCase(get()) }
    single { UpdateUserSizesUseCase(get()) }

    viewModel { ProfileViewModel(get(), get()) }
    viewModel { EditProfileViewModel(get(), get()) }
    viewModel { SizesViewModel(get(), get()) }
}
