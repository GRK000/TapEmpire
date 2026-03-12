package com.example.juego.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.juego.GameState
import com.example.juego.Generator
import com.example.juego.R
import com.example.juego.Worker
import com.example.juego.ui.components.AnimatedBackground
import com.example.juego.ui.components.GlassCard
import com.example.juego.ui.components.RepeatingButton
import com.example.juego.ui.theme.*
import com.example.juego.ui.viewmodel.GameUiState
import com.example.juego.ui.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessMapScreen(
    viewModel: GameViewModel,
    uiState: GameUiState,
    modifier: Modifier = Modifier
) {
    var selectedGenIndex by remember { mutableIntStateOf(-1) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Filter generators for current world and all previous worlds
    val worldGens = remember(uiState.generators, uiState.currentWorldIndex) {
        uiState.generators.mapIndexed { i, g -> i to g }
            .filter { (_, g) ->
                g.requiredWorldIndex == -1 || g.requiredWorldIndex <= uiState.currentWorldIndex
            }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedBackground(worldTheme = uiState.currentWorld?.theme)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            // Header
            Text(
                text = stringResource(R.string.business_map_title),
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary,
                modifier = Modifier.padding(24.dp, 16.dp, 24.dp, 4.dp)
            )

            val worldName = uiState.currentWorld?.theme?.let {
                "${it.emoji} ${it.name}"
            } ?: "Unknown"
            Text(
                text = worldName,
                fontSize = 14.sp,
                color = TextMuted,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Business grid (map)
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(worldGens) { _, (globalIndex, gen) ->
                    BusinessMapTile(
                        generator = gen,
                        onClick = { selectedGenIndex = globalIndex }
                    )
                }
            }
        }
    }

    // Bottom sheet for selected generator
    if (selectedGenIndex >= 0) {
        val gen = uiState.generators.getOrNull(selectedGenIndex)
        if (gen != null) {
            ModalBottomSheet(
                onDismissRequest = { selectedGenIndex = -1 },
                sheetState = sheetState,
                containerColor = Onyx.copy(alpha = 0.97f),
                contentColor = TextPrimary
            ) {
                BusinessDetailSheet(
                    generator = gen,
                    genIndex = selectedGenIndex,
                    coins = uiState.coins,
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun BusinessMapTile(
    generator: Generator,
    onClick: () -> Unit
) {
    val isOwned = generator.owned > 0
    val isUnlocked = generator.isUnlocked

    // Pulsing animation for producing generators
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isOwned && generator.productionPerSecond > 0) 1.06f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tileScale"
    )

    val accentColor = when {
        !isUnlocked -> Disabled
        isOwned -> NeonGreen
        else -> NeonPurple
    }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .scale(pulseScale)
            .clickable(enabled = isUnlocked) { onClick() },
        glassColor = if (!isUnlocked) Disabled.copy(alpha = 0.05f)
        else accentColor.copy(alpha = 0.08f),
        borderColor = accentColor.copy(alpha = 0.3f),
        cornerRadius = 16.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Text(text = generator.emoji, fontSize = 36.sp)

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = generator.name.replace(Regex("^\\S+\\s"), ""), // Remove emoji prefix
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isUnlocked) TextPrimary else TextMuted,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (isOwned) {
                Text(
                    text = "x${generator.owned}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(NeonCyan.copy(alpha = 0.15f))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                )
                Text(
                    text = "${GameState.fmt(generator.productionPerSecond)}/s",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = NeonCyan
                )
            } else if (!isUnlocked) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = Disabled,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun BusinessDetailSheet(
    generator: Generator,
    genIndex: Int,
    coins: Double,
    viewModel: GameViewModel
) {
    val uiState = viewModel.uiState.collectAsState().value
    val assignedWorkers = uiState.ownedWorkers.filter { it.assignedGeneratorId == generator.id }
    val maxCap = generator.maxWorkerCapacity

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 32.dp)
    ) {
        // Title
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = generator.emoji, fontSize = 32.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(generator.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(generator.description, fontSize = 13.sp, color = TextMuted)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Production info
        GlassCard(
            modifier = Modifier.fillMaxWidth(), cornerRadius = 12.dp,
            glassColor = NeonCyan.copy(alpha = 0.08f), borderColor = NeonCyan.copy(alpha = 0.15f)
        ) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("Production", fontSize = 11.sp, color = TextMuted)
                    Text("${GameState.fmt(generator.productionPerSecond)}/s", fontSize = 18.sp,
                        fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = NeonCyan)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Owned", fontSize = 11.sp, color = TextMuted)
                    Text("x${generator.owned}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = NeonGreen)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // === WORKERS SECTION ===
        Text("👷 Workers (${assignedWorkers.size}/$maxCap)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(modifier = Modifier.height(6.dp))

        if (assignedWorkers.isEmpty()) {
            Text("No workers assigned. Hire workers from the Workers menu and assign them here.",
                fontSize = 12.sp, color = TextMuted)
        } else {
            assignedWorkers.forEach { w ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                ) {
                    Text(w.workerClass.emoji, fontSize = 16.sp)
                    Spacer(Modifier.width(6.dp))
                    Text(w.name, fontSize = 12.sp, color = TextPrimary, modifier = Modifier.weight(1f), maxLines = 1)
                    Text(w.role.name, fontSize = 10.sp, color = when (w.rarity) {
                        Worker.WorkerRarity.LEGENDARY -> Color(0xFFFFA726)
                        Worker.WorkerRarity.EPIC -> Color(0xFFAB47BC)
                        Worker.WorkerRarity.RARE -> Color(0xFF42A5F5)
                        else -> TextMuted
                    })
                    Spacer(Modifier.width(6.dp))
                    Text(w.mood.emoji, fontSize = 12.sp)
                    Text(" ${w.productivity}%", fontSize = 10.sp, color = NeonGreen)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // === BUSINESS PROPERTIES ===
        Text("⚙️ Business Properties", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(modifier = Modifier.height(10.dp))

        // Branches
        val canOpenBranch = generator.canOpenBranch()
        PropertyUpgradeRow(
            icon = "🏢", label = "Branches",
            currentValue = "${generator.branches}",
            bonusText = "+${((generator.branchBonus - 1.0) * 100).toInt()}% prod",
            cost = if (canOpenBranch) generator.branchCost else 0.0,
            coins = coins, enabled = generator.owned > 0 && canOpenBranch,
            maxedOut = !canOpenBranch,
            onUpgrade = { viewModel.openBranch(genIndex) }
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Local size
        PropertyUpgradeRow(
            icon = "📐", label = "Local Size",
            currentValue = "${generator.localSizeM2} m² (cap: $maxCap)",
            bonusText = "+${((generator.sizeBonus - 1.0) * 100).toInt()}% prod",
            cost = generator.sizeUpgradeCost, coins = coins,
            enabled = generator.owned > 0,
            onUpgrade = { viewModel.upgradeSize(genIndex) }
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Location tier
        val canUpgradeLoc = generator.canUpgradeLocation()
        PropertyUpgradeRow(
            icon = "📍", label = "Location",
            currentValue = generator.locationTierName,
            bonusText = "+${((generator.locationBonus - 1.0) * 100).toInt()}% prod",
            cost = if (canUpgradeLoc) generator.locationUpgradeCost else 0.0,
            coins = coins, enabled = generator.owned > 0 && canUpgradeLoc,
            maxedOut = !canUpgradeLoc,
            onUpgrade = { viewModel.upgradeLocation(genIndex) }
        )
    }
}

@Composable
fun PropertyUpgradeRow(
    icon: String,
    label: String,
    currentValue: String,
    bonusText: String,
    cost: Double,
    coins: Double,
    enabled: Boolean,
    maxedOut: Boolean = false,
    onUpgrade: () -> Unit
) {
    val canAfford = coins >= cost && enabled && !maxedOut

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 10.dp,
        glassColor = if (canAfford) NeonGreen.copy(alpha = 0.05f) else GlassWhite,
        borderColor = GlassBorder
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = icon, fontSize = 22.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Row {
                    Text(
                        text = currentValue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = bonusText,
                        fontSize = 11.sp,
                        color = NeonGreen
                    )
                }
            }

            if (maxedOut) {
                Text(
                    text = "MAX",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CoinGold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(CoinGold.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            } else {
                RepeatingButton(
                    onClick = onUpgrade,
                    enabled = canAfford,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (canAfford) NeonPurple else Disabled,
                        contentColor = if (canAfford) Color.White else TextMuted
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = "⬆ ${GameState.fmt(cost)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
