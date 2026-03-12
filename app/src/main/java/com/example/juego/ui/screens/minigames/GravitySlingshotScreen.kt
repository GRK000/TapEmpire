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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
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

data class Planet(
    val x: Float, val y: Float,
    val radius: Float,
    val mass: Float,
    val color: Color,
    val ringColor: Color? = null,
    val hasRing: Boolean = false
)

data class GravityTarget(
    val x: Float, val y: Float,
    val radius: Float,
    var hit: Boolean = false
)

data class Projectile(
    var x: Float, var y: Float,
    var vx: Float, var vy: Float,
    var trail: List<Offset> = emptyList(),
    var active: Boolean = true
)

@Composable
fun GravitySlingshotScreen(
    onGameComplete: (performanceMultiplier: Double) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var phase by remember { mutableStateOf(SlingshotPhase.AIMING) }
    var score by remember { mutableIntStateOf(0) }
    var level by remember { mutableIntStateOf(1) }
    var shotsLeft by remember { mutableIntStateOf(10) }
    var canvasW by remember { mutableFloatStateOf(0f) }
    var canvasH by remember { mutableFloatStateOf(0f) }

    var projectile by remember { mutableStateOf<Projectile?>(null) }
    var planets by remember { mutableStateOf(listOf<Planet>()) }
    var target by remember { mutableStateOf<GravityTarget?>(null) }
    var launchPos by remember { mutableStateOf(Offset.Zero) }
    var dragPos by remember { mutableStateOf(Offset.Zero) }
    var isDragging by remember { mutableStateOf(false) }
    var predictedPath by remember { mutableStateOf(listOf<Offset>()) }
    var frameTime by remember { mutableFloatStateOf(0f) }
    var trailParticles by remember { mutableStateOf(listOf<TrailParticle>()) }

    // Generate level
    LaunchedEffect(level, canvasW, canvasH) {
        if (canvasW <= 0 || canvasH <= 0) return@LaunchedEffect
        val rng = Random(level * 31)

        launchPos = Offset(canvasW * 0.15f, canvasH * 0.8f)

        planets = buildList {
            val numPlanets = ((level + 1) / 3 + 1).coerceAtMost(4)
            for (i in 0 until numPlanets) {
                add(Planet(
                    x = canvasW * (0.25f + rng.nextFloat() * 0.5f),
                    y = canvasH * (0.25f + rng.nextFloat() * 0.4f),
                    radius = 22f + rng.nextFloat() * 18f,
                    mass = 120f + rng.nextFloat() * 150f + level * 15f,
                    color = listOf(
                        Color(0xFF4A90D9), Color(0xFFE87461), Color(0xFF7BC67E),
                        Color(0xFFD4A574), Color(0xFF9B7FD4)
                    ).random(rng),
                    hasRing = rng.nextFloat() < 0.3f,
                    ringColor = NeonCyan.copy(alpha = 0.3f)
                ))
            }
        }

        target = GravityTarget(
            x = canvasW * (0.65f + rng.nextFloat() * 0.25f),
            y = canvasH * (0.1f + rng.nextFloat() * 0.3f),
            radius = (35f - level * 1f).coerceAtLeast(16f)
        )

        // Boost gravity of planets far from target to create alternative slingshot routes
        if (planets.isNotEmpty()) {
            val tgt = target!!
            val distances = planets.map { p ->
                sqrt((p.x - tgt.x) * (p.x - tgt.x) + (p.y - tgt.y) * (p.y - tgt.y))
            }
            val maxDist = distances.max()
            val minDist = distances.min()
            val range = (maxDist - minDist).coerceAtLeast(1f)

            planets = planets.mapIndexed { i, p ->
                val normalizedDist = ((distances[i] - minDist) / range).coerceIn(0f, 1f)
                // Far planets: up to 2.5x mass boost; close planets: unchanged
                val gravityBoost = 1f + normalizedDist * 1.5f
                p.copy(mass = p.mass * gravityBoost)
            }
        }

        projectile = null
        phase = SlingshotPhase.AIMING
    }

    // Physics
    LaunchedEffect(phase) {
        if (phase != SlingshotPhase.FLYING) return@LaunchedEffect
        val proj = projectile ?: return@LaunchedEffect

        while (phase == SlingshotPhase.FLYING && proj.active) {
            frameTime += 0.016f

            // Gravity from each planet
            for (planet in planets) {
                val dx = planet.x - proj.x
                val dy = planet.y - proj.y
                val distSq = (dx * dx + dy * dy).coerceAtLeast(400f)
                val dist = sqrt(distSq)
                val force = planet.mass / distSq
                proj.vx += (dx / dist) * force
                proj.vy += (dy / dist) * force

                // Collision with planet
                if (dist < planet.radius + 8f) {
                    proj.active = false
                    trailParticles = trailParticles + List(20) {
                        TrailParticle(
                            x = proj.x, y = proj.y,
                            vx = (Random.nextFloat() - 0.5f) * 6f,
                            vy = (Random.nextFloat() - 0.5f) * 6f,
                            color = NeonRed
                        )
                    }
                    break
                }
            }

            proj.x += proj.vx
            proj.y += proj.vy
            proj.trail = (proj.trail + Offset(proj.x, proj.y)).takeLast(60)

            // Check target hit
            val currentTarget = target
            if (currentTarget != null && !currentTarget.hit) {
                val dx = proj.x - currentTarget.x
                val dy = proj.y - currentTarget.y
                if (sqrt(dx * dx + dy * dy) < currentTarget.radius + 8f) {
                    target = currentTarget.copy(hit = true)
                    val bonus = (30 - level).coerceAtLeast(10)
                    score += bonus + level * 5
                    proj.active = false
                    trailParticles = trailParticles + List(30) {
                        TrailParticle(
                            x = currentTarget.x, y = currentTarget.y,
                            vx = (Random.nextFloat() - 0.5f) * 8f,
                            vy = (Random.nextFloat() - 0.5f) * 8f,
                            color = listOf(CoinGold, NeonCyan, NeonGreen).random()
                        )
                    }
                }
            }

            // Out of bounds
            if (proj.x < -100 || proj.x > canvasW + 100 || proj.y < -100 || proj.y > canvasH + 100) {
                proj.active = false
            }

            // Update trail particles
            trailParticles = trailParticles.map {
                it.copy(x = it.x + it.vx, y = it.y + it.vy, life = it.life - 0.025f)
            }.filter { it.life > 0f }

            projectile = proj.copy()

            if (!proj.active) {
                delay(500)
                if (target?.hit == true) {
                    level++
                } else if (shotsLeft <= 0) {
                    phase = SlingshotPhase.FINISHED
                } else {
                    phase = SlingshotPhase.AIMING
                }
                break
            }

            delay(16)
        }
    }

    // Predict path during aim
    LaunchedEffect(isDragging, dragPos) {
        if (!isDragging || canvasW <= 0) {
            predictedPath = emptyList()
            return@LaunchedEffect
        }
        val dx = launchPos.x - dragPos.x
        val dy = launchPos.y - dragPos.y
        val len = sqrt(dx * dx + dy * dy)
        if (len < 15f) {
            predictedPath = emptyList()
            return@LaunchedEffect
        }
        val power = (len / 250f).coerceAtMost(1f)
        var px = launchPos.x
        var py = launchPos.y
        var pvx = (dx / len) * power * 10f
        var pvy = (dy / len) * power * 10f
        val path = mutableListOf<Offset>()
        for (step in 0 until 80) {
            for (planet in planets) {
                val ddx = planet.x - px
                val ddy = planet.y - py
                val distSq = (ddx * ddx + ddy * ddy).coerceAtLeast(400f)
                val dist = sqrt(distSq)
                val force = planet.mass / distSq
                pvx += (ddx / dist) * force
                pvy += (ddy / dist) * force
            }
            px += pvx; py += pvy
            path.add(Offset(px, py))
            if (px < -50 || px > canvasW + 50 || py < -50 || py > canvasH + 50) break
        }
        predictedPath = path
    }

    Box(
        modifier = modifier.fillMaxSize().background(
            Brush.radialGradient(
                colors = listOf(Color(0xFF08061A), Color(0xFF040210), Color(0xFF000005)),
                radius = 1500f
            )
        )
    ) {
        when (phase) {
            SlingshotPhase.FINISHED -> {
                val performance = (score / 100.0).coerceIn(0.1, 2.0)
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                        Text("🪐", fontSize = 56.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            when {
                                performance >= 1.5 -> stringResource(R.string.slingshot_result_amazing)
                                performance >= 1.0 -> stringResource(R.string.slingshot_result_good)
                                else -> stringResource(R.string.slingshot_result_ok)
                            },
                            fontSize = 24.sp, fontWeight = FontWeight.Black,
                            color = if (performance >= 1.5) CoinGold else TextPrimary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(stringResource(R.string.slingshot_score, score), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = CoinGold)
                        Text(stringResource(R.string.slingshot_level, level - 1), fontSize = 16.sp, color = NeonCyan)
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

            else -> {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(phase) {
                            if (phase != SlingshotPhase.AIMING) return@pointerInput
                            detectDragGestures(
                                onDragStart = { offset ->
                                    isDragging = true
                                    dragPos = offset
                                },
                                onDrag = { change, _ ->
                                    if (isDragging) dragPos = change.position
                                },
                                onDragEnd = {
                                    if (isDragging) {
                                        val dx = launchPos.x - dragPos.x
                                        val dy = launchPos.y - dragPos.y
                                        val len = sqrt(dx * dx + dy * dy)
                                        if (len > 20f) {
                                            val power = (len / 250f).coerceAtMost(1f)
                                            projectile = Projectile(
                                                x = launchPos.x, y = launchPos.y,
                                                vx = (dx / len) * power * 10f,
                                                vy = (dy / len) * power * 10f
                                            )
                                            shotsLeft--
                                            phase = SlingshotPhase.FLYING
                                        }
                                        isDragging = false
                                    }
                                },
                                onDragCancel = { isDragging = false }
                            )
                        }
                ) {
                    canvasW = size.width
                    canvasH = size.height

                    // Background
                    drawSlingshotBackground(this)

                    // Gravity field visualization
                    planets.forEach { planet ->
                        for (ring in 1..3) {
                            drawCircle(
                                color = planet.color.copy(alpha = 0.06f / ring),
                                radius = planet.radius + ring * 35f,
                                center = Offset(planet.x, planet.y),
                                style = Stroke(width = 1f)
                            )
                        }
                        // Planet
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(planet.color, planet.color.copy(alpha = 0.6f), Color.Black),
                                center = Offset(planet.x - planet.radius * 0.2f, planet.y - planet.radius * 0.2f),
                                radius = planet.radius * 1.8f
                            ),
                            radius = planet.radius,
                            center = Offset(planet.x, planet.y)
                        )
                        // Ring
                        if (planet.hasRing && planet.ringColor != null) {
                            drawArc(
                                color = planet.ringColor,
                                startAngle = -15f,
                                sweepAngle = 210f,
                                useCenter = false,
                                topLeft = Offset(planet.x - planet.radius * 1.6f, planet.y - planet.radius * 0.3f),
                                size = androidx.compose.ui.geometry.Size(planet.radius * 3.2f, planet.radius * 0.6f),
                                style = Stroke(width = 3f, cap = StrokeCap.Round)
                            )
                        }
                    }

                    // Target
                    target?.let { t ->
                        if (!t.hit) {
                            val pulse = 1f + sin(frameTime * 5f) * 0.15f
                            drawCircle(
                                color = CoinGold.copy(alpha = 0.1f),
                                radius = t.radius * 2.5f * pulse,
                                center = Offset(t.x, t.y)
                            )
                            drawCircle(
                                color = CoinGold.copy(alpha = 0.6f),
                                radius = t.radius * pulse,
                                center = Offset(t.x, t.y)
                            )
                            drawCircle(
                                color = CoinGold,
                                radius = t.radius * pulse,
                                center = Offset(t.x, t.y),
                                style = Stroke(width = 2f)
                            )
                            // Crosshair
                            drawLine(CoinGold.copy(alpha = 0.4f), Offset(t.x - t.radius * 1.5f, t.y), Offset(t.x + t.radius * 1.5f, t.y), strokeWidth = 1f)
                            drawLine(CoinGold.copy(alpha = 0.4f), Offset(t.x, t.y - t.radius * 1.5f), Offset(t.x, t.y + t.radius * 1.5f), strokeWidth = 1f)
                        }
                    }

                    // Predicted path
                    predictedPath.forEachIndexed { i, pos ->
                        val alpha = (1f - i.toFloat() / predictedPath.size) * 0.8f
                        drawCircle(
                            color = NeonCyan.copy(alpha = alpha),
                            radius = 3.5f,
                            center = pos
                        )
                        // Glow around dots
                        if (i % 3 == 0) {
                            drawCircle(
                                color = NeonCyan.copy(alpha = alpha * 0.3f),
                                radius = 8f,
                                center = pos
                            )
                        }
                    }

                    // Projectile trail
                    projectile?.trail?.forEachIndexed { i, pos ->
                        val alpha = i.toFloat() / (projectile?.trail?.size ?: 1).toFloat() * 0.5f
                        drawCircle(
                            color = NeonGreen.copy(alpha = alpha),
                            radius = 3f + alpha * 5f,
                            center = pos
                        )
                    }

                    // Trail particles
                    trailParticles.forEach { p ->
                        drawCircle(
                            color = p.color.copy(alpha = p.life),
                            radius = 2f + p.life * 4f,
                            center = Offset(p.x, p.y)
                        )
                    }

                    // Projectile
                    projectile?.let { p ->
                        if (p.active) {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color.White, NeonGreen, NeonGreen.copy(alpha = 0.2f)),
                                    center = Offset(p.x, p.y),
                                    radius = 20f
                                ),
                                radius = 8f,
                                center = Offset(p.x, p.y)
                            )
                            drawCircle(
                                color = NeonGreen.copy(alpha = 0.2f),
                                radius = 18f,
                                center = Offset(p.x, p.y)
                            )
                        }
                    }

                    // Launch pad
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(NeonCyan.copy(alpha = 0.3f), Color.Transparent),
                            center = launchPos,
                            radius = 45f
                        ),
                        radius = 45f,
                        center = launchPos
                    )
                    drawCircle(
                        color = NeonCyan.copy(alpha = 0.6f),
                        radius = 12f,
                        center = launchPos,
                        style = Stroke(width = 2f)
                    )

                    // Aim line
                    if (isDragging) {
                        drawLine(
                            color = NeonCyan.copy(alpha = 0.7f),
                            start = launchPos,
                            end = dragPos,
                            strokeWidth = 3f,
                            cap = StrokeCap.Round
                        )
                        // Power indicator circle at drag point
                        val dx = launchPos.x - dragPos.x
                        val dy = launchPos.y - dragPos.y
                        val power = (sqrt(dx * dx + dy * dy) / 250f).coerceAtMost(1f)
                        val powerColor = when {
                            power > 0.75f -> NeonRed
                            power > 0.4f -> NeonOrange
                            else -> NeonGreen
                        }
                        drawCircle(
                            color = powerColor.copy(alpha = 0.6f),
                            radius = 12f + power * 10f,
                            center = dragPos
                        )
                        drawCircle(
                            color = powerColor,
                            radius = 12f + power * 10f,
                            center = dragPos,
                            style = Stroke(width = 2f)
                        )
                    }
                }

                // HUD
                Row(
                    modifier = Modifier.fillMaxWidth().systemBarsPadding().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("🪐 $score", fontSize = 22.sp, fontWeight = FontWeight.Black, color = CoinGold)
                        Text("Nivel $level", fontSize = 14.sp, color = NeonGreen)
                    }
                    Text("Lanzamientos: $shotsLeft", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }

                if (phase == SlingshotPhase.AIMING) {
                    Box(
                        modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(bottom = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.slingshot_drag_hint), fontSize = 13.sp, color = TextMuted.copy(alpha = 0.7f))
                    }
                }
            }
        }
    }
}

private fun drawSlingshotBackground(scope: DrawScope) {
    val rng = Random(999)
    for (i in 0 until 90) {
        scope.drawCircle(
            color = Color.White.copy(alpha = rng.nextFloat() * 0.25f + 0.05f),
            radius = rng.nextFloat() * 1.2f + 0.3f,
            center = Offset(rng.nextFloat() * scope.size.width, rng.nextFloat() * scope.size.height)
        )
    }
    // Nebula wisps
    for (i in 0 until 3) {
        scope.drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(NeonPurple.copy(alpha = 0.03f), Color.Transparent),
                center = Offset(rng.nextFloat() * scope.size.width, rng.nextFloat() * scope.size.height),
                radius = 250f
            ),
            radius = 250f,
            center = Offset(rng.nextFloat() * scope.size.width, rng.nextFloat() * scope.size.height)
        )
    }
}

data class TrailParticle(
    val x: Float, val y: Float,
    val vx: Float, val vy: Float,
    val life: Float = 1f,
    val color: Color
)

enum class SlingshotPhase { AIMING, FLYING, FINISHED }
