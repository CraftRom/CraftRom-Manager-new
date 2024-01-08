package com.craftrom.manager.core.time

import com.craftrom.manager.core.coroutines.ControlledRunner

internal suspend fun <T> ControlledRunner<T>.run(
    behavior: TimerActionBehavior,
    action: suspend () -> T
) {
    when (behavior) {
        TimerActionBehavior.Skip -> {
            joinPreviousOrRun(action)
        }
        TimerActionBehavior.Replace -> {
            cancelPreviousThenRun(action)
        }
        else -> {
            // Do nothing - wait is not supported by the controlled runner
        }
    }
}