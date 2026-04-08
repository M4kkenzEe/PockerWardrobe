package com.ownstd.project.core.compose.foundation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.DisposableEffect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

fun interface Cancellation {
    fun cancel()
}

abstract class SideEffect<out T : Any> {
    abstract fun subscribe(observer: (T) -> Unit): Cancellation
}

fun <T : Any> MutableSharedFlow<T>.toSideEffect(): SideEffect<T> =
    object : SideEffect<T>() {
        private val scope: CoroutineScope = MainScope()
        override fun subscribe(observer: (T) -> Unit): Cancellation {
            val job = onEach { observer(it) }.launchIn(scope)
            return Cancellation { job.cancel() }
        }
    }

@Composable
@NonRestartableComposable
fun <T : Any> SideEffect<T>.handle(
    key: Any? = Unit,
    onEffect: (T) -> Unit,
) {
    DisposableEffect(key) {
        val cancellation = subscribe { effect -> onEffect(effect) }
        onDispose { cancellation.cancel() }
    }
}
