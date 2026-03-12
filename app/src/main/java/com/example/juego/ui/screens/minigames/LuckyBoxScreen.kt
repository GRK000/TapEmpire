package com.example.juego.ui.screens.minigames

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.juego.R
import com.example.juego.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun LuckyBoxScreen(
    onGameComplete: (Double) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 0=picking, 1=reveal, 2=done
    var phase by remember { mutableIntStateOf(0) }
    var chosenIndex by remember { mutableIntStateOf(-1) }

    // Randomize: one jackpot (x10), one decent (x3), one consolation (x0.5)
    val multipliers = remember {
        val vals = mutableListOf(10.0, 3.0, 0.5)
        vals.shuffle()
        vals
    }
    val labelTexts = listOf(
        stringResource(R.string.lucky_jackpot) to CoinGold,
        stringResource(R.string.lucky_good) to NeonCyan,
        stringResource(R.string.lucky_consolation) to TextMuted
    )
    val labels = multipliers.map { m ->
        when {
            m >= 10.0 -> labelTexts[0]
            m >= 3.0 -> labelTexts[1]
            else -> labelTexts[2]
        }
    }
    var revealedBoxes by remember { mutableStateOf(setOf<Int>()) }
    val bounceAnim = rememberInfiniteTransition(label = "box_bounce")
    val bounce by bounceAnim.animateFloat(
        initialValue = 1f, targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse), label = "b"
    )

    LaunchedEffect(chosenIndex) {
        if (chosenIndex >= 0) {
            phase = 1
            // Reveal chosen first
            revealedBoxes = setOf(chosenIndex)
            delay(1200)
            // Reveal the rest
            revealedBoxes = setOf(0, 1, 2)
            delay(1500)
            phase = 2
        }
    }

    Box(
        modifier = modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFF0A0020), Color(0xFF150030)))
        ),
        contentAlignment = Alignment.Center
    ) {
        when (phase) {
            0 -> Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Text("📦", fontSize = 56.sp)
                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.lucky_title), fontSize = 30.sp, fontWeight = FontWeight.Black, color = CoinGold)
                Text(stringResource(R.string.lucky_subtitle), fontSize = 16.sp, color = TextSecondary)
                Spacer(Modifier.height(40.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    for (i in 0 until 3) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(0.8f)
                                .scale(bounce)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(NeonPurple.copy(alpha = 0.3f), Onyx.copy(alpha = 0.9f))
                                    )
                                )
                                .clickable { chosenIndex = i },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("📦", fontSize = 48.sp)
                                Spacer(Modifier.height(8.dp))
                                Text("#${i + 1}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
                TextButton(onClick = onCancel) { Text(stringResource(R.string.btn_cancel), color = TextMuted) }
            }
            1, 2 -> Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    if (phase == 1) stringResource(R.string.lucky_opening) else labels[chosenIndex].first,
                    fontSize = if (phase == 2) 26.sp else 20.sp,
                    fontWeight = FontWeight.Black,
                    color = if (phase == 2) labels[chosenIndex].second else TextPrimary
                )
                Spacer(Modifier.height(32.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    for (i in 0 until 3) {
                        val revealed = i in revealedBoxes
                        val isChosen = i == chosenIndex
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(0.8f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    when {
                                        revealed && multipliers[i] >= 10.0 -> Brush.verticalGradient(
                                            listOf(CoinGold.copy(alpha = 0.3f), Color(0xFF332200))
                                        )
                                        revealed && multipliers[i] >= 3.0 -> Brush.verticalGradient(
                                            listOf(NeonCyan.copy(alpha = 0.2f), Onyx)
                                        )
                                        revealed -> Brush.verticalGradient(
                                            listOf(Disabled.copy(alpha = 0.2f), Onyx)
                                        )
                                        else -> Brush.verticalGradient(
                                            listOf(NeonPurple.copy(alpha = 0.3f), Onyx.copy(alpha = 0.9f))
                                        )
                                    }
                                )
                                .then(if (isChosen) Modifier.background(
                                    Brush.verticalGradient(listOf(Color.Transparent, labels[chosenIndex].second.copy(alpha = 0.1f)))
                                ) else Modifier),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                if (revealed) {
                                    Text(
                                        when {
                                            multipliers[i] >= 10.0 -> "💎"
                                            multipliers[i] >= 3.0 -> "💰"
                                            else -> "🎁"
                                        },
                                        fontSize = 40.sp
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "x${multipliers[i].let { if (it == 0.5) "0.5" else it.toInt().toString() }}",
                                        fontSize = 18.sp, fontWeight = FontWeight.Black,
                                        color = labels[i].second
                                    )
                                    if (isChosen) {
                                        Spacer(Modifier.height(2.dp))
                                        Text(stringResource(R.string.lucky_your_choice), fontSize = 9.sp, fontWeight = FontWeight.Bold,
                                            color = NeonCyan, textAlign = TextAlign.Center)
                                    }
                                } else {
                                    Text("📦", fontSize = 48.sp)
                                    Text("#${i + 1}", fontSize = 14.sp, color = TextMuted)
                                }
                            }
                        }
                    }
                }
                if (phase == 2) {
                    Spacer(Modifier.height(32.dp))
                    val perf = multipliers[chosenIndex] / 5.0 // x10 → 2.0, x3 → 0.6, x0.5 → 0.1
                    Button(onClick = { onGameComplete(perf.coerceIn(0.1, 2.0)) },
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
