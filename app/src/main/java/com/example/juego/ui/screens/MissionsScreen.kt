package com.example.juego.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.juego.DailyMission
import com.example.juego.GameState
import com.example.juego.R
import com.example.juego.ui.components.AnimatedBackground
import com.example.juego.ui.components.GlassCard
import com.example.juego.ui.theme.*
import com.example.juego.ui.viewmodel.GameUiState
import com.example.juego.ui.viewmodel.GameViewModel
import com.example.juego.ui.viewmodel.MissionSnapshot

@Composable
fun MissionsScreen(
    viewModel: GameViewModel,
    uiState: GameUiState,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        AnimatedBackground(worldTheme = uiState.currentWorld?.theme)

        Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
            Text(
                text = stringResource(R.string.missions_title),
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary,
                modifier = Modifier.padding(24.dp, 16.dp, 24.dp, 4.dp)
            )
            Text(
                text = stringResource(R.string.missions_subtitle),
                fontSize = 13.sp,
                color = TextMuted,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(uiState.missionSnapshots) { index, mission ->
                    MissionCard(
                        mission = mission,
                        onClaim = { viewModel.claimMission(index) }
                    )
                }
            }
        }
    }
}

@Composable
fun MissionCard(
    mission: MissionSnapshot,
    onClaim: () -> Unit
) {
    val progressPercent: Float = (mission.progressPercent.toFloat() / 100f).coerceIn(0f, 1f)
    val difficultyColor = when (mission.difficulty) {
        1 -> NeonGreen
        2 -> Warning
        3 -> NeonRed
        else -> TextMuted
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        glassColor = if (mission.claimed) NeonGreen.copy(alpha = 0.05f)
        else if (mission.completed) CoinGold.copy(alpha = 0.1f)
        else GlassWhite,
        borderColor = if (mission.claimed) NeonGreen.copy(alpha = 0.2f)
        else if (mission.completed) CoinGold.copy(alpha = 0.4f)
        else GlassBorder,
        cornerRadius = 14.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Emoji icon
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(difficultyColor.copy(alpha = 0.15f))
            ) {
                Text(text = mission.type.emoji, fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = mission.description,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = buildString { repeat(mission.difficulty.coerceAtMost(5)) { append("⭐") } },
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Progress bar
                LinearProgressIndicator(
                    progress = { progressPercent },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (mission.completed) NeonGreen else NeonPurple,
                    trackColor = Disabled.copy(alpha = 0.3f)
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "💰 +${GameState.fmt(mission.coinReward)}",
                    fontSize = 12.sp,
                    color = CoinGold
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            when {
                mission.claimed -> {
                    Text(
                        text = "✅",
                        fontSize = 24.sp
                    )
                }
                mission.completed -> {
                    Surface(
                        onClick = { onClaim() },
                        modifier = Modifier.height(36.dp),
                        shape = RoundedCornerShape(18.dp),
                        color = NeonGreen
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.btn_claim),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepSpace,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                else -> {
                    Text(
                        text = "${(progressPercent * 100).toInt()}%",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
                }
            }
        }
    }
}
