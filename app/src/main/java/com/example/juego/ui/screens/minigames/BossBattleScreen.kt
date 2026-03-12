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

@Composable
fun BossBattleScreen(
    onGameComplete: (Double) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var phase by remember { mutableIntStateOf(0) }
    var countdown by remember { mutableIntStateOf(3) }
    var bossHp by remember { mutableIntStateOf(200) }
    val bossMaxHp = 200
    var playerDmg by remember { mutableIntStateOf(0) }
    var timeLeft by remember { mutableFloatStateOf(20f) }
    var combo by remember { mutableIntStateOf(0) }
    var hitEffects by remember { mutableStateOf(listOf<HitFx>()) }
    var bossShake by remember { mutableFloatStateOf(0f) }
    var bossPhase by remember { mutableIntStateOf(0) } // 0=normal, 1=rage

    LaunchedEffect(Unit) {
        for (i in 3 downTo 1) { countdown = i; delay(800) }
        phase = 1
        val start = System.currentTimeMillis()
        while (phase == 1) {
            timeLeft = (20f - (System.currentTimeMillis() - start) / 1000f).coerceAtLeast(0f)
            if (timeLeft <= 0f || bossHp <= 0) { phase = 2; break }
            if (bossHp < bossMaxHp / 3) bossPhase = 1
            // Boss shake decay
            if (bossShake > 0) bossShake *= 0.85f
            // Hit effects decay
            hitEffects = hitEffects.map { it.copy(life = it.life - 0.04f) }.filter { it.life > 0 }
            // Combo decay
            delay(16)
        }
    }

    // Combo reset timer
    LaunchedEffect(combo) {
        if (combo > 0) {
            delay(1500)
            combo = 0
        }
    }

    Box(
        modifier = modifier.fillMaxSize().background(
            Brush.verticalGradient(
                if (bossPhase == 1) listOf(Color(0xFF200000), Color(0xFF100000))
                else listOf(Color(0xFF050020), Color(0xFF0A0010))
            )
        )
    ) {
        when (phase) {
            0 -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("👾", fontSize = 64.sp)
                    Spacer(Modifier.height(16.dp))
                    Text(stringResource(R.string.boss_title), fontSize = 32.sp, fontWeight = FontWeight.Black, color = NeonRed)
                    Text(stringResource(R.string.boss_subtitle), fontSize = 14.sp, color = TextMuted)
                    Spacer(Modifier.height(32.dp))
                    Text("$countdown", fontSize = 72.sp, fontWeight = FontWeight.Black, color = NeonCyan)
                }
            }
            1 -> {
                Column(
                    modifier = Modifier.fillMaxSize().systemBarsPadding().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Timer
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(stringResource(R.string.boss_damage, playerDmg), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = CoinGold)
                        Text("⏱️ ${timeLeft.toInt()}s", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                            color = if (timeLeft < 5f) NeonRed else TextPrimary)
                    }
                    Spacer(Modifier.height(8.dp))
                    // Boss HP bar
                    Column(Modifier.fillMaxWidth()) {
                        Text("👾 ${stringResource(R.string.boss_name)} ${if (bossPhase == 1) stringResource(R.string.boss_enraged) else ""}",
                            fontSize = 14.sp, color = if (bossPhase == 1) NeonRed else TextMuted)
                        Spacer(Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { bossHp.toFloat() / bossMaxHp },
                            modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)),
                            color = if (bossPhase == 1) NeonRed else Error,
                            trackColor = Onyx
                        )
                        Text(stringResource(R.string.boss_hp, bossHp, bossMaxHp), fontSize = 12.sp, color = TextMuted)
                    }
                    Spacer(Modifier.height(20.dp))

                    // Boss area
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .pointerInput(Unit) {
                                detectTapGestures { offset ->
                                    val dmg = (1 + combo / 3).coerceAtMost(5)
                                    val crit = Random.nextFloat() < 0.15f
                                    val totalDmg = if (crit) dmg * 3 else dmg
                                    bossHp = (bossHp - totalDmg).coerceAtLeast(0)
                                    playerDmg += totalDmg
                                    combo++
                                    bossShake = 8f
                                    hitEffects = hitEffects + HitFx(
                                        offset.x, offset.y,
                                        if (crit) "💥$totalDmg" else "-$totalDmg",
                                        if (crit) CoinGold else NeonRed
                                    )
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(Modifier.fillMaxSize()) {
                            hitEffects.forEach { fx ->
                                drawCircle(
                                    fx.color.copy(alpha = fx.life * 0.3f),
                                    radius = 30f * (1f - fx.life) + 10f,
                                    center = Offset(fx.x, fx.y)
                                )
                            }
                        }

                        val shakeX = if (bossShake > 0.5f) (Random.nextFloat() - 0.5f) * bossShake else 0f
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.offset(x = shakeX.dp)
                        ) {
                            Text(
                                if (bossPhase == 1) "👹" else "👾",
                                fontSize = 100.sp
                            )
                            if (combo > 3) {
                                Text("🔥 x$combo", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                                    color = if (combo > 10) ComboLegendary else ComboFire)
                            }
                        }
                    }
                }
            }
            2 -> {
                val killed = bossHp <= 0
                val perf = if (killed) (1.2 + (20.0 - timeLeft) / 20.0 * -0.4 + playerDmg / 300.0).coerceIn(0.5, 2.0)
                           else (playerDmg.toDouble() / bossMaxHp).coerceIn(0.1, 1.0)
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                        Text(if (killed) "🏆" else "💀", fontSize = 56.sp)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            if (killed) stringResource(R.string.boss_result_win) else stringResource(R.string.boss_result_lose),
                            fontSize = 24.sp, fontWeight = FontWeight.Black,
                            color = if (killed) CoinGold else NeonRed
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(stringResource(R.string.boss_total_damage, playerDmg), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = CoinGold)
                        if (killed) Text("Tiempo: ${(20 - timeLeft.toInt())}s", fontSize = 14.sp, color = NeonCyan)
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

private data class HitFx(val x: Float, val y: Float, val text: String, val color: Color, val life: Float = 1f)
