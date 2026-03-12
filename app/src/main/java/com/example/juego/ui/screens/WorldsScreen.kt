package com.example.juego.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.juego.GameState
import com.example.juego.R
import com.example.juego.World
import com.example.juego.ui.components.AnimatedBackground
import com.example.juego.ui.components.GlassCard
import com.example.juego.ui.components.getWorldColors
import com.example.juego.ui.theme.*
import com.example.juego.ui.viewmodel.GameUiState
import com.example.juego.ui.viewmodel.GameViewModel

@Composable
fun WorldsScreen(
    viewModel: GameViewModel,
    uiState: GameUiState,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        AnimatedBackground(worldTheme = uiState.currentWorld?.theme)

        Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
            // Header
            Text(
                text = stringResource(R.string.worlds_title),
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary,
                modifier = Modifier.padding(24.dp, 16.dp, 24.dp, 4.dp)
            )
            Text(
                text = stringResource(R.string.worlds_subtitle),
                fontSize = 13.sp,
                color = TextMuted,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(uiState.worlds) { index, world ->
                    WorldCard(
                        world = world,
                        isSelected = index == uiState.currentWorldIndex,
                        coins = uiState.coins,
                        prestigeLevel = uiState.prestigeLevel,
                        onSelect = { viewModel.switchWorld(index) },
                        onBuy = { viewModel.buyWorld(index) }
                    )
                }
            }
        }
    }
}

@Composable
fun WorldCard(
    world: World,
    isSelected: Boolean,
    coins: Double,
    prestigeLevel: Int,
    onSelect: () -> Unit,
    onBuy: () -> Unit
) {
    val theme = world.theme
    val colors = getWorldColors(theme)
    val accentColor = colors.first

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (world.isUnlocked) Modifier.clickable { onSelect() } else Modifier),
        glassColor = if (isSelected) accentColor.copy(alpha = 0.2f) else GlassWhite,
        borderColor = if (isSelected) accentColor.copy(alpha = 0.6f) else GlassBorder,
        cornerRadius = 16.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Icon
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.3f),
                                accentColor.copy(alpha = 0.1f)
                            )
                        )
                    )
            ) {
                Text(text = theme.emoji, fontSize = 28.sp)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = theme.name,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (world.isUnlocked) TextPrimary else TextMuted
                    )
                    if (isSelected) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.worlds_active),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(accentColor.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = theme.description,
                    fontSize = 12.sp,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Bonuses
                world.specialBonuses.forEach { bonus ->
                    Text(
                        text = "✦ $bonus",
                        fontSize = 11.sp,
                        color = accentColor.copy(alpha = 0.8f)
                    )
                }

                Text(
                    text = "🏭 x${String.format("%.2f", world.productionMultiplier)} ${stringResource(R.string.worlds_production)}",
                    fontSize = 11.sp,
                    color = NeonCyan,
                    fontWeight = FontWeight.Medium
                )
            }

            // Right side: lock/active indicator
            if (!world.isUnlocked) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Disabled,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = GameState.fmt(world.unlockCost),
                        fontSize = 12.sp,
                        color = if (coins >= world.unlockCost) CoinGold else Error,
                        fontWeight = FontWeight.Bold
                    )
                    if (world.requiredPrestigeLevel > 0) {
                        Text(
                            text = stringResource(R.string.worlds_prestige_req, world.requiredPrestigeLevel),
                            fontSize = 10.sp,
                            color = if (prestigeLevel >= world.requiredPrestigeLevel) PrestigeCyan else Error
                        )
                    }

                    if (world.canUnlock(coins, prestigeLevel)) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = onBuy,
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text(stringResource(R.string.btn_buy), fontSize = 11.sp)
                        }
                    }
                }
            } else if (isSelected) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = "Active",
                    tint = accentColor,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
