package com.example.juego.ui.screens.minigames

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

// ============ DATA CLASSES ============

private data class Ball(
    val id: Int,
    var x: Float,
    var y: Float,
    var vx: Float = 0f,
    var vy: Float = 0f,
    val radius: Float = 18f,
    val color: Color,
    val glowColor: Color,
    val points: Int = 10,
    var pocketed: Boolean = false,
    var trail: List<Offset> = emptyList()
)

private data class Pocket(
    val x: Float,
    val y: Float,
    val radius: Float = 30f
)

private data class PocketEffect(
    val x: Float, val y: Float,
    val color: Color,
    var alpha: Float = 1f,
    var radius: Float = 10f
)

// ============ MAIN COMPOSABLE ============

@Composable
fun CosmicBilliardsScreen(
    onGameComplete: (performanceMultiplier: Double) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var phase by remember { mutableStateOf(BilliardsPhase.AIMING) }
    var score by remember { mutableIntStateOf(0) }
    var shotsLeft by remember { mutableIntStateOf(15) }
    var pocketedCount by remember { mutableIntStateOf(0) }
    var canvasW by remember { mutableFloatStateOf(0f) }
    var canvasH by remember { mutableFloatStateOf(0f) }

    var tableRect by remember { mutableStateOf(Rect.Zero) }

    var cueBall by remember { mutableStateOf<Ball?>(null) }
    var balls by remember { mutableStateOf(listOf<Ball>()) }
    var pockets by remember { mutableStateOf(listOf<Pocket>()) }
    var pocketEffects by remember { mutableStateOf(listOf<PocketEffect>()) }

    var dragStart by remember { mutableStateOf(Offset.Zero) }
    var dragCurrent by remember { mutableStateOf(Offset.Zero) }
    var isDragging by remember { mutableStateOf(false) }
    var frameTime by remember { mutableFloatStateOf(0f) }

    // ============ SETUP TABLE ============
    LaunchedEffect(canvasW, canvasH) {
        if (canvasW <= 0 || canvasH <= 0) return@LaunchedEffect

        val margin = 50f
        val tLeft = margin
        val tTop = canvasH * 0.18f
        val tRight = canvasW - margin
        val tBottom = canvasH * 0.82f
        tableRect = Rect(tLeft, tTop, tRight, tBottom)

        val tw = tRight - tLeft
        val th = tBottom - tTop
        val pocketR = 30f

        // 6 pockets: 4 corners + 2 mid-sides
        pockets = listOf(
            Pocket(tLeft + 8f, tTop + 8f, pocketR),
            Pocket(tLeft + tw / 2f, tTop - 2f, pocketR - 4f),
            Pocket(tRight - 8f, tTop + 8f, pocketR),
            Pocket(tLeft + 8f, tBottom - 8f, pocketR),
            Pocket(tLeft + tw / 2f, tBottom + 2f, pocketR - 4f),
            Pocket(tRight - 8f, tBottom - 8f, pocketR)
        )

        // Cue ball
        cueBall = Ball(
            id = 0,
            x = tLeft + tw * 0.25f,
            y = tTop + th / 2f,
            radius = 18f,
            color = Color.White,
            glowColor = NeonCyan,
            points = 0
        )

        // Colored balls in triangle rack
        val rackX = tLeft + tw * 0.7f
        val rackY = tTop + th / 2f
        val spacing = 38f
        val ballColors = listOf(
            NeonCyan to Color(0xFF00FFFF),
            NeonPurple to NeonPurpleLight,
            NeonPink to Color(0xFFFF88BB),
            CoinGold to Color(0xFFFFE555),
            NeonGreen to Color(0xFF55FF88),
            NeonOrange to Color(0xFFFFAA33),
            NeonRed to Color(0xFFFF6666),
            GemPurple to Color(0xFFDD77FF),
            PrestigeCyan to Color(0xFF55DDEE),
            NeonCyan to Color(0xFF33CCFF)
        )
        val pointValues = listOf(10, 10, 15, 20, 10, 15, 25, 30, 10, 15)

        var ballId = 1
        val newBalls = mutableListOf<Ball>()
        var row = 0
        var idx = 0
        while (idx < 10 && row < 5) {
            for (col in 0..row) {
                if (idx >= 10) break
                val bx = rackX + row * spacing * 0.866f
                val by = rackY + (col - row / 2f) * spacing
                val (c, g) = ballColors[idx]
                newBalls.add(Ball(
                    id = ballId++,
                    x = bx, y = by,
                    radius = 18f,
                    color = c, glowColor = g,
                    points = pointValues[idx]
                ))
                idx++
            }
            row++
        }
        balls = newBalls
        phase = BilliardsPhase.AIMING
    }

    // ============ PHYSICS LOOP ============
    LaunchedEffect(phase) {
        if (phase != BilliardsPhase.SHOOTING) return@LaunchedEffect
        val cue = cueBall ?: return@LaunchedEffect
        val friction = 0.985f
        val allBalls = mutableListOf(cue).apply { addAll(balls.filter { !it.pocketed }) }

        while (phase == BilliardsPhase.SHOOTING) {
            frameTime += 0.016f

            for (b in allBalls) {
                b.x += b.vx
                b.y += b.vy
                b.vx *= friction
                b.vy *= friction
                if (b.id == 0) {
                    b.trail = (b.trail + Offset(b.x, b.y)).takeLast(20)
                }
            }

            // Wall collisions
            for (b in allBalls) {
                if (b.x - b.radius < tableRect.left) {
                    b.x = tableRect.left + b.radius; b.vx = -b.vx * 0.8f
                }
                if (b.x + b.radius > tableRect.right) {
                    b.x = tableRect.right - b.radius; b.vx = -b.vx * 0.8f
                }
                if (b.y - b.radius < tableRect.top) {
                    b.y = tableRect.top + b.radius; b.vy = -b.vy * 0.8f
                }
                if (b.y + b.radius > tableRect.bottom) {
                    b.y = tableRect.bottom - b.radius; b.vy = -b.vy * 0.8f
                }
            }

            // Ball-ball collisions
            for (i in allBalls.indices) {
                for (j in i + 1 until allBalls.size) {
                    val a = allBalls[i]; val b = allBalls[j]
                    val dx = b.x - a.x; val dy = b.y - a.y
                    val dist = sqrt(dx * dx + dy * dy)
                    val minDist = a.radius + b.radius
                    if (dist < minDist && dist > 0.001f) {
                        val overlap = (minDist - dist) / 2f
                        val nx = dx / dist; val ny = dy / dist
                        a.x -= nx * overlap; a.y -= ny * overlap
                        b.x += nx * overlap; b.y += ny * overlap
                        val dvx = a.vx - b.vx; val dvy = a.vy - b.vy
                        val dot = dvx * nx + dvy * ny
                        if (dot > 0) {
                            a.vx -= dot * nx; a.vy -= dot * ny
                            b.vx += dot * nx; b.vy += dot * ny
                        }
                    }
                }
            }

            // Pocket detection
            for (pocket in pockets) {
                for (b in allBalls) {
                    if (b.pocketed) continue
                    val dx = b.x - pocket.x; val dy = b.y - pocket.y
                    if (sqrt(dx * dx + dy * dy) < pocket.radius) {
                        if (b.id == 0) {
                            b.x = tableRect.left + (tableRect.right - tableRect.left) * 0.25f
                            b.y = tableRect.top + (tableRect.bottom - tableRect.top) / 2f
                            b.vx = 0f; b.vy = 0f
                            score = (score - 5).coerceAtLeast(0)
                            pocketEffects = pocketEffects + PocketEffect(pocket.x, pocket.y, NeonRed)
                        } else {
                            b.pocketed = true
                            score += b.points
                            pocketedCount++
                            pocketEffects = pocketEffects + PocketEffect(pocket.x, pocket.y, b.color)
                        }
                    }
                }
            }

            pocketEffects = pocketEffects
                .map { it.copy(alpha = it.alpha - 0.03f, radius = it.radius + 2f) }
                .filter { it.alpha > 0f }

            cueBall = cue.copy()
            balls = balls.map { ball ->
                allBalls.find { it.id == ball.id }?.let {
                    ball.copy(x = it.x, y = it.y, vx = it.vx, vy = it.vy, pocketed = it.pocketed)
                } ?: ball
            }

            val maxSpeed = allBalls.maxOfOrNull { sqrt(it.vx * it.vx + it.vy * it.vy) } ?: 0f
            if (maxSpeed < 0.15f) {
                for (b in allBalls) { b.vx = 0f; b.vy = 0f }
                cueBall = cue.copy()
                balls = balls.map { ball ->
                    allBalls.find { it.id == ball.id }?.let {
                        ball.copy(x = it.x, y = it.y, vx = 0f, vy = 0f, pocketed = it.pocketed)
                    } ?: ball
                }
                delay(300)
                val activeBalls = balls.count { !it.pocketed }
                phase = if (activeBalls == 0 || shotsLeft <= 0) BilliardsPhase.FINISHED
                        else BilliardsPhase.AIMING
                break
            }
            delay(16)
        }
    }

    // ============ UI ============
    Box(modifier = modifier.fillMaxSize().background(Color(0xFF040210))) {
        when (phase) {
            BilliardsPhase.FINISHED -> {
                val performance = ((pocketedCount.toDouble() / 10.0) * 1.5 + score / 200.0).coerceIn(0.1, 2.0)
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                        Text("🎱", fontSize = 60.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            when {
                                pocketedCount >= 9 -> stringResource(R.string.billiards_result_perfect)
                                pocketedCount >= 6 -> stringResource(R.string.billiards_result_great)
                                pocketedCount >= 3 -> stringResource(R.string.billiards_result_good)
                                else -> stringResource(R.string.billiards_result_ok)
                            },
                            fontSize = 26.sp, fontWeight = FontWeight.Black,
                            color = if (pocketedCount >= 9) CoinGold else TextPrimary
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(stringResource(R.string.billiards_pocketed_stat, pocketedCount), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
                        Text(stringResource(R.string.billiards_score, score), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = CoinGold)
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
                            if (phase != BilliardsPhase.AIMING) return@pointerInput
                            detectDragGestures(
                                onDragStart = { offset ->
                                    val cue = cueBall ?: return@detectDragGestures
                                    val dx = offset.x - cue.x; val dy = offset.y - cue.y
                                    if (dx * dx + dy * dy < 25000f) {
                                        isDragging = true
                                        dragStart = Offset(cue.x, cue.y)
                                        dragCurrent = offset
                                    }
                                },
                                onDrag = { change, _ -> if (isDragging) dragCurrent = change.position },
                                onDragEnd = {
                                    if (isDragging) {
                                        val cue = cueBall ?: return@detectDragGestures
                                        val dx = dragStart.x - dragCurrent.x
                                        val dy = dragStart.y - dragCurrent.y
                                        val len = sqrt(dx * dx + dy * dy)
                                        if (len > 15f) {
                                            val power = (len / 350f).coerceAtMost(1f)
                                            cue.vx = (dx / len) * power * 28f
                                            cue.vy = (dy / len) * power * 28f
                                            cue.trail = emptyList()
                                            cueBall = cue.copy()
                                            shotsLeft--
                                            phase = BilliardsPhase.SHOOTING
                                        }
                                        isDragging = false
                                    }
                                },
                                onDragCancel = { isDragging = false }
                            )
                        }
                ) {
                    canvasW = size.width; canvasH = size.height
                    if (tableRect == Rect.Zero) return@Canvas

                    drawSpaceBackground(frameTime)
                    drawBilliardTable(tableRect, pockets)

                    pocketEffects.forEach { fx ->
                        drawCircle(color = fx.color.copy(alpha = fx.alpha * 0.5f), radius = fx.radius, center = Offset(fx.x, fx.y))
                    }

                    balls.filter { !it.pocketed }.forEach { drawBall(it) }

                    cueBall?.let { cue ->
                        cue.trail.forEachIndexed { i, pos ->
                            drawCircle(NeonCyan.copy(alpha = i.toFloat() / cue.trail.size * 0.25f), cue.radius * 0.6f, pos)
                        }
                        drawBall(cue)
                    }

                    // Aim line + cue stick
                    if (isDragging && phase == BilliardsPhase.AIMING) {
                        cueBall?.let { cue ->
                            val dx = dragStart.x - dragCurrent.x; val dy = dragStart.y - dragCurrent.y
                            val len = sqrt(dx * dx + dy * dy)
                            if (len > 5f) {
                                val dirX = dx / len; val dirY = dy / len
                                val power = (len / 350f).coerceAtMost(1f)

                                // Cue stick
                                val stickLen = 120f + (1f - power) * 80f
                                drawLine(
                                    Color(0xFFDEB887),
                                    Offset(cue.x - dirX * 20f, cue.y - dirY * 20f),
                                    Offset(cue.x - dirX * stickLen, cue.y - dirY * stickLen),
                                    strokeWidth = 5f, cap = StrokeCap.Round
                                )
                                drawCircle(Color(0xFF5599CC), 4f, Offset(cue.x - dirX * 18f, cue.y - dirY * 18f))

                                // Aim dots
                                for (i in 1..8) {
                                    drawCircle(
                                        Color.White.copy(alpha = (1f - i / 8f) * 0.5f * power),
                                        2.5f,
                                        Offset(cue.x + dirX * i * 25f, cue.y + dirY * i * 25f)
                                    )
                                }

                                // === IMPACT ASSISTANT ===
                                // Raycast from cue ball along aim direction to find first ball hit
                                val activeBalls = balls.filter { !it.pocketed }
                                var closestDist = Float.MAX_VALUE
                                var hitBall: Ball? = null
                                var hitPointX = 0f
                                var hitPointY = 0f

                                for (b in activeBalls) {
                                    // Vector from cue to ball center
                                    val cx = b.x - cue.x
                                    val cy = b.y - cue.y
                                    // Project onto aim direction
                                    val proj = cx * dirX + cy * dirY
                                    if (proj <= 0f) continue // ball is behind
                                    // Perpendicular distance from aim line to ball center
                                    val perpX = cx - proj * dirX
                                    val perpY = cy - proj * dirY
                                    val perpDist = sqrt(perpX * perpX + perpY * perpY)
                                    val minDist = cue.radius + b.radius
                                    if (perpDist < minDist && proj < closestDist) {
                                        closestDist = proj
                                        hitBall = b
                                        // Contact point: cue ball center at moment of impact
                                        val offset = sqrt((minDist * minDist - perpDist * perpDist).coerceAtLeast(0f))
                                        hitPointX = cue.x + dirX * (proj - offset)
                                        hitPointY = cue.y + dirY * (proj - offset)
                                    }
                                }

                                hitBall?.let { target ->
                                    // Draw extended aim line to impact point
                                    drawLine(
                                        NeonCyan.copy(alpha = 0.25f),
                                        Offset(cue.x + dirX * 25f * 8, cue.y + dirY * 25f * 8),
                                        Offset(hitPointX, hitPointY),
                                        strokeWidth = 1.5f, cap = StrokeCap.Round
                                    )

                                    // Ghost cue ball at impact point
                                    drawCircle(
                                        Color.White.copy(alpha = 0.15f),
                                        cue.radius,
                                        Offset(hitPointX, hitPointY)
                                    )
                                    drawCircle(
                                        NeonCyan.copy(alpha = 0.3f),
                                        cue.radius,
                                        Offset(hitPointX, hitPointY),
                                        style = Stroke(width = 1.5f)
                                    )

                                    // Calculate target ball direction after impact
                                    val impactDx = target.x - hitPointX
                                    val impactDy = target.y - hitPointY
                                    val impactLen = sqrt(impactDx * impactDx + impactDy * impactDy).coerceAtLeast(0.01f)
                                    val targetDirX = impactDx / impactLen
                                    val targetDirY = impactDy / impactLen

                                    // Draw predicted target ball trajectory
                                    for (i in 1..12) {
                                        val alpha = (1f - i / 12f) * 0.55f * power
                                        drawCircle(
                                            target.color.copy(alpha = alpha),
                                            3.5f,
                                            Offset(
                                                target.x + targetDirX * i * 22f,
                                                target.y + targetDirY * i * 22f
                                            )
                                        )
                                    }

                                    // Highlight ring on target ball
                                    drawCircle(
                                        Color.White.copy(alpha = 0.4f),
                                        target.radius + 4f,
                                        Offset(target.x, target.y),
                                        style = Stroke(width = 2f)
                                    )

                                    // Highlight nearest pocket if trajectory leads close to one
                                    val predEndX = target.x + targetDirX * 300f
                                    val predEndY = target.y + targetDirY * 300f
                                    var nearestPocket: Pocket? = null
                                    var nearestPocketDist = Float.MAX_VALUE
                                    for (p in pockets) {
                                        // Distance from pocket to trajectory line
                                        val apx = p.x - target.x
                                        val apy = p.y - target.y
                                        val dot = apx * targetDirX + apy * targetDirY
                                        if (dot <= 0f) continue
                                        val closestX = target.x + targetDirX * dot
                                        val closestY = target.y + targetDirY * dot
                                        val d = sqrt((p.x - closestX) * (p.x - closestX) + (p.y - closestY) * (p.y - closestY))
                                        if (d < p.radius * 2.5f && d < nearestPocketDist) {
                                            nearestPocketDist = d
                                            nearestPocket = p
                                        }
                                    }
                                    nearestPocket?.let { pocket ->
                                        // Glow around pocket that the ball may enter
                                        drawCircle(
                                            CoinGold.copy(alpha = 0.35f),
                                            pocket.radius + 10f,
                                            Offset(pocket.x, pocket.y)
                                        )
                                        drawCircle(
                                            CoinGold.copy(alpha = 0.5f),
                                            pocket.radius + 6f,
                                            Offset(pocket.x, pocket.y),
                                            style = Stroke(width = 2f)
                                        )
                                    }
                                }

                                // Power bar
                                val barW = 200f; val barH = 8f
                                val barX = size.width / 2f - barW / 2f
                                val barY = tableRect.bottom + 40f
                                drawRoundRect(
                                    Onyx, Offset(barX, barY),
                                    androidx.compose.ui.geometry.Size(barW, barH),
                                    androidx.compose.ui.geometry.CornerRadius(4f)
                                )
                                val powerColor = when {
                                    power > 0.8f -> NeonRed; power > 0.5f -> NeonOrange; else -> NeonGreen
                                }
                                drawRoundRect(
                                    powerColor, Offset(barX, barY),
                                    androidx.compose.ui.geometry.Size(barW * power, barH),
                                    androidx.compose.ui.geometry.CornerRadius(4f)
                                )
                            }
                        }
                    }
                }

                // HUD
                Row(
                    modifier = Modifier.fillMaxWidth().systemBarsPadding().padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("🎱 $score pts", fontSize = 22.sp, fontWeight = FontWeight.Black, color = CoinGold)
                        Text(stringResource(R.string.billiards_pocketed, pocketedCount), fontSize = 13.sp, color = NeonCyan)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Tiros: $shotsLeft", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = if (shotsLeft <= 3) NeonRed else TextPrimary)
                        if (phase == BilliardsPhase.AIMING) {
                            Text(stringResource(R.string.billiards_drag_hint), fontSize = 11.sp, color = TextMuted)
                        }
                    }
                }
            }
        }
    }
}

// ============ DRAW HELPERS ============

private fun DrawScope.drawSpaceBackground(time: Float) {
    drawRect(Brush.verticalGradient(listOf(Color(0xFF040118), Color(0xFF0A0225), Color(0xFF060115))))
    val rng = Random(77)
    for (i in 0 until 70) {
        val x = rng.nextFloat() * size.width; val y = rng.nextFloat() * size.height
        val twinkle = sin(time * 2f + rng.nextFloat() * 6.28f) * 0.5f + 0.5f
        drawCircle(Color.White.copy(alpha = (rng.nextFloat() * 0.2f + 0.05f) * twinkle), rng.nextFloat() * 1.2f + 0.3f, Offset(x, y))
    }
    drawCircle(
        Brush.radialGradient(listOf(NeonPurple.copy(alpha = 0.03f), Color.Transparent), Offset(size.width * 0.3f, size.height * 0.2f), 300f),
        300f, Offset(size.width * 0.3f, size.height * 0.2f)
    )
    drawCircle(
        Brush.radialGradient(listOf(NeonCyan.copy(alpha = 0.02f), Color.Transparent), Offset(size.width * 0.8f, size.height * 0.8f), 250f),
        250f, Offset(size.width * 0.8f, size.height * 0.8f)
    )
}

private fun DrawScope.drawBilliardTable(table: Rect, pockets: List<Pocket>) {
    val border = 14f
    // Outer frame
    drawRoundRect(
        Color(0xFF1A0A2E),
        Offset(table.left - border - 4f, table.top - border - 4f),
        androidx.compose.ui.geometry.Size(table.width + (border + 4f) * 2, table.height + (border + 4f) * 2),
        androidx.compose.ui.geometry.CornerRadius(12f)
    )
    // Rail neon trim
    drawRoundRect(
        Brush.linearGradient(listOf(NeonPurple.copy(alpha = 0.7f), NeonCyan.copy(alpha = 0.5f), NeonPurple.copy(alpha = 0.7f))),
        Offset(table.left - border, table.top - border),
        androidx.compose.ui.geometry.Size(table.width + border * 2, table.height + border * 2),
        androidx.compose.ui.geometry.CornerRadius(8f),
        style = Stroke(width = border)
    )
    // Felt
    drawRoundRect(
        Brush.radialGradient(
            listOf(Color(0xFF0C2818), Color(0xFF081E12), Color(0xFF061A0E)),
            Offset(table.left + table.width / 2, table.top + table.height / 2), table.width * 0.7f
        ),
        Offset(table.left, table.top),
        androidx.compose.ui.geometry.Size(table.width, table.height),
        androidx.compose.ui.geometry.CornerRadius(4f)
    )
    // Center line
    drawLine(Color.White.copy(alpha = 0.06f), Offset(table.left + table.width / 2, table.top + 10f), Offset(table.left + table.width / 2, table.bottom - 10f), 1f)
    // D-zone
    drawArc(
        Color.White.copy(alpha = 0.05f), -90f, 180f, false,
        Offset(table.left + table.width * 0.2f - 40f, table.top + table.height / 2 - 40f),
        androidx.compose.ui.geometry.Size(80f, 80f), style = Stroke(1f)
    )
    // Pockets
    pockets.forEach { p ->
        drawCircle(
            Brush.radialGradient(listOf(Color.Black, Color(0xFF0A0020), Color.Black), Offset(p.x, p.y), p.radius),
            p.radius, Offset(p.x, p.y)
        )
        drawCircle(NeonPurple.copy(alpha = 0.25f), p.radius + 3f, Offset(p.x, p.y), style = Stroke(2f))
    }
}

private fun DrawScope.drawBall(ball: Ball) {
    drawCircle(
        Brush.radialGradient(listOf(ball.glowColor.copy(alpha = 0.2f), Color.Transparent), Offset(ball.x, ball.y), ball.radius * 2.5f),
        ball.radius * 2.5f, Offset(ball.x, ball.y)
    )
    drawCircle(
        Brush.radialGradient(
            listOf(ball.color, ball.color.copy(alpha = 0.9f),
                Color(ball.color.red * 0.5f, ball.color.green * 0.5f, ball.color.blue * 0.5f)),
            Offset(ball.x - ball.radius * 0.25f, ball.y - ball.radius * 0.25f), ball.radius * 1.5f
        ),
        ball.radius, Offset(ball.x, ball.y)
    )
    drawCircle(Color.White.copy(alpha = 0.45f), ball.radius * 0.35f, Offset(ball.x - ball.radius * 0.3f, ball.y - ball.radius * 0.3f))
    drawCircle(Color.White.copy(alpha = 0.15f), ball.radius, Offset(ball.x, ball.y), style = Stroke(1f))
}

enum class BilliardsPhase { AIMING, SHOOTING, FINISHED }
