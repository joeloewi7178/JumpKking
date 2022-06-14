package com.joeloewi.jumpkking.util

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.runtime.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Stable
class RoundTripStateImpl(
    override val maxOffset: Dp = (-150).dp,
    override val minOffset: Dp = 0.dp,
) : RoundTripState {
    override val roundTripValue = mutableStateOf<RoundTripValue>(RoundTripValue.Idle)
    override val targetOffset = mutableStateOf(minOffset)
}

@Stable
sealed class RoundTripValue {
    open val isIdle: Boolean = false

    object Idle : RoundTripValue() {
        override val isIdle: Boolean = true
    }

    object Going : RoundTripValue()
    object TurningBack : RoundTripValue()
}

interface RoundTripState {
    val maxOffset: Dp
    val minOffset: Dp
    val roundTripValue: MutableState<RoundTripValue>
    val targetOffset: MutableState<Dp>

    fun start() {
        targetOffset.value = maxOffset
        roundTripValue.value = RoundTripValue.Going
    }

    fun turnBack() {
        targetOffset.value = minOffset
        roundTripValue.value = RoundTripValue.TurningBack
    }

    fun enterIdle() {
        roundTripValue.value = RoundTripValue.Idle
    }

    fun doTurnBackOrEnterIdle(dp: Dp) {
        if (dp == minOffset) {
            enterIdle()
        } else if (dp == maxOffset) {
            turnBack()
        }
    }
}

@Composable
fun rememberRoundTripState(
    maxOffset: Dp = (-150).dp,
    minOffset: Dp = 0.dp,
): RoundTripStateImpl = remember { RoundTripStateImpl(maxOffset, minOffset) }

@Composable
fun animateRoundTripByDpAsState(
    roundTripState: RoundTripStateImpl = rememberRoundTripState()
): State<Dp> {
    val targetOffset by remember { roundTripState.targetOffset }

    return animateDpAsState(
        targetValue = targetOffset,
        finishedListener = roundTripState::doTurnBackOrEnterIdle
    )
}