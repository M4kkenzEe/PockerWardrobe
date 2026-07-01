package com.ownstd.project.storage

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set

object EnvConfig {
    private val settings by lazy { Settings() }
    private const val KEY = "debug_server_env"

    const val PROD_URL = "https://api.clothis.tech/api/v1/"
    const val DEV_URL = "http://194.87.190.248:8082/api/v1/"

    val currentBaseUrl: String
        get() = if (settings.getStringOrNull(KEY) == "dev") DEV_URL else PROD_URL

    val isDev: Boolean
        get() = settings.getStringOrNull(KEY) == "dev"

    fun setDev(dev: Boolean) {
        settings[KEY] = if (dev) "dev" else "prod"
    }
}
