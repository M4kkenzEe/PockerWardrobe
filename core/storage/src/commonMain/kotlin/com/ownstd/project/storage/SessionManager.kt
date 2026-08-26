package com.ownstd.project.storage

import kotlinx.coroutines.flow.SharedFlow

interface SessionManager {
    val sessionExpiredEvent: SharedFlow<Unit>
    fun notifySessionExpired()
}
