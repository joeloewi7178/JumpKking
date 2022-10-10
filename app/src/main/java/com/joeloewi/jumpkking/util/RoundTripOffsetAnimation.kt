package com.joeloewi.jumpkking.util

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.runtime.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

@Stable
sealed class RoundTripValue {
    open val isIdle: Boolean = false

    object Idle : RoundTripValue() {
        override val isIdle: Boolean = true
    }

    object Going : RoundTripValue()
    object TurningBack : RoundTripValue()
}

@Stable
class RoundTripState(
    private val maxOffset: Dp,
    private val minOffset: Dp
) {
    var roundTripValue by mutableStateOf<RoundTripValue>(RoundTripValue.Idle)
        private set
    var targetOffset by mutableStateOf(minOffset)
        private set

    suspend fun start() {
        suspendCancellableCoroutine { continuation ->
            if (continuation.isActive) {
                continuation.resumeWith(
                    kotlin.runCatching {
                        targetOffset = maxOffset
                        roundTripValue = RoundTripValue.Going
                    }
                )
            }
        }
    }

    private fun turnBack() {
        targetOffset = minOffset
        roundTripValue = RoundTripValue.TurningBack
    }

    private fun enterIdle() {
        roundTripValue = RoundTripValue.Idle
    }

    suspend fun doTurnBackOrEnterIdle(dp: Dp) {
        suspendCancellableCoroutine { continuation ->
            if (continuation.isActive) {
                continuation.resumeWith(
                    dp.runCatching {
                        if (equals(minOffset)) {
                            enterIdle()
                        } else if (equals(maxOffset)) {
                            turnBack()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun rememberRoundTripState(
    maxOffset: Dp = (-150).dp,
    minOffset: Dp = 0.dp,
): RoundTripState = remember(
    maxOffset,
    minOffset
) {
    RoundTripState(maxOffset, minOffset)
}

@Composable
fun animateRoundTripByDpAsState(
    roundTripState: RoundTripState = rememberRoundTripState(),
    coroutineScope: CoroutineScope = rememberCoroutineScope()
): State<Dp> = animateDpAsState(
    targetValue = roundTripState.targetOffset,
    finishedListener = {
        coroutineScope.launch {
            roundTripState.doTurnBackOrEnterIdle(it)
        }
    }
)