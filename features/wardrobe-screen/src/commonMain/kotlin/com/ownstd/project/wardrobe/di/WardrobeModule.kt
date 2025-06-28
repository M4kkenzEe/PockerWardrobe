package com.ownstd.project.wardrobe.di

import com.ownstd.project.network.di.networkModule
import org.koin.dsl.module

val wardrobeModule = module {
    includes(networkModule())

}