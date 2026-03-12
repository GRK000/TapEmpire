package com.example.juego.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.juego.Achievement
import com.example.juego.GameState
import com.example.juego.R
import com.example.juego.ui.components.AnimatedBackground
import com.example.juego.ui.components.GlassCard
import com.example.juego.ui.theme.*
import com.example.juego.ui.viewmodel.GameUiState

@Composable
fun AchievementsScreen(
    uiState: GameUiState,
    modifier: Modifier = Modifier
) {
    val unlocked = uiState.achievements.count { it.isUnlocked }
    val total = uiState.achievements.size

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedBackground(worldTheme = uiState.currentWorld?.theme)

        Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
            Text(
                text = stringResource(R.string.achievements_title),
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary,
                modifier = Modifier.padding(24.dp, 16.dp, 24.dp, 4.dp)
            )
            Text(
                text = stringResource(R.string.achievements_completed, unlocked, total),
                fontSize = 14.sp,
                color = CoinGold,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(uiState.achievements) { _, achievement ->
                    AchievementCard(
                        achievement = achievement,
                        gameState = uiState
                    )
                }
            }
        }
    }
}

@Composable
fun AchievementCard(
    achievement: Achievement,
    gameState: GameUiState
) {
    val progress = getAchievementCurrentValue(achievement, gameState)
    val progressPercent = (progress / achievement.targetValue).coerceIn(0.0, 1.0).toFloat()

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        glassColor = if (achievement.isUnlocked) CoinGold.copy(alpha = 0.08f) else GlassWhite,
        borderColor = if (achievement.isUnlocked) CoinGold.copy(alpha = 0.3f) else GlassBorder,
        cornerRadius = 12.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (achievement.isUnlocked) CoinGold.copy(alpha = 0.2f)
                        else Disabled.copy(alpha = 0.1f)
                    )
            ) {
                Text(
                    text = if (achievement.isUnlocked) achievement.emoji else "🔒",
                    fontSize = 22.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = achievement.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (achievement.isUnlocked) CoinGold else TextPrimary
                )
                Text(
                    text = achievement.description,
                    fontSize = 11.sp,
                    color = TextMuted
                )

                if (!achievement.isUnlocked) {
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { progressPercent },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = NeonPurple,
                        trackColor = Disabled.copy(alpha = 0.3f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (achievement.isUnlocked) "✅" else "${(progressPercent * 100).toInt()}%",
                    fontSize = 12.sp,
                    color = if (achievement.isUnlocked) NeonGreen else TextMuted,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "+${GameState.fmt(achievement.reward)}",
                    fontSize = 11.sp,
                    color = CoinGold
                )
            }
        }
    }
}

private fun getAchievementCurrentValue(achievement: Achievement, state: GameUiState): Double {
    return when (achievement.type) {
        Achievement.AchievementType.TOTAL_TAPS -> state.totalTaps.toDouble()
        Achievement.AchievementType.TOTAL_COINS_EARNED -> state.totalCoinsEarned
        Achievement.AchievementType.COINS_OWNED -> state.coins
        Achievement.AchievementType.PRODUCTION_PER_SECOND -> state.perSecond
        Achievement.AchievementType.PRESTIGE_LEVEL -> state.prestigeLevel.toDouble()
        Achievement.AchievementType.MAX_COMBO -> state.maxComboReached.toDouble()
        else -> 0.0
    }
}
