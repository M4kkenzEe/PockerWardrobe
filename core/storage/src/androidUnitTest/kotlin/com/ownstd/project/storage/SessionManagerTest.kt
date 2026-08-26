package com.ownstd.project.storage

import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Test

class SessionManagerTest {

    private val sessionManager: SessionManager = SessionManagerImpl()

    // -----------------------------------------------------------------------
    // 1. notifySessionExpired: подписчик получает ровно 1 событие
    // -----------------------------------------------------------------------
    @Test
    fun `notifySessionExpired emits event to sessionExpiredEvent flow`() = runTest {
        val events = mutableListOf<Unit>()

        val collectJob = launch {
            sessionManager.sessionExpiredEvent.take(1).toList(events)
        }

        // Дать коллектору время начать подписку
        yield()

        sessionManager.notifySessionExpired()

        collectJob.join()

        assertEquals("Должно быть ровно одно событие", 1, events.size)
    }

    // -----------------------------------------------------------------------
    // 2. tryEmit не блокирует вызывающий поток при отсутствии подписчиков
    // -----------------------------------------------------------------------
    @Test
    fun `notifySessionExpired does not block when there are no subscribers`() = runTest {
        // Не должно бросить исключение или завис
        sessionManager.notifySessionExpired()
        sessionManager.notifySessionExpired()
        // Если дошли сюда — тест пройден
    }
}
