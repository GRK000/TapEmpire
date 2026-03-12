package com.example.juego.ui.screens.minigames

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.juego.R
import com.example.juego.ui.theme.*
import kotlinx.coroutines.delay

private data class MemoryCard(
    val id: Int,
    val emoji: String,
    val pairId: Int,
    var faceUp: Boolean = false,
    var matched: Boolean = false
)

@Composable
fun MemoryMatchScreen(
    onGameComplete: (Double) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var phase by remember { mutableIntStateOf(0) } // 0=countdown, 1=play, 2=done
    var countdown by remember { mutableIntStateOf(3) }
    var moves by remember { mutableIntStateOf(0) }
    var matchesFound by remember { mutableIntStateOf(0) }
    var timeLeft by remember { mutableFloatStateOf(45f) }
    val totalPairs = 8

    val emojis = remember { listOf("🚀","🌟","🪐","⚡","💎","🔮","🌌","🎱") }
    var cards by remember {
        val list = emojis.mapIndexed { i, e ->
            listOf(MemoryCard(i * 2, e, i), MemoryCard(i * 2 + 1, e, i))
        }.flatten().shuffled()
        mutableStateOf(list)
    }
    var firstFlipped by remember { mutableStateOf<Int?>(null) }
    var secondFlipped by remember { mutableStateOf<Int?>(null) }
    var lockInput by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        for (i in 3 downTo 1) { countdown = i; delay(800) }
        phase = 1
        val start = System.currentTimeMillis()
        while (phase == 1) {
            timeLeft = (45f - (System.currentTimeMillis() - start) / 1000f).coerceAtLeast(0f)
            if (timeLeft <= 0f) { phase = 2; break }
            delay(50)
        }
    }

    // Check for matches
    LaunchedEffect(secondFlipped) {
        val first = firstFlipped ?: return@LaunchedEffect
        val second = secondFlipped ?: return@LaunchedEffect
        lockInput = true
        delay(600)
        val c1 = cards.find { it.id == first }
        val c2 = cards.find { it.id == second }
        if (c1 != null && c2 != null && c1.pairId == c2.pairId) {
            cards = cards.map { if (it.id == first || it.id == second) it.copy(matched = true, faceUp = true) else it }
            matchesFound++
            if (matchesFound >= totalPairs) phase = 2
        } else {
            cards = cards.map { if (it.id == first || it.id == second) it.copy(faceUp = false) else it }
        }
        firstFlipped = null; secondFlipped = null; lockInput = false
    }

    Box(
        modifier = modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFF050520), Color(0xFF0A0A30)))
        )
    ) {
        when (phase) {
            0 -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🧠", fontSize = 64.sp)
                    Spacer(Modifier.height(16.dp))
                    Text(stringResource(R.string.memory_title), fontSize = 32.sp, fontWeight = FontWeight.Black, color = NeonPurple)
                    Text(stringResource(R.string.memory_subtitle), fontSize = 14.sp, color = TextMuted)
                    Spacer(Modifier.height(32.dp))
                    Text("$countdown", fontSize = 72.sp, fontWeight = FontWeight.Black, color = NeonCyan)
                }
            }
            1 -> Column(
                modifier = Modifier.fillMaxSize().systemBarsPadding().padding(16.dp)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("🧠 $matchesFound/$totalPairs", fontSize = 20.sp, fontWeight = FontWeight.Black, color = NeonPurple)
                    Text(stringResource(R.string.memory_moves, moves), fontSize = 14.sp, color = TextMuted)
                    Text("⏱️ ${timeLeft.toInt()}s", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                        color = if (timeLeft < 10f) NeonRed else TextPrimary)
                }
                Spacer(Modifier.height(16.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(cards.size) { idx ->
                        val card = cards[idx]
                        Box(
                            modifier = Modifier
                                .aspectRatio(0.75f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    when {
                                        card.matched -> NeonGreen.copy(alpha = 0.15f)
                                        card.faceUp -> NeonPurple.copy(alpha = 0.2f)
                                        else -> Onyx.copy(alpha = 0.8f)
                                    }
                                )
                                .then(
                                    if (!card.faceUp && !card.matched && !lockInput)
                                        Modifier.clickable {
                                            if (firstFlipped == null) {
                                                firstFlipped = card.id
                                                cards = cards.map { if (it.id == card.id) it.copy(faceUp = true) else it }
                                                moves++
                                            } else if (secondFlipped == null && card.id != firstFlipped) {
                                                secondFlipped = card.id
                                                cards = cards.map { if (it.id == card.id) it.copy(faceUp = true) else it }
                                                moves++
                                            }
                                        }
                                    else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (card.faceUp || card.matched) {
                                Text(card.emoji, fontSize = 28.sp)
                            } else {
                                Text("❓", fontSize = 26.sp)
                            }
                        }
                    }
                }
            }
            2 -> {
                val won = matchesFound >= totalPairs
                val perf = if (won) (1.0 + (45.0 - timeLeft) / 45.0 * -0.5 + (30.0 - moves) / 30.0 * 0.5).coerceIn(0.3, 2.0)
                           else (matchesFound.toDouble() / totalPairs * 0.8).coerceIn(0.1, 1.0)
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                        Text("🧠", fontSize = 56.sp)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            if (won) { if (moves <= 20) stringResource(R.string.memory_result_perfect) else stringResource(R.string.memory_result_good) }
                            else stringResource(R.string.memory_result_timeout),
                            fontSize = 24.sp, fontWeight = FontWeight.Black,
                            color = if (won && moves <= 20) CoinGold else TextPrimary
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(stringResource(R.string.memory_pairs, matchesFound, totalPairs), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = NeonPurple)
                        Text(stringResource(R.string.memory_moves, moves), fontSize = 16.sp, color = TextMuted)
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
