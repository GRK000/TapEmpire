package com.example.juego.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.juego.R
import com.example.juego.ui.components.AnimatedBackground
import com.example.juego.ui.components.GlassCard
import com.example.juego.ui.theme.*
import com.example.juego.ui.viewmodel.GameUiState

data class MoreMenuItem(
    val title: String,
    val emoji: String,
    val route: String,
    val color: Color,
    val badge: Int = 0,
    val locked: Boolean = false,
    val lockMessage: String = ""
)

@Composable
fun MoreScreen(
    uiState: GameUiState,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val unclaimedMissions = uiState.missionSnapshots.count { it.completed && !it.claimed }
    val sickPets = uiState.pets.count { it.isOwned && it.isAlive && it.diseases.isNotEmpty() }

    val menuItems = mutableListOf(
        MoreMenuItem(stringResource(R.string.premium_shop_title), "💎", "premium_shop", GemPurple,
            badge = if (uiState.monetizationState.starterPackAvailable) 1 else 0),
        MoreMenuItem(stringResource(R.string.more_shop), "🛒", "shop", NeonPurple),
        MoreMenuItem(stringResource(R.string.more_achievements), "🏆", "achievements", CoinGold,
            badge = uiState.achievements.count { it.isUnlocked }),
        MoreMenuItem(stringResource(R.string.more_prestige), "⭐", "prestige", PrestigeCyan),
        MoreMenuItem(stringResource(R.string.more_missions), "📋", "missions", NeonGreen, badge = unclaimedMissions),
        MoreMenuItem(stringResource(R.string.events_title), "⚡", "events", Warning,
            badge = (uiState.activeGameEvents as? List<com.example.juego.GameEvent>)?.count { it.needsChoice() } ?: 0),
        MoreMenuItem("Business Map", "🗺️", "business_map", NeonOrange),
    )
    // Workers & Contracts — always visible, locked until World 3
    val world3LockMsg = stringResource(R.string.locked_until_world, "Tokyo Tech")
    menuItems.add(MoreMenuItem(stringResource(R.string.workers_title), "👷", "workers", Color(0xFF42A5F5),
        badge = if (uiState.workersUnlocked) uiState.ownedWorkers.count { it.mood == com.example.juego.Worker.WorkerMood.ON_STRIKE } else 0,
        locked = !uiState.workersUnlocked,
        lockMessage = world3LockMsg))
    menuItems.add(MoreMenuItem(stringResource(R.string.contracts_title), "📋", "contracts", Color(0xFF66BB6A),
        badge = if (uiState.workersUnlocked) uiState.ownedWorkers.count { it.mood == com.example.juego.Worker.WorkerMood.ON_STRIKE } else 0,
        locked = !uiState.workersUnlocked,
        lockMessage = world3LockMsg))
    menuItems.addAll(listOf(
        MoreMenuItem(stringResource(R.string.more_pets), "🐾", "pets", NeonPink, badge = sickPets),
    ))
    // Nursery — always visible, locked until World 3
    menuItems.add(MoreMenuItem(stringResource(R.string.more_nursery), "🏠", "nursery", NeonOrange,
        badge = if (uiState.workersUnlocked) uiState.pets.count { it.isOwned } else 0,
        locked = !uiState.workersUnlocked,
        lockMessage = world3LockMsg))
    menuItems.add(MoreMenuItem(stringResource(R.string.more_stats), "📊", "stats", NeonCyan))

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedBackground(worldTheme = uiState.currentWorld?.theme)

        Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
            Text(
                text = stringResource(R.string.more_title),
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary,
                modifier = Modifier.padding(24.dp, 16.dp, 24.dp, 12.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(menuItems.size) { index ->
                    val item = menuItems[index]
                    MoreMenuCard(
                        item = item,
                        onClick = { onNavigate(item.route) }
                    )
                }
            }
        }
    }
}

@Composable
fun MoreMenuCard(
    item: MoreMenuItem,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.2f)
            .then(
                if (item.locked) Modifier else Modifier.clickable { onClick() }
            )
    ) {
        GlassCard(
            modifier = Modifier.fillMaxSize(),
            glassColor = if (item.locked) Color.White.copy(alpha = 0.03f) else item.color.copy(alpha = 0.1f),
            borderColor = if (item.locked) Disabled.copy(alpha = 0.15f) else item.color.copy(alpha = 0.25f),
            cornerRadius = 16.dp
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    if (item.locked) {
                        // Locked: show emoji dimmed with lock overlay
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = item.emoji,
                                fontSize = 36.sp,
                                modifier = Modifier.graphicsLayer { alpha = 0.3f }
                            )
                            Text(
                                text = "🔒",
                                fontSize = 22.sp,
                                modifier = Modifier.offset(x = 14.dp, y = 12.dp)
                            )
                        }
                    } else {
                        Text(text = item.emoji, fontSize = 36.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = item.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (item.locked) TextMuted else TextPrimary,
                        textAlign = TextAlign.Center
                    )
                    if (item.locked && item.lockMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.lockMessage,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Warning.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center,
                            maxLines = 2
                        )
                    }
                }
            }
        }

        if (item.badge > 0 && !item.locked) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(22.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(Error)
            ) {
                Text(
                    text = "${item.badge}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        }
    }
}
