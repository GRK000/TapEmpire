package com.example.juego.ui.screens.minigames

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.juego.R
import com.example.juego.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class FallingStar(
    val id: Int,
    var x: Float,
    var y: Float,
    val size: Float,
    val speed: Float,
    val rotationSpeed: Float,
    var rotation: Float = 0f,
    val color: Color,
    val points: Int,
    var caught: Boolean = false,
    var alpha: Float = 1f,
    var spawnTime: Long = System.currentTimeMillis()
)

@Composable
fun StarCatcherScreen(
    onGameComplete: (performanceMultiplier: Double) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var gameState by remember { mutableStateOf(StarCatcherState.COUNTDOWN) }
    var countdown by remember { mutableIntStateOf(3) }
    var score by remember { mutableIntStateOf(0) }
    var timeLeft by remember { mutableFloatStateOf(20f) }
    var stars by remember { mutableStateOf(listOf<FallingStar>()) }
    var starIdCounter by remember { mutableIntStateOf(0) }
    var missed by remember { mutableIntStateOf(0) }
    var combo by remember { mutableIntStateOf(0) }
    var maxCombo by remember { mutableIntStateOf(0) }
    var canvasWidth by remember { mutableFloatStateOf(0f) }
    var canvasHeight by remember { mutableFloatStateOf(0f) }
    var catchEffects by remember { mutableStateOf(listOf<CatchEffect>()) }

    // Countdown
    LaunchedEffect(gameState) {
        if (gameState == StarCatcherState.COUNTDOWN) {
            for (i in 3 downTo 1) {
                countdown = i
                delay(800)
            }
            gameState = StarCatcherState.PLAYING
        }
    }

    // Game loop
    LaunchedEffect(gameState) {
        if (gameState != StarCatcherState.PLAYING) return@LaunchedEffect
        val startTime = System.currentTimeMillis()
        val gameDuration = 20_000L

        while (gameState == StarCatcherState.PLAYING) {
            val elapsed = System.currentTimeMillis() - startTime
            timeLeft = ((gameDuration - elapsed) / 1000f).coerceAtLeast(0f)

            if (elapsed >= gameDuration) {
                gameState = StarCatcherState.FINISHED
                break
            }

            // Spawn stars
            val difficulty = (elapsed / gameDuration.toFloat()).coerceIn(0f, 1f)
            val spawnChance = 0.04f + difficulty * 0.06f
            if (Random.nextFloat() < spawnChance && canvasWidth > 0) {
                val isGolden = Random.nextFloat() < 0.08f + difficulty * 0.04f
                val isNebula = !isGolden && Random.nextFloat() < 0.15f
                starIdCounter++
                val newStar = FallingStar(
                    id = starIdCounter,
                    x = Random.nextFloat() * canvasWidth * 0.85f + canvasWidth * 0.075f,
                    y = -30f,
                    size = if (isGolden) 28f else if (isNebula) 22f else 18f + Random.nextFloat() * 8f,
                    speed = 2f + difficulty * 4f + Random.nextFloat() * 2f,
                    rotationSpeed = (Random.nextFloat() - 0.5f) * 6f,
                    color = when {
                        isGolden -> CoinGold
                        isNebula -> NeonPink
                        else -> listOf(NeonCyan, NeonPurple, NeonGreen, NeonOrange).random()
                    },
                    points = when {
                        isGolden -> 50
                        isNebula -> 25
                        else -> 10
                    }
                )
                stars = stars + newStar
            }

            // Update stars
            stars = stars.map { star ->
                star.copy(
                    y = star.y + star.speed,
                    rotation = star.rotation + star.rotationSpeed,
                    alpha = if (star.caught) (star.alpha - 0.08f).coerceAtLeast(0f) else star.alpha
                )
            }

            // Remove off-screen or faded stars
            val beforeCount = stars.count { !it.caught }
            stars = stars.filter { star ->
                if (star.caught) star.alpha > 0f
                else star.y < (canvasHeight + 50f)
            }
            val afterCount = stars.count { !it.caught }
            val missedNow = beforeCount - afterCount - stars.count { it.caught }
            if (missedNow > 0) {
                missed += missedNow
                combo = 0
            }

            // Update catch effects
            catchEffects = catchEffects
                .map { it.copy(alpha = it.alpha - 0.04f, radius = it.radius + 2f) }
                .filter { it.alpha > 0f }

            delay(16)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF050520),
                        Color(0xFF0A0A30),
                        Color(0xFF15103A)
                    )
                )
            )
    ) {
        when (gameState) {
            StarCatcherState.COUNTDOWN -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⭐", fontSize = 64.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Star Catcher", fontSize = 32.sp, fontWeight = FontWeight.Black, color = CoinGold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(stringResource(R.string.star_subtitle), fontSize = 16.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(32.dp))
                        Text("$countdown", fontSize = 72.sp, fontWeight = FontWeight.Black, color = NeonCyan)
                    }
                }
            }

            StarCatcherState.PLAYING -> {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                val tapRadius = 55f
                                val hitStar = stars
                                    .filter { !it.caught }
                                    .sortedByDescending { it.points }
                                    .firstOrNull { star ->
                                        val dx = offset.x - star.x
                                        val dy = offset.y - star.y
                                        dx * dx + dy * dy < (star.size + tapRadius) * (star.size + tapRadius)
                                    }
                                if (hitStar != null) {
                                    stars = stars.map {
                                        if (it.id == hitStar.id) it.copy(caught = true) else it
                                    }
                                    combo++
                                    if (combo > maxCombo) maxCombo = combo
                                    val comboBonus = if (combo >= 10) 3 else if (combo >= 5) 2 else 1
                                    score += hitStar.points * comboBonus
                                    catchEffects = catchEffects + CatchEffect(
                                        x = hitStar.x, y = hitStar.y,
                                        color = hitStar.color
                                    )
                                }
                            }
                        }
                ) {
                    canvasWidth = size.width
                    canvasHeight = size.height

                    // Draw background stars (static)
                    drawBackgroundStars(this)

                    // Draw catch effects
                    catchEffects.forEach { effect ->
                        drawCircle(
                            color = effect.color.copy(alpha = effect.alpha * 0.4f),
                            radius = effect.radius,
                            center = Offset(effect.x, effect.y)
                        )
                    }

                    // Draw falling stars
                    stars.forEach { star ->
                        if (star.alpha > 0.01f) {
                            rotate(star.rotation, pivot = Offset(star.x, star.y)) {
                                drawStar(
                                    center = Offset(star.x, star.y),
                                    outerRadius = star.size,
                                    innerRadius = star.size * 0.45f,
                                    color = star.color.copy(alpha = star.alpha),
                                    points = 5
                                )
                                // Glow
                                drawCircle(
                                    color = star.color.copy(alpha = star.alpha * 0.2f),
                                    radius = star.size * 1.8f,
                                    center = Offset(star.x, star.y)
                                )
                            }
                        }
                    }
                }

                // HUD
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .systemBarsPadding()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("⭐ $score", fontSize = 28.sp, fontWeight = FontWeight.Black, color = CoinGold)
                            if (combo > 2) {
                                Text(
                                    "🔥 Combo x$combo",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (combo >= 10) ComboLegendary else if (combo >= 5) ComboFire else ComboBasic
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "⏱️ ${timeLeft.toInt()}s",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (timeLeft < 5f) NeonRed else TextPrimary
                            )
                            Text("Perdidas: $missed", fontSize = 12.sp, color = TextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { timeLeft / 20f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = if (timeLeft < 5f) NeonRed else NeonCyan,
                        trackColor = Disabled.copy(alpha = 0.3f)
                    )
                }
            }

            StarCatcherState.FINISHED -> {
                val performance = calculateStarCatcherPerformance(score, missed, maxCombo)
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text(
                            text = when {
                                performance >= 1.8 -> stringResource(R.string.star_result_amazing)
                                performance >= 1.3 -> stringResource(R.string.star_result_good)
                                else -> stringResource(R.string.star_result_ok)
                            },
                            fontSize = 28.sp, fontWeight = FontWeight.Black,
                            color = when {
                                performance >= 1.8 -> CoinGold
                                performance >= 1.3 -> NeonCyan
                                else -> TextPrimary
                            }
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(stringResource(R.string.billiards_score, score), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = CoinGold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Max Combo: x$maxCombo", fontSize = 16.sp, color = NeonPurple)
                        Text("Estrellas perdidas: $missed", fontSize = 14.sp, color = TextMuted)
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = { onGameComplete(performance) },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            Text(stringResource(R.string.btn_collect_reward), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = onCancel) {
                            Text(stringResource(R.string.btn_back), color = TextMuted)
                        }
                    }
                }
            }
        }
    }
}

private fun calculateStarCatcherPerformance(score: Int, missed: Int, maxCombo: Int): Double {
    val basePerf = score / 200.0
    val comboBonus = maxCombo * 0.02
    val missPenalty = missed * 0.03
    return (basePerf + comboBonus - missPenalty).coerceIn(0.1, 2.0)
}

private fun drawBackgroundStars(scope: DrawScope) {
    val rng = Random(42)
    for (i in 0 until 80) {
        val x = rng.nextFloat() * scope.size.width
        val y = rng.nextFloat() * scope.size.height
        val brightness = rng.nextFloat() * 0.4f + 0.1f
        scope.drawCircle(
            color = Color.White.copy(alpha = brightness),
            radius = rng.nextFloat() * 1.5f + 0.5f,
            center = Offset(x, y)
        )
    }
}

private fun DrawScope.drawStar(center: Offset, outerRadius: Float, innerRadius: Float, color: Color, points: Int) {
    val path = Path()
    val angleStep = Math.PI.toFloat() / points
    for (i in 0 until points * 2) {
        val radius = if (i % 2 == 0) outerRadius else innerRadius
        val angle = angleStep * i - Math.PI.toFloat() / 2f
        val x = center.x + cos(angle) * radius
        val y = center.y + sin(angle) * radius
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color)
}

data class CatchEffect(
    val x: Float, val y: Float,
    val color: Color,
    val radius: Float = 10f,
    val alpha: Float = 1f
)

enum class StarCatcherState { COUNTDOWN, PLAYING, FINISHED }
