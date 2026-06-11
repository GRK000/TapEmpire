package com.example.juego.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.example.juego.ui.components.Haptics
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

            // Selector de cantidad de compra
            var buyAmount by rememberSaveable { mutableStateOf(1) }
            val maxLabel = stringResource(R.string.label_max)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                listOf(1 to "×1", 10 to "×10", GameViewModel.BUY_MAX to "×$maxLabel").forEach { (amount, label) ->
                    val selected = buyAmount == amount
                    Text(
                        text = label,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = if (selected) Color.Black else TextMuted,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selected) NeonCyan else GlassWhite)
                            .clickable { buyAmount = amount }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    )
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
                        buyAmount = buyAmount,
                        onBuy = { viewModel.buyGenerator(index, buyAmount) }
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
    buyAmount: Int = 1,
    onBuy: () -> Unit
) {
    val isUnlocked = generator.isUnlocked
    // Unidades y coste según la cantidad seleccionada (×1, ×10 o MAX)
    val unitsToBuy = if (buyAmount == GameViewModel.BUY_MAX) {
        generator.getMaxAffordable(coins)
    } else {
        buyAmount
    }
    val displayCost = if (unitsToBuy > 0) generator.getCostForAmount(unitsToBuy) else generator.currentCost
    val canAfford = unitsToBuy > 0 && coins >= displayCost
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.label_per_sec, GameState.fmt(generator.productionPerSecond)),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = NeonCyan
                        )
                        if (generator.milestoneBonus > 1) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "⚡×${generator.milestoneBonus.toInt()}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CoinGold,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(CoinGold.copy(alpha = 0.15f))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    // Progreso hacia el próximo hito (duplica la producción)
                    val nextMilestone = generator.nextMilestone
                    if (generator.owned > 0 && nextMilestone > 0) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.milestone_label, generator.owned, nextMilestone),
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                            LinearProgressIndicator(
                                progress = { (generator.owned.toFloat() / nextMilestone).coerceIn(0f, 1f) },
                                color = CoinGold,
                                trackColor = GlassWhite,
                                modifier = Modifier
                                    .fillMaxWidth(0.7f)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                            )
                        }
                    }
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
                val context = LocalContext.current
                RepeatingButton(
                    onClick = {
                        Haptics.medium(context)
                        onBuy()
                    },
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
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (unitsToBuy > 1) {
                            Text(
                                text = "×$unitsToBuy",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = GameState.fmt(displayCost),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
