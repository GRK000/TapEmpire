package com.example.juego.ui.screens.minigames

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import kotlin.math.*
import kotlin.random.Random

data class Asteroid(
    val id: Int,
    var x: Float, var y: Float,
    val size: Float,
    val speed: Float,
    val rotation: Float,
    val rotSpeed: Float,
    val color: Color,
    val vertices: List<Float> // radii for irregular shape
)

data class Crystal(
    val id: Int,
    var x: Float, var y: Float,
    val speed: Float,
    val color: Color,
    val value: Int,
    var collected: Boolean = false
)

data class ShipParticle(
    var x: Float, var y: Float,
    var vx: Float, var vy: Float,
    var life: Float = 1f,
    val color: Color
)

@Composable
fun AsteroidDodgeScreen(
    onGameComplete: (performanceMultiplier: Double) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var phase by remember { mutableStateOf(DodgePhase.COUNTDOWN) }
    var countdown by remember { mutableIntStateOf(3) }
    var shipX by remember { mutableFloatStateOf(0f) }
    var shipY by remember { mutableFloatStateOf(0f) }
    var score by remember { mutableIntStateOf(0) }
    var crystalsCollected by remember { mutableIntStateOf(0) }
    var timeLeft by remember { mutableFloatStateOf(30f) }
    var health by remember { mutableIntStateOf(3) }
    var asteroids by remember { mutableStateOf(listOf<Asteroid>()) }
    var crystals by remember { mutableStateOf(listOf<Crystal>()) }
    var particles by remember { mutableStateOf(listOf<ShipParticle>()) }
    var engineTrail by remember { mutableStateOf(listOf<ShipParticle>()) }
    var idCounter by remember { mutableIntStateOf(0) }
    var canvasW by remember { mutableFloatStateOf(0f) }
    var canvasH by remember { mutableFloatStateOf(0f) }
    var invincibleTimer by remember { mutableFloatStateOf(0f) }
    var frameTime by remember { mutableFloatStateOf(0f) }

    // Initialize ship
    LaunchedEffect(canvasW, canvasH) {
        if (canvasW > 0 && canvasH > 0) {
            shipX = canvasW / 2f
            shipY = canvasH * 0.78f
        }
    }

    // Countdown
    LaunchedEffect(phase) {
        if (phase == DodgePhase.COUNTDOWN) {
            for (i in 3 downTo 1) { countdown = i; delay(800) }
            phase = DodgePhase.PLAYING
        }
    }

    // Game loop
    LaunchedEffect(phase) {
        if (phase != DodgePhase.PLAYING) return@LaunchedEffect
        val startTime = System.currentTimeMillis()
        val gameDuration = 30_000L

        while (phase == DodgePhase.PLAYING) {
            val elapsed = System.currentTimeMillis() - startTime
            timeLeft = ((gameDuration - elapsed) / 1000f).coerceAtLeast(0f)
            frameTime += 0.016f

            if (elapsed >= gameDuration || health <= 0) {
                phase = DodgePhase.FINISHED
                break
            }

            val difficulty = (elapsed / gameDuration.toFloat()).coerceIn(0f, 1f)

            // Spawn asteroids
            if (Random.nextFloat() < 0.03f + difficulty * 0.05f && canvasW > 0) {
                idCounter++
                val rng = Random
                val numVerts = 8
                asteroids = asteroids + Asteroid(
                    id = idCounter,
                    x = rng.nextFloat() * canvasW,
                    y = -60f,
                    size = 20f + rng.nextFloat() * 30f + difficulty * 15f,
                    speed = 2.5f + rng.nextFloat() * 3f + difficulty * 4f,
                    rotation = rng.nextFloat() * 360f,
                    rotSpeed = (rng.nextFloat() - 0.5f) * 4f,
                    color = listOf(Color(0xFF8B7355), Color(0xFF6B5344), Color(0xFF9C8C7A), Color(0xFF554433)).random(),
                    vertices = List(numVerts) { 0.7f + rng.nextFloat() * 0.6f }
                )
            }

            // Spawn crystals
            if (Random.nextFloat() < 0.015f + difficulty * 0.01f && canvasW > 0) {
                idCounter++
                val isRare = Random.nextFloat() < 0.15f
                crystals = crystals + Crystal(
                    id = idCounter,
                    x = Random.nextFloat() * (canvasW - 40f) + 20f,
                    y = -30f,
                    speed = 2f + Random.nextFloat() * 2f,
                    color = if (isRare) CoinGold else NeonCyan,
                    value = if (isRare) 30 else 10
                )
            }

            // Update asteroids
            asteroids = asteroids.map { it.copy(y = it.y + it.speed) }.filter { it.y < canvasH + 80f }

            // Update crystals
            crystals = crystals.map { c ->
                if (!c.collected) c.copy(y = c.y + c.speed) else c
            }.filter { it.y < canvasH + 40f && (!it.collected) }

            // Invincibility timer
            if (invincibleTimer > 0) invincibleTimer -= 0.016f

            // Collision: ship vs asteroids
            if (invincibleTimer <= 0) {
                val shipRadius = 22f
                for (ast in asteroids) {
                    val dx = shipX - ast.x
                    val dy = shipY - ast.y
                    val dist = sqrt(dx * dx + dy * dy)
                    if (dist < shipRadius + ast.size * 0.6f) {
                        health--
                        invincibleTimer = 1.5f
                        // Explosion particles
                        particles = particles + List(15) {
                            ShipParticle(
                                x = shipX, y = shipY,
                                vx = (Random.nextFloat() - 0.5f) * 8f,
                                vy = (Random.nextFloat() - 0.5f) * 8f,
                                color = listOf(NeonRed, NeonOrange, CoinGold).random()
                            )
                        }
                        break
                    }
                }
            }

            // Collision: ship vs crystals
            crystals = crystals.map { crystal ->
                val dx = shipX - crystal.x
                val dy = shipY - crystal.y
                val dist = sqrt(dx * dx + dy * dy)
                if (dist < 35f && !crystal.collected) {
                    score += crystal.value
                    crystalsCollected++
                    particles = particles + List(8) {
                        ShipParticle(
                            x = crystal.x, y = crystal.y,
                            vx = (Random.nextFloat() - 0.5f) * 5f,
                            vy = (Random.nextFloat() - 0.5f) * 5f,
                            color = crystal.color
                        )
                    }
                    crystal.copy(collected = true)
                } else crystal
            }

            // Update particles
            particles = particles.map {
                it.copy(x = it.x + it.vx, y = it.y + it.vy, life = it.life - 0.03f)
            }.filter { it.life > 0 }

            // Engine trail
            engineTrail = (engineTrail + ShipParticle(
                x = shipX + (Random.nextFloat() - 0.5f) * 8f,
                y = shipY + 18f,
                vx = (Random.nextFloat() - 0.5f) * 1f,
                vy = 2f + Random.nextFloat() * 2f,
                color = listOf(NeonCyan, NeonPurple).random()
            )).map {
                it.copy(x = it.x + it.vx, y = it.y + it.vy, life = it.life - 0.06f)
            }.filter { it.life > 0 }.takeLast(30)

            // Score from survival
            score += 1

            delay(16)
        }
    }

    Box(
        modifier = modifier.fillMaxSize().background(
            Brush.verticalGradient(
                colors = listOf(Color(0xFF020010), Color(0xFF0A0028), Color(0xFF050015))
            )
        )
    ) {
        when (phase) {
            DodgePhase.COUNTDOWN -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("☄️", fontSize = 64.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Asteroid Dodge", fontSize = 32.sp, fontWeight = FontWeight.Black, color = NeonOrange)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(stringResource(R.string.asteroid_subtitle), fontSize = 16.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(32.dp))
                        Text("$countdown", fontSize = 72.sp, fontWeight = FontWeight.Black, color = NeonCyan)
                    }
                }
            }

            DodgePhase.PLAYING -> {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                shipX = (shipX + dragAmount.x).coerceIn(25f, canvasW - 25f)
                                shipY = (shipY + dragAmount.y).coerceIn(canvasH * 0.3f, canvasH - 30f)
                            }
                        }
                ) {
                    canvasW = size.width
                    canvasH = size.height

                    // Stars background
                    drawBackgroundStarfield(this)

                    // Engine trail
                    engineTrail.forEach { p ->
                        drawCircle(
                            color = p.color.copy(alpha = p.life * 0.5f),
                            radius = 3f + p.life * 4f,
                            center = Offset(p.x, p.y)
                        )
                    }

                    // Crystals
                    crystals.forEach { crystal ->
                        if (!crystal.collected) {
                            val pulse = 1f + sin(frameTime * 6f + crystal.id) * 0.2f
                            drawCircle(
                                color = crystal.color.copy(alpha = 0.15f),
                                radius = 20f * pulse,
                                center = Offset(crystal.x, crystal.y)
                            )
                            // Diamond shape
                            val path = Path().apply {
                                moveTo(crystal.x, crystal.y - 12f * pulse)
                                lineTo(crystal.x + 8f * pulse, crystal.y)
                                lineTo(crystal.x, crystal.y + 12f * pulse)
                                lineTo(crystal.x - 8f * pulse, crystal.y)
                                close()
                            }
                            drawPath(path, crystal.color)
                        }
                    }

                    // Asteroids
                    asteroids.forEach { ast ->
                        rotate(ast.rotation + frameTime * ast.rotSpeed * 50f, pivot = Offset(ast.x, ast.y)) {
                            val path = Path()
                            ast.vertices.forEachIndexed { i, r ->
                                val angle = (2f * PI.toFloat() / ast.vertices.size) * i
                                val px = ast.x + cos(angle) * ast.size * r
                                val py = ast.y + sin(angle) * ast.size * r
                                if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                            }
                            path.close()
                            drawPath(path, ast.color)
                            drawPath(path, ast.color.copy(alpha = 0.4f), style = androidx.compose.ui.graphics.drawscope.Stroke(2f))
                        }
                    }

                    // Ship
                    val shipAlpha = if (invincibleTimer > 0) {
                        if ((invincibleTimer * 10).toInt() % 2 == 0) 0.3f else 1f
                    } else 1f

                    val shipPath = Path().apply {
                        moveTo(shipX, shipY - 22f)
                        lineTo(shipX + 16f, shipY + 14f)
                        lineTo(shipX + 6f, shipY + 8f)
                        lineTo(shipX - 6f, shipY + 8f)
                        lineTo(shipX - 16f, shipY + 14f)
                        close()
                    }
                    drawPath(shipPath, NeonCyan.copy(alpha = shipAlpha))
                    // Ship glow
                    drawCircle(
                        color = NeonCyan.copy(alpha = 0.12f * shipAlpha),
                        radius = 35f,
                        center = Offset(shipX, shipY)
                    )

                    // Particles
                    particles.forEach { p ->
                        drawCircle(
                            color = p.color.copy(alpha = p.life),
                            radius = 2f + p.life * 3f,
                            center = Offset(p.x, p.y)
                        )
                    }
                }

                // HUD
                Column(
                    modifier = Modifier.fillMaxWidth().systemBarsPadding().padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("💎 $score", fontSize = 24.sp, fontWeight = FontWeight.Black, color = NeonCyan)
                            Text("Cristales: $crystalsCollected", fontSize = 12.sp, color = TextMuted)
                        }
                        Row {
                            repeat(3) { i ->
                                Text(
                                    if (i < health) "❤️" else "🖤",
                                    fontSize = 20.sp,
                                    modifier = Modifier.padding(horizontal = 2.dp)
                                )
                            }
                        }
                        Text(
                            "⏱️ ${timeLeft.toInt()}s",
                            fontSize = 18.sp, fontWeight = FontWeight.Bold,
                            color = if (timeLeft < 5f) NeonRed else TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { timeLeft / 30f },
                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                        color = if (timeLeft < 5f) NeonRed else NeonOrange,
                        trackColor = Disabled.copy(alpha = 0.3f)
                    )
                }
            }

            DodgePhase.FINISHED -> {
                val survived = health > 0
                val performance = ((score / 300.0) + (if (survived) 0.3 else 0.0) + (crystalsCollected * 0.02)).coerceIn(0.1, 2.0)
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                        Text(if (survived) "☄️" else "💥", fontSize = 56.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            if (survived) {
                                when {
                                    performance >= 1.5 -> stringResource(R.string.asteroid_result_amazing)
                                    performance >= 1.0 -> stringResource(R.string.asteroid_result_good)
                                    else -> stringResource(R.string.asteroid_result_ok)
                                }
                            } else "💥",
                            fontSize = 26.sp, fontWeight = FontWeight.Black,
                            color = if (performance >= 1.5) CoinGold else if (survived) NeonCyan else NeonRed
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(stringResource(R.string.billiards_score, score), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = CoinGold)
                        Text("💎 $crystalsCollected", fontSize = 16.sp, color = NeonCyan)
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = { onGameComplete(performance) },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                        ) { Text(stringResource(R.string.btn_collect_reward), fontSize = 16.sp, fontWeight = FontWeight.Bold) }
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = onCancel) { Text(stringResource(R.string.btn_back), color = TextMuted) }
                    }
                }
            }
        }
    }
}

private fun drawBackgroundStarfield(scope: DrawScope) {
    val rng = Random(123)
    for (i in 0 until 100) {
        scope.drawCircle(
            color = Color.White.copy(alpha = rng.nextFloat() * 0.35f + 0.05f),
            radius = rng.nextFloat() * 1.5f + 0.3f,
            center = Offset(rng.nextFloat() * scope.size.width, rng.nextFloat() * scope.size.height)
        )
    }
}

enum class DodgePhase { COUNTDOWN, PLAYING, FINISHED }
