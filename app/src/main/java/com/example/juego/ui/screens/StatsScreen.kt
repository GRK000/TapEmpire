package com.example.juego.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.juego.GameState
import com.example.juego.ui.components.AnimatedBackground
import com.example.juego.ui.components.GlassCard
import com.example.juego.ui.theme.*
import com.example.juego.ui.viewmodel.GameUiState

@Composable
fun StatsScreen(
    uiState: GameUiState,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("🌍 Global", "⭐ Prestige")

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedBackground(worldTheme = uiState.currentWorld?.theme)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = "📊 Statistics",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary,
                modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)
            )

            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Onyx.copy(alpha = 0.7f),
                contentColor = NeonCyan,
                indicator = { tabPositions ->
                    if (selectedTab < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = NeonCyan
                        )
                    }
                }
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 14.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) NeonCyan else TextMuted
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                when (selectedTab) {
                    0 -> GlobalStatsTab(uiState)
                    1 -> PrestigeStatsTab(uiState)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun GlobalStatsTab(uiState: GameUiState) {
    val stats = listOf(
        "💰 Total earned (all time)" to GameState.fmt(uiState.globalTotalCoinsEarned),
        "👆 Total taps (all time)" to "${uiState.globalTotalTaps}",
        "💥 Critical hits (all time)" to "${uiState.globalTotalCriticalHits}",
        "🏭 Generators bought (all time)" to "${uiState.globalTotalGeneratorsBought}",
        "⭐ Total prestiges" to "${uiState.totalPrestigesPerformed}",
        "⭐ Prestige level" to "${uiState.prestigeLevel}",
        "🌍 Worlds unlocked" to "${uiState.worlds.count { it.isUnlocked }}/${uiState.worlds.size}",
        "🏆 Achievements" to "${uiState.achievements.count { it.isUnlocked }}/${uiState.achievements.size}",
        "🐾 Pets owned" to "${uiState.pets.count { it.isOwned }}/${uiState.pets.size}",
        "🎮 Minigames played" to "${uiState.totalMiniGamesPlayed}"
    )

    stats.forEach { (label, value) ->
        StatRow(label = label, value = value)
    }
}

@Composable
private fun PrestigeStatsTab(uiState: GameUiState) {
    val stats = listOf(
        "💰 Coins earned (this prestige)" to GameState.fmt(uiState.totalCoinsEarned),
        "💰 Current coins" to GameState.fmt(uiState.coins),
        "👆 Taps (this prestige)" to "${uiState.totalTaps}",
        "👆 Per tap" to GameState.fmt(uiState.perTap),
        "⚙️ Per second" to GameState.fmt(uiState.perSecond),
        "🏭 Generators bought" to "${uiState.totalGeneratorsBought}",
        "🌍 Current world" to (uiState.currentWorld?.theme?.let { "${it.emoji} ${it.name}" } ?: "-"),
        "⭐ Prestige multiplier" to "x${String.format("%.2f", uiState.prestigeMultiplier)}",
        "🎯 Critical chance" to "${(uiState.criticalChance * 100).toInt()}%",
        "💥 Critical multiplier" to "x${String.format("%.1f", uiState.criticalMultiplier)}",
        "🐾 Active pet" to (uiState.activePet?.type?.name ?: "None")
    )

    stats.forEach { (label, value) ->
        StatRow(label = label, value = value)
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        cornerRadius = 10.dp
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                color = TextSecondary
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = CoinGold
            )
        }
    }
}
