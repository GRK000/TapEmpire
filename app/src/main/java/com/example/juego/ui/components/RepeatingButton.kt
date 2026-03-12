package com.example.juego.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * A Button that fires onClick once on tap, and repeatedly when held down.
 * Delay starts at 400ms and decreases to 50ms for accelerating purchase.
 *
 * Uses awaitPointerEventScope for robust press/release tracking so the
 * repeating job is always cancelled when the finger lifts, even if the
 * composable recomposes or the touch is cancelled.
 */
@Composable
fun RepeatingButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit
) {
    val scope = rememberCoroutineScope()
    val currentOnClick = rememberUpdatedState(onClick)
    val currentEnabled = rememberUpdatedState(enabled)

    // Keep a reference to the active repeating job so we can cancel it from
    // multiple places (pointer up, composable disposal, enabled change).
    val activeJob = remember { mutableStateOf<Job?>(null) }

    // Cancel any running job when the composable leaves composition or
    // when enabled changes to false.
    DisposableEffect(enabled) {
        if (!enabled) {
            activeJob.value?.cancel()
            activeJob.value = null
        }
        onDispose {
            activeJob.value?.cancel()
            activeJob.value = null
        }
    }

    val resolvedContainerColor = colors.containerColor
    val resolvedContentColor = colors.contentColor
    val disabledContainer = colors.disabledContainerColor
    val disabledContent = colors.disabledContentColor

    val bgColor = if (enabled) resolvedContainerColor else disabledContainer
    val fgColor = if (enabled) resolvedContentColor else disabledContent

    Surface(
        modifier = modifier.pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    // Wait for a press
                    val down = awaitPointerEvent()
                    val downChange = down.changes.firstOrNull { it.changedToDown() }
                    if (downChange == null || !currentEnabled.value) continue
                    downChange.consume()

                    // Fire once immediately
                    currentOnClick.value()

                    // Launch repeating job
                    val job = scope.launch {
                        delay(400)
                        var interval = 250L
                        while (isActive && currentEnabled.value) {
                            currentOnClick.value()
                            delay(interval)
                            if (interval > 50) interval = (interval - 30).coerceAtLeast(50)
                        }
                    }
                    activeJob.value = job

                    // Wait for release or cancel
                    try {
                        while (true) {
                            val event = awaitPointerEvent()
                            // Check if all pointers are up
                            if (event.changes.all { !it.pressed }) {
                                event.changes.forEach { it.consume() }
                                break
                            }
                        }
                    } finally {
                        job.cancel()
                        activeJob.value = null
                    }
                }
            }
        },
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        contentColor = fgColor
    ) {
        CompositionLocalProvider(LocalContentColor provides fgColor) {
            Row(
                modifier = Modifier.padding(contentPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                content()
            }
        }
    }
}
