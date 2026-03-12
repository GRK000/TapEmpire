package com.example.juego.ui.screens.minigames

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.juego.R
import com.example.juego.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.*
import kotlin.random.Random

private data class WheelSlice(
    val label: String,
    val emoji: String,
    val multiplier: Double,
    val color: Color
)

@Composable
fun FortuneWheelScreen(
    onGameComplete: (Double) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val slices = remember {
        listOf(
            WheelSlice("x5", "💎", 5.0, Color(0xFF7C4DFF)),
            WheelSlice("x1", "💰", 1.0, Color(0xFF00BFA5)),
            WheelSlice("x3", "🌟", 3.0, Color(0xFFFFD740)),
            WheelSlice("x0.5", "🎁", 0.5, Color(0xFF607D8B)),
            WheelSlice("x2", "✨", 2.0, Color(0xFF00E5FF)),
            WheelSlice("x0", "😢", 0.1, Color(0xFF37474F)),
            WheelSlice("x10", "👑", 10.0, Color(0xFFFF6D00)),
            WheelSlice("x1.5", "⭐", 1.5, Color(0xFFE040FB))
        )
    }
    val sliceAngle = 360f / slices.size

    var phase by remember { mutableIntStateOf(0) } // 0=ready, 1=spinning, 2=done
    var rotation by remember { mutableFloatStateOf(0f) }
    var targetRotation by remember { mutableFloatStateOf(0f) }
    var resultIndex by remember { mutableIntStateOf(-1) }

    val animatedRotation by animateFloatAsState(
        targetValue = targetRotation,
        animationSpec = tween(durationMillis = 4000, easing = FastOutSlowInEasing),
        finishedListener = {
            // Calculate which slice the pointer lands on
            val normalizedAngle = (360f - (targetRotation % 360f) + 360f) % 360f
            resultIndex = (normalizedAngle / sliceAngle).toInt() % slices.size
            phase = 2
        },
        label = "wheel_spin"
    )

    Box(
        modifier = modifier.fillMaxSize().background(
            Brush.radialGradient(listOf(Color(0xFF0A0028), Color(0xFF050015)), radius = 1000f)
        ),
        contentAlignment = Alignment.Center
    ) {
        when (phase) {
            0 -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🎡", fontSize = 56.sp)
                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.wheel_title), fontSize = 26.sp, fontWeight = FontWeight.Black, color = CoinGold)
                Text(stringResource(R.string.wheel_subtitle), fontSize = 14.sp, color = TextMuted)
                Spacer(Modifier.height(32.dp))

                // Draw wheel
                Box(contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.size(280.dp)) {
                        val cx = size.width / 2f; val cy = size.height / 2f
                        val r = size.width / 2f - 10f
                        // Draw slices
                        slices.forEachIndexed { i, slice ->
                            val startAngle = i * sliceAngle - 90f
                            drawArc(
                                color = slice.color,
                                startAngle = startAngle,
                                sweepAngle = sliceAngle,
                                useCenter = true,
                                topLeft = Offset(cx - r, cy - r),
                                size = Size(r * 2, r * 2)
                            )
                            drawArc(
                                color = Color.White.copy(alpha = 0.15f),
                                startAngle = startAngle,
                                sweepAngle = sliceAngle,
                                useCenter = true,
                                topLeft = Offset(cx - r, cy - r),
                                size = Size(r * 2, r * 2),
                                style = Stroke(2f)
                            )
                        }
                        // Center circle
                        drawCircle(Onyx, 25f, Offset(cx, cy))
                        drawCircle(CoinGold, 25f, Offset(cx, cy), style = Stroke(3f))
                        // Pointer
                        val pointerPath = Path().apply {
                            moveTo(cx, cy - r - 15f)
                            lineTo(cx - 12f, cy - r + 10f)
                            lineTo(cx + 12f, cy - r + 10f)
                            close()
                        }
                        drawPath(pointerPath, NeonRed)
                    }
                }

                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        val spins = 5 + Random.nextInt(3)
                        targetRotation = rotation + 360f * spins + Random.nextFloat() * 360f
                        phase = 1
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoinGold),
                    modifier = Modifier.height(52.dp).fillMaxWidth(0.7f)
                ) { Text(stringResource(R.string.wheel_spin), fontSize = 18.sp, fontWeight = FontWeight.Black, color = DeepSpace) }

                Spacer(Modifier.height(12.dp))
                TextButton(onClick = onCancel) { Text(stringResource(R.string.btn_cancel), color = TextMuted) }
            }

            1 -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.wheel_spinning), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = CoinGold)
                Spacer(Modifier.height(24.dp))

                Box(contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.size(280.dp)) {
                        val cx = size.width / 2f; val cy = size.height / 2f
                        val r = size.width / 2f - 10f
                        rotate(animatedRotation, Offset(cx, cy)) {
                            slices.forEachIndexed { i, slice ->
                                val startAngle = i * sliceAngle - 90f
                                drawArc(
                                    color = slice.color,
                                    startAngle = startAngle,
                                    sweepAngle = sliceAngle,
                                    useCenter = true,
                                    topLeft = Offset(cx - r, cy - r),
                                    size = Size(r * 2, r * 2)
                                )
                                drawArc(
                                    color = Color.White.copy(alpha = 0.15f),
                                    startAngle = startAngle,
                                    sweepAngle = sliceAngle,
                                    useCenter = true,
                                    topLeft = Offset(cx - r, cy - r),
                                    size = Size(r * 2, r * 2),
                                    style = Stroke(2f)
                                )
                            }
                            drawCircle(Onyx, 25f, Offset(cx, cy))
                            drawCircle(CoinGold, 25f, Offset(cx, cy), style = Stroke(3f))
                        }
                        // Pointer stays fixed
                        val pointerPath = Path().apply {
                            moveTo(cx, cy - r - 15f)
                            lineTo(cx - 12f, cy - r + 10f)
                            lineTo(cx + 12f, cy - r + 10f)
                            close()
                        }
                        drawPath(pointerPath, NeonRed)
                    }
                }
            }

            2 -> {
                val result = slices.getOrNull(resultIndex) ?: slices[0]
                val perf = (result.multiplier / 5.0).coerceIn(0.1, 2.0)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Text(result.emoji, fontSize = 64.sp)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        when {
                            result.multiplier >= 10.0 -> stringResource(R.string.wheel_result_jackpot)
                            result.multiplier >= 5.0 -> stringResource(R.string.wheel_result_great)
                            result.multiplier >= 2.0 -> stringResource(R.string.wheel_result_good)
                            result.multiplier >= 1.0 -> stringResource(R.string.wheel_result_ok)
                            else -> stringResource(R.string.wheel_result_bad)
                        },
                        fontSize = 26.sp, fontWeight = FontWeight.Black,
                        color = if (result.multiplier >= 5.0) CoinGold else TextPrimary
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        stringResource(R.string.wheel_multiplier, result.label),
                        fontSize = 22.sp, fontWeight = FontWeight.Bold, color = result.color
                    )
                    Spacer(Modifier.height(32.dp))
                    Button(
                        onClick = { onGameComplete(perf) },
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
