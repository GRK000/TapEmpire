package com.example.juego.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.juego.GameState
import com.example.juego.R
import com.example.juego.ui.components.AnimatedBackground
import com.example.juego.ui.components.GlassCard
import com.example.juego.ui.components.Haptics
import com.example.juego.ui.theme.*
import com.example.juego.ui.viewmodel.GameUiState
import com.example.juego.ui.viewmodel.GameViewModel
import kotlin.math.sin

/**
 * El Cierre del Pacto: el duelo final contra Magnus Vex en Nexus Prime.
 * Dos torres — tu imperio contra VexCorp. Cuando tu valoración supera la
 * suya, puedes invocar el Pacto de los Fundadores y ganar la apuesta.
 */
@Composable
fun PactScreen(
    viewModel: GameViewModel,
    uiState: GameUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val progress = uiState.pactProgress.toFloat()

    val transition = rememberInfiniteTransition(label = "pact")
    val pulse by transition.animateFloat(
        initialValue = 0f, targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing)),
        label = "pact_pulse"
    )

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedBackground(worldTheme = uiState.currentWorld?.theme)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = stringResource(R.string.pact_title),
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary,
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                text = stringResource(R.string.pact_subtitle),
                fontSize = 13.sp,
                color = TextMuted
            )

            Spacer(Modifier.height(16.dp))

            // === DUELO DE TORRES ===
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                val w = size.width
                val h = size.height
                val towerW = w * 0.22f
                val gap = w * 0.12f

                // Tu torre (izquierda, dorada): crece con el progreso
                val yourH = h * (0.15f + 0.8f * progress.coerceIn(0.02f, 1f))
                val yourX = w / 2f - gap / 2f - towerW
                drawTower(yourX, h, towerW, yourH, CoinGold, pulse)

                // Torre VexCorp (derecha, roja): estática, imponente
                val vexH = h * 0.95f
                val vexX = w / 2f + gap / 2f
                drawTower(vexX, h, towerW, vexH, NeonRed, pulse + 1.5f)

                // Línea de meta: la altura de VexCorp
                drawLine(
                    Color.White.copy(alpha = 0.25f),
                    Offset(0f, h - vexH),
                    Offset(w, h - vexH),
                    strokeWidth = 2f
                )
            }

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.pact_your_empire), fontSize = 11.sp, color = CoinGold, fontWeight = FontWeight.Bold)
                    Text(
                        GameState.fmt(uiState.totalCoinsEarned),
                        fontSize = 16.sp, fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace, color = CoinGold
                    )
                }
                Text("⚖️", fontSize = 24.sp)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("VexCorp", fontSize = 11.sp, color = NeonRed, fontWeight = FontWeight.Bold)
                    Text(
                        GameState.fmt(GameState.VEX_VALUATION),
                        fontSize = 16.sp, fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace, color = NeonRed
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Estado del duelo
            when {
                uiState.pactWon -> {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        glassColor = CoinGold.copy(alpha = 0.12f),
                        borderColor = CoinGold.copy(alpha = 0.4f),
                        cornerRadius = 16.dp
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth().padding(8.dp)
                        ) {
                            Text("👑", fontSize = 48.sp)
                            Text(
                                stringResource(R.string.pact_won_title),
                                fontSize = 20.sp, fontWeight = FontWeight.Black, color = CoinGold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                stringResource(R.string.pact_won_body),
                                fontSize = 13.sp, color = TextSecondary,
                                textAlign = TextAlign.Center, lineHeight = 19.sp
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                stringResource(R.string.pact_seal_active),
                                fontSize = 14.sp, fontWeight = FontWeight.Black, color = NeonGreen
                            )
                        }
                    }
                }
                uiState.canClosePact -> {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        glassColor = NeonGreen.copy(alpha = 0.1f),
                        borderColor = NeonGreen.copy(alpha = 0.4f),
                        cornerRadius = 16.dp
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth().padding(8.dp)
                        ) {
                            Text(
                                stringResource(R.string.pact_ready_title),
                                fontSize = 17.sp, fontWeight = FontWeight.Black, color = NeonGreen,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                stringResource(R.string.pact_ready_body),
                                fontSize = 13.sp, color = TextSecondary,
                                textAlign = TextAlign.Center, lineHeight = 19.sp
                            )
                            Spacer(Modifier.height(14.dp))
                            Button(
                                onClick = {
                                    Haptics.heavy(context)
                                    viewModel.closePact()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CoinGold),
                                modifier = Modifier.fillMaxWidth().height(54.dp)
                            ) {
                                Text(
                                    stringResource(R.string.pact_close_button),
                                    fontSize = 16.sp, fontWeight = FontWeight.Black, color = DeepSpace
                                )
                            }
                        }
                    }
                }
                else -> {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 16.dp
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                            Text(
                                stringResource(R.string.pact_progress_label, (progress * 100).toInt()),
                                fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                stringResource(R.string.pact_progress_hint),
                                fontSize = 12.sp, color = TextMuted, lineHeight = 18.sp
                            )
                            Spacer(Modifier.height(10.dp))
                            // Taunt de Vex según lo cerca que estés
                            val tauntRes = when {
                                progress < 0.25f -> R.string.pact_taunt_far
                                progress < 0.75f -> R.string.pact_taunt_mid
                                else -> R.string.pact_taunt_close
                            }
                            Text(
                                "🎩 «${stringResource(tauntRes)}»",
                                fontSize = 12.sp, color = NeonRed.copy(alpha = 0.9f),
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

/** Torre estilizada con ventanas parpadeantes */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawTower(
    x: Float,
    baseY: Float,
    towerW: Float,
    towerH: Float,
    color: Color,
    time: Float
) {
    val top = baseY - towerH
    // Cuerpo
    drawRect(
        Brush.verticalGradient(
            listOf(color.copy(alpha = 0.55f), color.copy(alpha = 0.15f)),
            startY = top, endY = baseY
        ),
        topLeft = Offset(x, top),
        size = Size(towerW, towerH)
    )
    // Antena
    drawLine(color.copy(alpha = 0.7f), Offset(x + towerW / 2f, top), Offset(x + towerW / 2f, top - 18f), 3f)
    drawCircle(color.copy(alpha = 0.5f + sin(time * 2f) * 0.4f), 4f, Offset(x + towerW / 2f, top - 20f))
    // Ventanas
    val cols = 3
    val rows = (towerH / 26f).toInt()
    for (r in 0 until rows) {
        for (c in 0 until cols) {
            val lit = sin(time + r * 1.7f + c * 2.3f) > 0.1f
            drawRect(
                if (lit) Color.White.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.2f),
                topLeft = Offset(
                    x + towerW * (0.16f + c * 0.28f),
                    top + 14f + r * 26f
                ),
                size = Size(towerW * 0.16f, 10f)
            )
        }
    }
}
