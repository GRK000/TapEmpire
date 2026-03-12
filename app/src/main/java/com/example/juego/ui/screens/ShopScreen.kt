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
import com.example.juego.GameState
import com.example.juego.R
import com.example.juego.Upgrade
import com.example.juego.ui.components.AnimatedBackground
import com.example.juego.ui.components.GlassCard
import com.example.juego.ui.components.RepeatingButton
import com.example.juego.ui.theme.*
import com.example.juego.ui.viewmodel.GameUiState
import com.example.juego.ui.viewmodel.GameViewModel

@Composable
fun ShopScreen(
    viewModel: GameViewModel,
    uiState: GameUiState,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        AnimatedBackground(worldTheme = uiState.currentWorld?.theme)

        Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
            Text(
                text = stringResource(R.string.shop_title),
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary,
                modifier = Modifier.padding(24.dp, 16.dp, 24.dp, 4.dp)
            )

            // Coins
            Text(
                text = "💰 ${GameState.fmt(uiState.coins)}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = CoinGold,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(uiState.upgrades) { index, upgrade ->
                    UpgradeCard(
                        upgrade = upgrade,
                        coins = uiState.coins,
                        onBuy = { viewModel.buyUpgrade(index) }
                    )
                }
            }
        }
    }
}

@Composable
fun UpgradeCard(
    upgrade: Upgrade,
    coins: Double,
    onBuy: () -> Unit
) {
    val canAfford = coins >= upgrade.currentCost
    val isMax = upgrade.isMaxLevel
    val progressPercent = upgrade.currentLevel.toFloat() / upgrade.maxLevel.toFloat()

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 14.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Emoji
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(NeonPurple.copy(alpha = 0.15f))
            ) {
                Text(text = upgrade.emoji, fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = upgrade.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.shop_level, upgrade.currentLevel, upgrade.maxLevel),
                        fontSize = 11.sp,
                        color = NeonCyan,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = upgrade.description,
                    fontSize = 11.sp,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Progress bar
                LinearProgressIndicator(
                    progress = { progressPercent },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = if (isMax) NeonGreen else NeonPurple,
                    trackColor = Disabled.copy(alpha = 0.3f)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            if (isMax) {
                Text(
                    text = stringResource(R.string.label_max),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = NeonGreen,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(NeonGreen.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            } else {
                RepeatingButton(
                    onClick = onBuy,
                    enabled = canAfford,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (canAfford) NeonPurple else Disabled,
                        contentColor = TextPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(
                        text = GameState.fmt(upgrade.currentCost),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
