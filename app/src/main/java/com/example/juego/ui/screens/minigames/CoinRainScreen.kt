package com.example.juego.ui.screens.minigames

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.juego.R
import com.example.juego.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.*
import kotlin.random.Random

private data class RainCoin(
    val id: Int,
    var x: Float, var y: Float,
    val speed: Float,
    val size: Float,
    val value: Int,
    val color: Color,
    var caught: Boolean = false,
    var alpha: Float = 1f
)

@Composable
fun CoinRainScreen(
    onGameComplete: (Double) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var phase by remember { mutableIntStateOf(0) }
    var countdown by remember { mutableIntStateOf(3) }
    var score by remember { mutableIntStateOf(0) }
    var missed by remember { mutableIntStateOf(0) }
    var caught by remember { mutableIntStateOf(0) }
    var timeLeft by remember { mutableFloatStateOf(15f) }
    var coins by remember { mutableStateOf(listOf<RainCoin>()) }
    var idCounter by remember { mutableIntStateOf(0) }
    var canvasW by remember { mutableFloatStateOf(0f) }
    var canvasH by remember { mutableFloatStateOf(0f) }
    var sparkles by remember { mutableStateOf(listOf<Offset>()) }

    LaunchedEffect(Unit) {
        for (i in 3 downTo 1) { countdown = i; delay(800) }
        phase = 1
        val start = System.currentTimeMillis()
        while (phase == 1) {
            val elapsed = System.currentTimeMillis() - start
            timeLeft = (15f - elapsed / 1000f).coerceAtLeast(0f)
            if (timeLeft <= 0f) { phase = 2; break }

            val diff = (elapsed / 15000f).coerceIn(0f, 1f)
            // Spawn
            if (Random.nextFloat() < 0.06f + diff * 0.08f && canvasW > 0) {
                idCounter++
                val isGold = Random.nextFloat() < 0.12f
                val isBomb = !isGold && Random.nextFloat() < 0.08f
                coins = coins + RainCoin(
                    id = idCounter,
                    x = Random.nextFloat() * (canvasW - 40f) + 20f,
                    y = -20f,
                    speed = 3f + diff * 5f + Random.nextFloat() * 2f,
                    size = if (isGold) 24f else if (isBomb) 20f else 16f,
                    value = when { isGold -> 25; isBomb -> -10; else -> 5 },
                    color = when { isGold -> CoinGold; isBomb -> NeonRed; else -> Color(0xFFFFD700) }
                )
            }
            // Update
            coins = coins.map { c ->
                if (c.caught) c.copy(alpha = (c.alpha - 0.1f).coerceAtLeast(0f))
                else c.copy(y = c.y + c.speed)
            }
            val before = coins.count { !it.caught && it.y < canvasH + 30 }
            coins = coins.filter { it.y < canvasH + 30 || it.caught }.filter { it.alpha > 0f }
            val after = coins.count { !it.caught }
            val lost = before - after
            if (lost > 0) missed += lost

            sparkles = sparkles.filter { false } // clear old ones

            delay(16)
        }
    }

    Box(
        modifier = modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFF001020), Color(0xFF002040)))
        )
    ) {
        when (phase) {
            0 -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🌧️", fontSize = 64.sp)
                    Spacer(Modifier.height(16.dp))
                    Text(stringResource(R.string.rain_title), fontSize = 32.sp, fontWeight = FontWeight.Black, color = CoinGold)
                    Text(stringResource(R.string.rain_subtitle), fontSize = 14.sp, color = TextMuted)
                    Spacer(Modifier.height(32.dp))
                    Text("$countdown", fontSize = 72.sp, fontWeight = FontWeight.Black, color = NeonCyan)
                }
            }
            1 -> {
                Canvas(
                    modifier = Modifier.fillMaxSize().pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val hit = coins.filter { !it.caught }
                                .sortedByDescending { it.value }
                                .firstOrNull { c ->
                                    val dx = offset.x - c.x; val dy = offset.y - c.y
                                    dx * dx + dy * dy < (c.size + 40f) * (c.size + 40f)
                                }
                            if (hit != null) {
                                coins = coins.map { if (it.id == hit.id) it.copy(caught = true) else it }
                                score = (score + hit.value).coerceAtLeast(0)
                                if (hit.value > 0) caught++
                            }
                        }
                    }
                ) {
                    canvasW = size.width; canvasH = size.height
                    // Background rain lines
                    val rng = Random(42)
                    for (i in 0 until 30) {
                        val x = rng.nextFloat() * size.width
                        val y = rng.nextFloat() * size.height
                        drawLine(Color.White.copy(alpha = 0.03f), Offset(x, y), Offset(x - 2, y + 15f), 1f)
                    }

                    coins.forEach { coin ->
                        if (coin.alpha > 0f) {
                            // Glow
                            drawCircle(coin.color.copy(alpha = coin.alpha * 0.2f), coin.size * 2f, Offset(coin.x, coin.y))
                            // Coin
                            drawCircle(coin.color.copy(alpha = coin.alpha * 0.9f), coin.size, Offset(coin.x, coin.y))
                            // Highlight
                            drawCircle(Color.White.copy(alpha = coin.alpha * 0.4f), coin.size * 0.4f,
                                Offset(coin.x - coin.size * 0.2f, coin.y - coin.size * 0.2f))
                            // Bomb indicator
                            if (coin.value < 0) {
                                drawCircle(NeonRed.copy(alpha = coin.alpha * 0.5f), coin.size * 1.5f, Offset(coin.x, coin.y),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(2f))
                            }
                        }
                    }
                }
                // HUD
                Row(Modifier.fillMaxWidth().systemBarsPadding().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("💰 $score", fontSize = 24.sp, fontWeight = FontWeight.Black, color = CoinGold)
                    Text("⏱️ ${timeLeft.toInt()}s", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                        color = if (timeLeft < 4f) NeonRed else TextPrimary)
                }
            }
            2 -> {
                val perf = (score / 80.0).coerceIn(0.1, 2.0)
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                        Text("🌧️", fontSize = 56.sp)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            when { score >= 120 -> stringResource(R.string.rain_result_amazing); score >= 60 -> stringResource(R.string.rain_result_good); else -> stringResource(R.string.rain_result_ok) },
                            fontSize = 24.sp, fontWeight = FontWeight.Black,
                            color = if (score >= 120) CoinGold else TextPrimary
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(stringResource(R.string.rain_score, score), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = CoinGold)
                        Text(stringResource(R.string.rain_stats, caught, missed), fontSize = 14.sp, color = TextMuted)
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
}
