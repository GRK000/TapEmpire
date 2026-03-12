package com.example.juego.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.juego.GameState
import com.example.juego.Generator
import com.example.juego.R
import com.example.juego.ui.components.AnimatedBackground
import com.example.juego.ui.components.GlassCard
import com.example.juego.ui.components.RepeatingButton
import com.example.juego.ui.theme.*
import com.example.juego.ui.viewmodel.GameUiState
import com.example.juego.ui.viewmodel.GameViewModel

@Composable
fun GeneratorsScreen(
    viewModel: GameViewModel,
    uiState: GameUiState,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        AnimatedBackground(worldTheme = uiState.currentWorld?.theme)

        Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
            // Header
            Text(
                text = stringResource(R.string.generators_title),
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary,
                modifier = Modifier.padding(24.dp, 16.dp, 24.dp, 4.dp)
            )

            // Total production
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                cornerRadius = 12.dp,
                glassColor = NeonCyan.copy(alpha = 0.1f),
                borderColor = NeonCyan.copy(alpha = 0.2f)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(stringResource(R.string.generators_total_production), fontSize = 12.sp, color = TextMuted)
                        Text(
                            text = "⚙️ ${GameState.fmt(uiState.perSecond)}/seg",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = NeonCyan
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(stringResource(R.string.label_coins), fontSize = 12.sp, color = TextMuted)
                        Text(
                            text = "💰 ${GameState.fmt(uiState.coins)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = CoinGold
                        )
                    }
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Filter: show generators that belong to current world or are universal (-1)
                val worldGens = uiState.generators.mapIndexed { i, g -> i to g }
                    .filter { (_, g) ->
                        g.requiredWorldIndex == -1 || g.requiredWorldIndex <= uiState.currentWorldIndex
                    }
                itemsIndexed(worldGens) { _, (index, gen) ->
                    GeneratorCard(
                        generator = gen,
                        coins = uiState.coins,
                        onBuy = { viewModel.buyGenerator(index) }
                    )
                }
            }
        }
    }
}

@Composable
fun GeneratorCard(
    generator: Generator,
    coins: Double,
    onBuy: () -> Unit
) {
    val isUnlocked = generator.isUnlocked
    val canAfford = coins >= generator.currentCost
    val accentColor = if (isUnlocked && canAfford) NeonGreen else if (isUnlocked) NeonPurple else Disabled

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        glassColor = if (!isUnlocked) Disabled.copy(alpha = 0.05f) else GlassWhite,
        borderColor = if (!isUnlocked) Disabled.copy(alpha = 0.1f) else GlassBorder,
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
                    .size(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.25f),
                                accentColor.copy(alpha = 0.05f)
                            )
                        )
                    )
            ) {
                Text(text = generator.emoji, fontSize = 26.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = generator.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isUnlocked) TextPrimary else TextMuted
                    )
                    if (generator.owned > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "x${generator.owned}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(NeonCyan.copy(alpha = 0.15f))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }

                if (isUnlocked) {
                    Text(
                        text = stringResource(R.string.label_per_sec, GameState.fmt(generator.productionPerSecond)),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = NeonCyan
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = Disabled,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.generators_need_coins, GameState.fmt(generator.unlockRequirement.toDouble())),
                            fontSize = 11.sp,
                            color = Disabled
                        )
                    }
                }

                Text(
                    text = generator.description,
                    fontSize = 11.sp,
                    color = TextMuted,
                    maxLines = 3
                )
            }

            if (isUnlocked) {
                RepeatingButton(
                    onClick = onBuy,
                    enabled = canAfford,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (canAfford) NeonGreen else Disabled,
                        contentColor = if (canAfford) Color.Black else TextMuted
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = GameState.fmt(generator.currentCost),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
