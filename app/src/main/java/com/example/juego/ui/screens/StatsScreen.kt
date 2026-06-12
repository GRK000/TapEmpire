package com.example.juego.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.juego.GameState
import com.example.juego.R
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
    val tabTitles = listOf(
        "🌍 ${stringResource(R.string.stats_tab_global)}",
        "⭐ ${stringResource(R.string.stats_tab_prestige)}"
    )

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedBackground(worldTheme = uiState.currentWorld?.theme)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = "📊 ${stringResource(R.string.stats_title)}",
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
        "💰 ${stringResource(R.string.stats_total_earned_all)}" to GameState.fmt(uiState.globalTotalCoinsEarned),
        "👆 ${stringResource(R.string.stats_total_taps_all)}" to "${uiState.globalTotalTaps}",
        "💥 ${stringResource(R.string.stats_crits_all)}" to "${uiState.globalTotalCriticalHits}",
        "🏭 ${stringResource(R.string.stats_generators_all)}" to "${uiState.globalTotalGeneratorsBought}",
        "⭐ ${stringResource(R.string.stats_total_prestiges)}" to "${uiState.totalPrestigesPerformed}",
        "⭐ ${stringResource(R.string.stats_prestige_level)}" to "${uiState.prestigeLevel}",
        "🌍 ${stringResource(R.string.stats_worlds_unlocked)}" to "${uiState.worlds.count { it.isUnlocked }}/${uiState.worlds.size}",
        "🏆 ${stringResource(R.string.stats_achievements)}" to "${uiState.achievements.count { it.isUnlocked }}/${uiState.achievements.size}",
        "🐾 ${stringResource(R.string.stats_pets_owned)}" to "${uiState.pets.count { it.isOwned }}/${uiState.pets.size}",
        "🎮 ${stringResource(R.string.stats_minigames_played)}" to "${uiState.totalMiniGamesPlayed}",
        "☄️ ${stringResource(R.string.stats_comets_caught)}" to "${uiState.totalGoldenComets}"
    )

    stats.forEach { (label, value) ->
        StatRow(label = label, value = value)
    }
}

@Composable
private fun PrestigeStatsTab(uiState: GameUiState) {
    val stats = listOf(
        "💰 ${stringResource(R.string.stats_coins_this_run)}" to GameState.fmt(uiState.totalCoinsEarned),
        "💰 ${stringResource(R.string.stats_current_coins)}" to GameState.fmt(uiState.coins),
        "👆 ${stringResource(R.string.stats_taps_this_run)}" to "${uiState.totalTaps}",
        "👆 ${stringResource(R.string.stats_per_tap)}" to GameState.fmt(uiState.perTap),
        "⚙️ ${stringResource(R.string.stats_per_second)}" to GameState.fmt(uiState.perSecond),
        "🏭 ${stringResource(R.string.stats_generators_bought)}" to "${uiState.totalGeneratorsBought}",
        "🌍 ${stringResource(R.string.stats_current_world)}" to (uiState.currentWorld?.theme?.let { "${it.emoji} ${it.name}" } ?: "-"),
        "⭐ ${stringResource(R.string.stats_prestige_multiplier)}" to "x${String.format("%.2f", uiState.prestigeMultiplier)}",
        "🎯 ${stringResource(R.string.stats_crit_chance)}" to "${(uiState.criticalChance * 100).toInt()}%",
        "💥 ${stringResource(R.string.stats_crit_multiplier)}" to "x${String.format("%.1f", uiState.criticalMultiplier)}",
        "🐾 ${stringResource(R.string.stats_active_pet)}" to (uiState.activePet?.type?.name ?: stringResource(R.string.stats_none))
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
