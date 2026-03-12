package com.example.juego.ui.screens.minigames

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.juego.R
import com.example.juego.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun TapFrenzyScreen(
    onGameComplete: (Double) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var phase by remember { mutableStateOf(0) } // 0=countdown, 1=playing, 2=done
    var countdown by remember { mutableIntStateOf(3) }
    var taps by remember { mutableIntStateOf(0) }
    var timeLeft by remember { mutableFloatStateOf(10f) }
    var buttonScale by remember { mutableFloatStateOf(1f) }

    LaunchedEffect(Unit) {
        for (i in 3 downTo 1) { countdown = i; delay(800) }
        phase = 1
        val start = System.currentTimeMillis()
        while (phase == 1) {
            timeLeft = (10f - (System.currentTimeMillis() - start) / 1000f).coerceAtLeast(0f)
            if (timeLeft <= 0f) { phase = 2; break }
            delay(30)
        }
    }

    Box(
        modifier = modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFF0A001A), Color(0xFF150030)))
        ),
        contentAlignment = Alignment.Center
    ) {
        when (phase) {
            0 -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("⚡", fontSize = 64.sp)
                    Spacer(Modifier.height(16.dp))
                    Text(stringResource(R.string.frenzy_title), fontSize = 32.sp, fontWeight = FontWeight.Black, color = NeonOrange)
                    Text(stringResource(R.string.frenzy_subtitle), fontSize = 14.sp, color = TextMuted)
                    Spacer(Modifier.height(32.dp))
                    Text("$countdown", fontSize = 72.sp, fontWeight = FontWeight.Black, color = NeonCyan)
                }
            }
            1 -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("⏱️ ${String.format("%.1f", timeLeft)}s", fontSize = 22.sp, fontWeight = FontWeight.Bold,
                        color = if (timeLeft < 3f) NeonRed else TextPrimary)
                    Spacer(Modifier.height(8.dp))
                    Text("⚡ $taps taps", fontSize = 36.sp, fontWeight = FontWeight.Black, color = CoinGold)
                    Spacer(Modifier.height(40.dp))
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .scale(buttonScale)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(listOf(NeonOrange, NeonRed.copy(alpha = 0.8f), Color(0xFF330000)))
                            )
                            .pointerInput(Unit) {
                                detectTapGestures {
                                    taps++
                                    buttonScale = 0.9f
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("⚡", fontSize = 60.sp)
                    }
                    // Restore scale
                    LaunchedEffect(buttonScale) {
                        if (buttonScale < 1f) { delay(80); buttonScale = 1f }
                    }
                    Spacer(Modifier.height(20.dp))
                    Text("TPS: ${if (timeLeft < 10f) String.format("%.1f", taps / (10f - timeLeft).coerceAtLeast(0.1f)) else "0.0"}",
                        fontSize = 14.sp, color = TextMuted)
                }
            }
            2 -> {
                val tps = taps / 10f
                val perf = (tps / 8.0).coerceIn(0.1, 2.0) // 8 tps = max performance
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                    Text("⚡", fontSize = 56.sp)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        when { taps >= 80 -> stringResource(R.string.frenzy_result_amazing); taps >= 50 -> stringResource(R.string.frenzy_result_good); else -> stringResource(R.string.frenzy_result_ok) },
                        fontSize = 26.sp, fontWeight = FontWeight.Black,
                        color = if (taps >= 80) CoinGold else TextPrimary
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(stringResource(R.string.frenzy_stats, taps), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = CoinGold)
                    Text(stringResource(R.string.frenzy_tps, String.format("%.1f", tps)), fontSize = 16.sp, color = NeonCyan)
                    Spacer(Modifier.height(32.dp))
                    Button(onClick = { onGameComplete(perf) },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) { Text(stringResource(R.string.btn_collect_reward), fontSize = 16.sp, fontWeight = FontWeight.Bold) }
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = onCancel) { Text(stringResource(R.string.btn_back), color = TextMuted) }
                }
            }
        }
    }
}
