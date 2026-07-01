package com.ownstd.project.storage

import com.russhwolf.settings.Settings

internal actual fun createSecureSettings(): Settings = KeychainSettings()
