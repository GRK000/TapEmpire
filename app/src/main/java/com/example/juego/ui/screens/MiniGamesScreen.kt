package com.example.juego.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.juego.MiniGame
import com.example.juego.R
import com.example.juego.ui.components.AnimatedBackground
import com.example.juego.ui.components.GlassCard
import com.example.juego.ui.theme.*
import com.example.juego.ui.viewmodel.GameUiState
import com.example.juego.ui.viewmodel.GameViewModel

@Composable
fun MiniGamesScreen(
    viewModel: GameViewModel,
    uiState: GameUiState,
    onNavigateToGame: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        AnimatedBackground(worldTheme = uiState.currentWorld?.theme)

        Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
            Text(
                text = stringResource(R.string.minigames_title),
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary,
                modifier = Modifier.padding(24.dp, 16.dp, 24.dp, 4.dp)
            )
            Text(
                text = stringResource(R.string.minigames_subtitle),
                fontSize = 13.sp,
                color = TextMuted,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(uiState.miniGames) { index, game ->
                    MiniGameCard(
                        game = game,
                        isPlayable = true,
                        onPlay = {
                            if (viewModel.startMiniGame(index)) {
                                val route = when (game.type) {
                                    MiniGame.MiniGameType.COSMIC_BILLIARDS -> "minigame_cosmic_billiards"
                                    MiniGame.MiniGameType.ASTEROID_DODGE -> "minigame_asteroid_dodge"
                                    MiniGame.MiniGameType.STAR_CATCHER -> "minigame_star_catcher"
                                    MiniGame.MiniGameType.GRAVITY_SLINGSHOT -> "minigame_gravity_slingshot"
                                    MiniGame.MiniGameType.TAP_FRENZY -> "minigame_tap_frenzy"
                                    MiniGame.MiniGameType.MEMORY_MATCH -> "minigame_memory_match"
                                    MiniGame.MiniGameType.COIN_RAIN -> "minigame_coin_rain"
                                    MiniGame.MiniGameType.BOSS_BATTLE -> "minigame_boss_battle"
                                    MiniGame.MiniGameType.LUCKY_BOX -> "minigame_lucky_box"
                                    MiniGame.MiniGameType.FORTUNE_WHEEL -> "minigame_fortune_wheel"
                                }
                                onNavigateToGame(route)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MiniGameCard(
    game: MiniGame,
    isPlayable: Boolean = false,
    onPlay: () -> Unit
) {
    val isAvailable = game.isAvailable
    val cooldownSec = game.cooldownRemaining
    val accentColor = if (isPlayable) NeonCyan else NeonPurple

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isAvailable) Modifier.clickable { onPlay() } else Modifier),
        glassColor = if (isAvailable) accentColor.copy(alpha = 0.12f) else Disabled.copy(alpha = 0.05f),
        borderColor = if (isAvailable) accentColor.copy(alpha = 0.3f) else Disabled.copy(alpha = 0.1f),
        cornerRadius = 16.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = game.type.emoji, fontSize = 40.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = game.type.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isAvailable) TextPrimary else TextMuted,
                textAlign = TextAlign.Center
            )
            Text(
                text = game.type.description,
                fontSize = 11.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (isPlayable && isAvailable) {
                Text(
                    text = stringResource(R.string.label_play),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(NeonCyan.copy(alpha = 0.2f), NeonPurple.copy(alpha = 0.2f))
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )
            } else if (isAvailable) {
                Text(
                    text = stringResource(R.string.label_play),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonGreen,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(NeonGreen.copy(alpha = 0.15f))
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )
            } else {
                Text(
                    text = if (cooldownSec > 0) stringResource(R.string.minigames_cooldown, cooldownSec / 60, cooldownSec % 60)
                    else stringResource(R.string.label_daily_limit),
                    fontSize = 12.sp,
                    color = Disabled
                )
            }

            Text(
                text = stringResource(R.string.label_remaining, game.remainingPlays, game.maxPlaysPerDay),
                fontSize = 10.sp,
                color = TextMuted
            )
        }
    }
}
