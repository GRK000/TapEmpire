package com.example.juego.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.juego.GameState
import com.example.juego.Pet
import com.example.juego.R
import com.example.juego.ui.components.AnimatedBackground
import com.example.juego.ui.components.GlassCard
import com.example.juego.ui.components.getPetArchetype
import com.example.juego.ui.theme.*
import com.example.juego.ui.viewmodel.GameUiState
import com.example.juego.ui.viewmodel.GameViewModel
import kotlin.math.*

@Composable
fun NurseryScreen(
    viewModel: GameViewModel,
    uiState: GameUiState,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf(
        "🐾 ${stringResource(R.string.nursery_tab_care)}",
        "🧬 ${stringResource(R.string.nursery_tab_breeding)}",
        "📚 ${stringResource(R.string.nursery_tab_collection)}"
    )

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedBackground(worldTheme = uiState.currentWorld?.theme)

        Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
            Text(
                text = stringResource(R.string.nursery_title),
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary,
                modifier = Modifier.padding(24.dp, 16.dp, 24.dp, 4.dp)
            )

            // Welfare bar
            NurseryWelfareBar(
                welfare = uiState.nurseryWelfareLevel,
                workerCount = uiState.nurseryWorkerCount,
                workersNeeded = uiState.nurseryWorkersNeeded
            )

            // Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = TextPrimary,
                edgePadding = 16.dp,
                divider = {}
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp,
                                color = if (selectedTab == index) NeonCyan else TextMuted
                            )
                        }
                    )
                }
            }

            when (selectedTab) {
                0 -> NurseryCareTab(viewModel, uiState)
                1 -> NurseryBreedingTab(viewModel, uiState)
                2 -> NurseryCollectionTab(uiState)
            }
        }
    }
}

// ======================== WELFARE BAR ========================

@Composable
fun NurseryWelfareBar(welfare: Double, workerCount: Int, workersNeeded: Int) {
    val welfareColor = when {
        welfare >= 70 -> NeonGreen
        welfare >= 40 -> Warning
        else -> Error
    }
    GlassCard(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        glassColor = welfareColor.copy(alpha = 0.08f),
        borderColor = welfareColor.copy(alpha = 0.2f),
        cornerRadius = 12.dp
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🏠", fontSize = 14.sp)
                Spacer(Modifier.width(6.dp))
                Text(
                    stringResource(R.string.nursery_welfare),
                    fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary
                )
                Spacer(Modifier.weight(1f))
                Text(
                    "${welfare.toInt()}%",
                    fontSize = 14.sp, fontWeight = FontWeight.Black, color = welfareColor
                )
            }
            Spacer(Modifier.height(4.dp))
            Box(
                modifier = Modifier.fillMaxWidth().height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Disabled.copy(alpha = 0.2f))
            ) {
                Box(
                    modifier = Modifier.fillMaxHeight()
                        .fillMaxWidth((welfare / 100.0).toFloat().coerceIn(0f, 1f))
                        .clip(RoundedCornerShape(3.dp))
                        .background(welfareColor)
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "👷 ${stringResource(R.string.nursery_workers_assigned)}: $workerCount / $workersNeeded",
                fontSize = 10.sp, color = if (workerCount >= workersNeeded) NeonGreen else Warning
            )
        }
    }
}

// ======================== CARE TAB ========================

@Composable
fun NurseryCareTab(viewModel: GameViewModel, uiState: GameUiState) {
    val ownedPets = uiState.pets.mapIndexedNotNull { index, pet ->
        if (pet.isOwned) index to pet else null
    }

    if (ownedPets.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🐾", fontSize = 60.sp)
                Spacer(Modifier.height(16.dp))
                Text(stringResource(R.string.nursery_empty), fontSize = 16.sp, color = TextMuted, fontWeight = FontWeight.Medium)
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(ownedPets) { _, (globalIndex, pet) ->
                NurseryPetCard(
                    pet = pet, globalIndex = globalIndex,
                    isActive = pet == uiState.activePet,
                    coins = uiState.coins, viewModel = viewModel
                )
            }
        }
    }
}

// ======================== BREEDING TAB ========================

@Composable
fun NurseryBreedingTab(viewModel: GameViewModel, uiState: GameUiState) {
    val breedablePets = uiState.pets.mapIndexedNotNull { index, pet ->
        if (pet.isOwned && pet.isAlive && !pet.isGhost) index to pet else null
    }
    var parent1Index by remember { mutableStateOf<Int?>(null) }
    var parent2Index by remember { mutableStateOf<Int?>(null) }
    var breedResult by remember { mutableStateOf<String?>(null) }

    val ownedCount = uiState.pets.count { it.isOwned }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Info card
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                glassColor = NeonPurple.copy(alpha = 0.08f),
                borderColor = NeonPurple.copy(alpha = 0.2f),
                cornerRadius = 16.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🧬 ${stringResource(R.string.breeding_title)}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = NeonCyan)
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.breeding_description), fontSize = 12.sp, color = TextSecondary, lineHeight = 16.sp)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        InfoChip("🐾 $ownedCount/${Pet.MAX_PETS}")
                        InfoChip("⏱️ 1h ${stringResource(R.string.breeding_cooldown)}")
                        InfoChip("🎲 ${(Pet.MUTATION_CHANCE * 100).toInt()}% ${stringResource(R.string.breeding_mutation)}")
                    }
                }
            }
        }

        // Parent selection
        item {
            Text(
                stringResource(R.string.breeding_select_parents),
                fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        // Parent 1
        item {
            BreedingParentSelector(
                label = "${stringResource(R.string.breeding_parent)} 1",
                pets = breedablePets,
                selectedIndex = parent1Index,
                excludeIndex = parent2Index,
                onSelect = { parent1Index = it }
            )
        }

        // Parent 2
        item {
            BreedingParentSelector(
                label = "${stringResource(R.string.breeding_parent)} 2",
                pets = breedablePets,
                selectedIndex = parent2Index,
                excludeIndex = parent1Index,
                onSelect = { parent2Index = it }
            )
        }

        // Preview & Breed button
        item {
            val p1 = parent1Index?.let { idx -> uiState.pets.getOrNull(idx) }
            val p2 = parent2Index?.let { idx -> uiState.pets.getOrNull(idx) }

            if (p1 != null && p2 != null) {
                val cost = Pet.getBreedCost(p1, p2)
                val canAfford = uiState.coins >= cost
                val canBreed = p1.canBreed() && p2.canBreed() && ownedCount < Pet.MAX_PETS

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    glassColor = if (canBreed && canAfford) NeonGreen.copy(alpha = 0.08f) else Error.copy(alpha = 0.06f),
                    borderColor = if (canBreed && canAfford) NeonGreen.copy(alpha = 0.3f) else Error.copy(alpha = 0.2f),
                    cornerRadius = 16.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🧬 ${stringResource(R.string.breeding_preview)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
                        Spacer(Modifier.height(8.dp))

                        // Show parent traits
                        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                            TraitPreview(p1.type.emoji, p1.traits)
                            Text("❤️", fontSize = 20.sp)
                            TraitPreview(p2.type.emoji, p2.traits)
                        }

                        Spacer(Modifier.height(8.dp))
                        Text("💰 ${stringResource(R.string.breeding_cost)}: ${GameState.fmt(cost)}", fontSize = 13.sp, fontWeight = FontWeight.Bold,
                            color = if (canAfford) CoinGold else Error)

                        if (!p1.canBreed()) {
                            Text("⏱️ ${p1.customName}: ${p1.breedCooldownRemaining / 60}m ${stringResource(R.string.breeding_cooldown)}",
                                fontSize = 11.sp, color = Warning)
                        }
                        if (!p2.canBreed()) {
                            Text("⏱️ ${p2.customName}: ${p2.breedCooldownRemaining / 60}m ${stringResource(R.string.breeding_cooldown)}",
                                fontSize = 11.sp, color = Warning)
                        }
                        if (ownedCount >= Pet.MAX_PETS) {
                            Text("⚠️ ${stringResource(R.string.breeding_max_reached)}", fontSize = 11.sp, color = Error)
                        }

                        Spacer(Modifier.height(12.dp))

                        val traitsLabel = stringResource(R.string.breeding_traits)
                        val incubatingMsg = stringResource(R.string.breeding_incubating_msg)
                        Button(
                            onClick = {
                                val offspring = viewModel.breedPets(parent1Index!!, parent2Index!!)
                                breedResult = if (offspring != null) {
                                    val hours = offspring.incubationRemaining / 3600
                                    "🥚 ${offspring.customName}\n⏱️ $incubatingMsg: ${hours}h"
                                } else null
                                parent1Index = null
                                parent2Index = null
                            },
                            enabled = canBreed && canAfford,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, disabledContainerColor = Disabled),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("🧬 ${stringResource(R.string.breeding_breed_btn)}", fontWeight = FontWeight.Bold, color = DeepSpace)
                        }
                    }
                }
            }
        }

        // Breed result
        breedResult?.let { result ->
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    glassColor = CoinGold.copy(alpha = 0.1f),
                    borderColor = CoinGold.copy(alpha = 0.3f),
                    cornerRadius = 16.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🥚 ${stringResource(R.string.breeding_incubating_title)}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = CoinGold)
                        Spacer(Modifier.height(8.dp))
                        Text(result, fontSize = 14.sp, color = TextPrimary, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(8.dp))
                        TextButton(onClick = { breedResult = null }) {
                            Text(stringResource(R.string.btn_great), color = NeonCyan)
                        }
                    }
                }
            }
        }

        // === INCUBATING EGGS ===
        val incubatingPets = uiState.pets.mapIndexed { i, p -> i to p }.filter { it.second.isIncubating }
        if (incubatingPets.isNotEmpty()) {
            item {
                Text("🥚 ${stringResource(R.string.breeding_incubating_title)} (${incubatingPets.size})",
                    fontSize = 15.sp, fontWeight = FontWeight.Bold, color = CoinGold,
                    modifier = Modifier.padding(top = 8.dp))
            }
            incubatingPets.forEach { (globalIndex, egg) ->
                item {
                    val remaining = egg.incubationRemaining
                    val hours = remaining / 3600
                    val minutes = (remaining % 3600) / 60
                    val ready = egg.isReadyToHatch
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        glassColor = if (ready) NeonGreen.copy(alpha = 0.1f) else CoinGold.copy(alpha = 0.06f),
                        borderColor = if (ready) NeonGreen.copy(alpha = 0.3f) else CoinGold.copy(alpha = 0.15f),
                        cornerRadius = 14.dp
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(if (ready) "🐣" else "🥚", fontSize = 28.sp)
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(egg.customName ?: "???", fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold, color = TextPrimary)
                                    if (ready) {
                                        Text("✨ ${stringResource(R.string.breeding_ready_hatch)}!",
                                            fontSize = 12.sp, color = NeonGreen, fontWeight = FontWeight.Bold)
                                    } else {
                                        Text("⏱️ ${hours}h ${minutes}m ${stringResource(R.string.breeding_remaining)}",
                                            fontSize = 12.sp, color = CoinGold)
                                    }
                                    // Show traits preview (hidden until hatch)
                                    Text("🧬 G${egg.generation} • ${stringResource(R.string.breeding_traits_hidden)}",
                                        fontSize = 10.sp, color = TextMuted)
                                }
                                if (egg.hadMutation()) {
                                    Text("✨", fontSize = 20.sp, modifier = Modifier.padding(end = 4.dp))
                                }
                            }
                            // Hatch button - only when ready
                            if (ready) {
                                Spacer(Modifier.height(8.dp))
                                Button(
                                    onClick = { viewModel.hatchPet(globalIndex) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                                ) {
                                    Text("🐣 ${stringResource(R.string.breeding_hatch_btn)}", color = DeepSpace,
                                        fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BreedingParentSelector(
    label: String,
    pets: List<Pair<Int, Pet>>,
    selectedIndex: Int?,
    excludeIndex: Int?,
    onSelect: (Int?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedPet = selectedIndex?.let { idx -> pets.find { it.first == idx }?.second }

    GlassCard(
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
        glassColor = if (selectedPet != null) NeonCyan.copy(alpha = 0.06f) else GlassWhite,
        borderColor = if (selectedPet != null) NeonCyan.copy(alpha = 0.3f) else GlassBorder,
        cornerRadius = 14.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                Spacer(Modifier.weight(1f))
                if (selectedPet != null) {
                    Text("${selectedPet.type.emoji} ${selectedPet.customName ?: selectedPet.type.name}",
                        fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(Modifier.width(4.dp))
                    selectedPet.traits.forEach { Text(it.emoji, fontSize = 12.sp) }
                } else {
                    Text(stringResource(R.string.breeding_tap_to_select), fontSize = 13.sp, color = TextMuted)
                }
            }

            if (expanded) {
                Spacer(Modifier.height(8.dp))
                pets.filter { it.first != excludeIndex }.forEach { (idx, pet) ->
                    val canBreed = pet.canBreed()
                    Surface(
                        onClick = {
                            onSelect(idx)
                            expanded = false
                        },
                        enabled = canBreed,
                        shape = RoundedCornerShape(8.dp),
                        color = if (canBreed) NeonCyan.copy(alpha = 0.08f) else Disabled.copy(alpha = 0.05f),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(pet.type.emoji, fontSize = 18.sp)
                            Spacer(Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(pet.customName ?: pet.type.name, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                                    color = if (canBreed) TextPrimary else TextMuted)
                                Row {
                                    pet.traits.forEach { t ->
                                        Text("${t.emoji}${t.name} ", fontSize = 10.sp, color = if (t.rare) CoinGold else TextSecondary)
                                    }
                                }
                            }
                            if (!canBreed) {
                                Text("⏱️ ${pet.breedCooldownRemaining / 60}m", fontSize = 10.sp, color = Warning)
                            }
                            Text("G${pet.generation}", fontSize = 10.sp, color = NeonPurple)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TraitPreview(emoji: String, traits: List<Pet.PetTrait>) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 28.sp)
        traits.forEach { t ->
            Text("${t.emoji} ${t.name}", fontSize = 10.sp,
                color = if (t.rare) CoinGold else TextSecondary)
        }
    }
}

@Composable
fun InfoChip(text: String) {
    Text(
        text, fontSize = 10.sp, color = TextSecondary,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(GlassWhite)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

// ======================== COLLECTION TAB ========================

@Composable
fun NurseryCollectionTab(uiState: GameUiState) {
    val allTraits = Pet.PetTrait.values().toList()
    val ownedPets = uiState.pets.filter { it.isOwned }
    val discoveredTraits = ownedPets.flatMap { it.traits }.toSet()
    val discoveredCombos = ownedPets.map { pet ->
        pet.traits.sortedBy { it.ordinal }.joinToString("+") { it.name }
    }.filter { it.contains("+") }.toSet()

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Progress header
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                glassColor = NeonPurple.copy(alpha = 0.08f),
                borderColor = NeonPurple.copy(alpha = 0.2f),
                cornerRadius = 16.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("📚 ${stringResource(R.string.nursery_collection_title)}",
                        fontSize = 18.sp, fontWeight = FontWeight.Black, color = NeonCyan)
                    Spacer(Modifier.height(8.dp))
                    val progress = discoveredTraits.size.toFloat() / allTraits.size
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${discoveredTraits.size}/${allTraits.size}", fontSize = 14.sp,
                            fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier.weight(1f).height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Disabled.copy(alpha = 0.2f))
                        ) {
                            Box(
                                modifier = Modifier.fillMaxHeight()
                                    .fillMaxWidth(progress)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(NeonCyan)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text("${(progress * 100).toInt()}%", fontSize = 12.sp, color = NeonCyan, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Common traits
        item {
            Text("⬜ ${stringResource(R.string.nursery_common_traits)}",
                fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
        item {
            TraitGrid(allTraits.filter { !it.rare }, discoveredTraits)
        }

        // Rare traits
        item {
            Text("✨ ${stringResource(R.string.nursery_rare_traits)}",
                fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CoinGold)
        }
        item {
            TraitGrid(allTraits.filter { it.rare }, discoveredTraits)
        }

        // Discovered combos
        if (discoveredCombos.isNotEmpty()) {
            item {
                Text("🔗 ${stringResource(R.string.nursery_combos)} (${discoveredCombos.size})",
                    fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NeonPurple)
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    discoveredCombos.forEach { combo ->
                        Text("✅ $combo", fontSize = 12.sp, color = TextSecondary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(NeonGreen.copy(alpha = 0.06f))
                                .padding(8.dp, 4.dp)
                                .fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Breeding stats
        item {
            val hybridCount = ownedPets.count { it.isHybrid }
            val maxGen = ownedPets.maxOfOrNull { it.generation } ?: 0
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                glassColor = CoinGold.copy(alpha = 0.06f),
                borderColor = CoinGold.copy(alpha = 0.15f),
                cornerRadius = 14.dp
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("📊 ${stringResource(R.string.nursery_breeding_stats)}",
                        fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CoinGold)
                    Spacer(Modifier.height(6.dp))
                    Text("🧬 ${stringResource(R.string.nursery_hybrids)}: $hybridCount", fontSize = 12.sp, color = TextSecondary)
                    Text("🏆 ${stringResource(R.string.nursery_max_gen)}: G$maxGen", fontSize = 12.sp, color = TextSecondary)
                    Text("✨ ${stringResource(R.string.nursery_rare_found)}: ${discoveredTraits.count { it.rare }}/${allTraits.count { it.rare }}", fontSize = 12.sp, color = TextSecondary)
                }
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
fun TraitGrid(traits: List<Pet.PetTrait>, discovered: Set<Pet.PetTrait>) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        traits.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { trait ->
                    val isDiscovered = trait in discovered
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isDiscovered) {
                                if (trait.rare) CoinGold.copy(alpha = 0.1f) else NeonGreen.copy(alpha = 0.08f)
                            } else Disabled.copy(alpha = 0.06f))
                            .border(1.dp, if (isDiscovered) {
                                if (trait.rare) CoinGold.copy(alpha = 0.3f) else NeonGreen.copy(alpha = 0.2f)
                            } else Disabled.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                if (isDiscovered) trait.emoji else "❓",
                                fontSize = 18.sp
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    if (isDiscovered) trait.name else "???",
                                    fontSize = 12.sp, fontWeight = FontWeight.Bold,
                                    color = if (isDiscovered) TextPrimary else TextMuted
                                )
                                if (isDiscovered) {
                                    Text(
                                        "+${(trait.bonusValue * 100).toInt()}% ${trait.bonusType.name.lowercase().replace("_", " ")}",
                                        fontSize = 9.sp, color = NeonCyan
                                    )
                                }
                            }
                        }
                    }
                }
                // Fill empty slot
                if (row.size < 2) Spacer(Modifier.weight(1f))
            }
        }
    }
}

// ======================== PET CARD (CARE TAB) ========================

@Composable
fun NurseryPetCard(
    pet: Pet,
    globalIndex: Int,
    isActive: Boolean,
    coins: Double,
    viewModel: GameViewModel
) {
    val archetype = remember(pet.type) { getPetArchetype(pet.type) }

    val infiniteTransition = rememberInfiniteTransition(label = "nursery_${pet.type}")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing)
        ),
        label = "nursery_time_${pet.type}"
    )

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        glassColor = if (isActive) archetype.primaryColor.copy(alpha = 0.12f)
        else if (!pet.isAlive) Error.copy(alpha = 0.08f)
        else GlassWhite,
        borderColor = if (isActive) archetype.primaryColor.copy(alpha = 0.4f)
        else if (!pet.isAlive) Error.copy(alpha = 0.3f)
        else GlassBorder,
        cornerRadius = 18.dp
    ) {
        Column {
            // === Top: Pet illustration + Info ===
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Mini Canvas illustration
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    archetype.primaryColor.copy(alpha = 0.15f),
                                    DeepSpace.copy(alpha = 0.5f)
                                )
                            )
                        )
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val s = minOf(size.width, size.height) * 0.35f

                        // Glow
                        drawCircle(
                            archetype.primaryColor.copy(alpha = 0.2f + sin(time) * 0.08f),
                            s * 1.2f,
                            Offset(cx, cy)
                        )

                        if (pet.isAlive) {
                            // Pet emoji centered (we draw a colored orb as body silhouette)
                            drawCircle(archetype.primaryColor.copy(alpha = 0.35f), s * 0.55f, Offset(cx, cy))
                            drawCircle(archetype.secondaryColor.copy(alpha = 0.25f), s * 0.35f, Offset(cx, cy - s * 0.2f))
                            // Eye sparkles
                            val eyeA = 0.4f + sin(time * 2f) * 0.2f
                            drawCircle(Color.White.copy(alpha = eyeA), s * 0.06f, Offset(cx - s * 0.12f, cy - s * 0.15f))
                            drawCircle(Color.White.copy(alpha = eyeA), s * 0.06f, Offset(cx + s * 0.12f, cy - s * 0.15f))
                        } else {
                            // Dead - ghostly
                            drawCircle(Color.Gray.copy(alpha = 0.2f), s * 0.5f, Offset(cx, cy))
                        }

                        // Floating particles around
                        for (i in 0..4) {
                            val a = time + i * 1.25f
                            drawCircle(
                                archetype.secondaryColor.copy(alpha = 0.15f + sin(a * 2f) * 0.1f),
                                2.5f,
                                Offset(cx + cos(a) * s * 0.8f, cy + sin(a) * s * 0.8f)
                            )
                        }
                    }

                    // Emoji overlay
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (pet.isAlive) pet.type.emoji else if (pet.isGhost) "👻" else "💀",
                            fontSize = 32.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Pet info
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = pet.customName ?: pet.type.name,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                        if (isActive) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "✦ ACTIVA",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = archetype.primaryColor,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(archetype.primaryColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = "${pet.statusEmoji} ${pet.mood.description} • Lv.${pet.level}",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = pet.type.description,
                        fontSize = 11.sp,
                        color = TextMuted
                    )

                    // Traits display
                    if (pet.traits.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            pet.traits.forEach { trait ->
                                Text(
                                    "${trait.emoji}${trait.name}",
                                    fontSize = 9.sp,
                                    color = if (trait.rare) CoinGold else NeonCyan,
                                    fontWeight = if (trait.rare) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (trait.rare) CoinGold.copy(alpha = 0.1f) else NeonCyan.copy(alpha = 0.08f))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                            if (pet.isHybrid) {
                                Text("🧬G${pet.generation}", fontSize = 9.sp, color = NeonPurple,
                                    modifier = Modifier.clip(RoundedCornerShape(4.dp))
                                        .background(NeonPurple.copy(alpha = 0.08f))
                                        .padding(horizontal = 4.dp, vertical = 1.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // XP bar
                    val xpProgress = (pet.experience / pet.experienceToNextLevel).toFloat().coerceIn(0f, 1f)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⭐", fontSize = 10.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Disabled.copy(alpha = 0.3f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(xpProgress)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(CoinGold)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${GameState.fmt(pet.experience)}/${GameState.fmt(pet.experienceToNextLevel)}",
                            fontSize = 9.sp,
                            color = TextMuted
                        )
                    }
                }
            }

            if (pet.isAlive) {
                Spacer(modifier = Modifier.height(12.dp))

                // === Stat bars ===
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    NurseryStatBar("❤️", stringResource(R.string.nursery_health), pet.health, Error)
                    NurseryStatBar("😊", stringResource(R.string.nursery_happiness), pet.happiness, CoinGold)
                    NurseryStatBar("🍔", stringResource(R.string.nursery_hunger), 100 - pet.hunger, NeonGreen)
                    NurseryStatBar("⚡", stringResource(R.string.nursery_energy), pet.energy, NeonCyan)
                    NurseryStatBar("🧠", stringResource(R.string.nursery_mental), pet.mentalHealth, NeonPurple)
                    NurseryStatBar("🧼", stringResource(R.string.nursery_hygiene), pet.hygiene, PrestigeCyan)
                }

                // === Diseases ===
                if (pet.diseases.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "🏥 ${stringResource(R.string.nursery_diseases)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Error
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    pet.diseases.forEach { disease ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = "${disease.emoji} ${disease.name}",
                                fontSize = 12.sp,
                                color = Error,
                                modifier = Modifier.weight(1f)
                            )
                            val canAfford = coins >= disease.treatmentCost
                            Surface(
                                onClick = { viewModel.treatPetDisease(globalIndex, disease) },
                                shape = RoundedCornerShape(12.dp),
                                color = if (canAfford) NeonGreen else Disabled,
                                modifier = Modifier.height(26.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(horizontal = 10.dp)
                                ) {
                                    Text(
                                        text = "💊 ${GameState.fmt(disease.treatmentCost)}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (canAfford) DeepSpace else TextMuted
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // === Action buttons ===
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    NurseryActionBtn(
                        emoji = "🍖",
                        label = stringResource(R.string.nursery_feed),
                        onClick = { viewModel.feedPetAt(globalIndex) },
                        color = NeonGreen,
                        modifier = Modifier.weight(1f)
                    )
                    NurseryActionBtn(
                        emoji = "💕",
                        label = stringResource(R.string.nursery_pet),
                        enabled = pet.canPet(),
                        onClick = { viewModel.petPetAt(globalIndex) },
                        color = NeonPink,
                        modifier = Modifier.weight(1f)
                    )
                    NurseryActionBtn(
                        emoji = "🎮",
                        label = stringResource(R.string.nursery_play),
                        enabled = pet.canPlay(),
                        onClick = { viewModel.playPet(globalIndex) },
                        color = NeonCyan,
                        modifier = Modifier.weight(1f)
                    )
                    NurseryActionBtn(
                        emoji = "🧼",
                        label = stringResource(R.string.nursery_clean),
                        onClick = { viewModel.cleanPet(globalIndex) },
                        color = PrestigeCyan,
                        modifier = Modifier.weight(1f)
                    )
                    NurseryActionBtn(
                        emoji = "💤",
                        label = stringResource(R.string.nursery_rest),
                        onClick = { viewModel.restPet(globalIndex) },
                        color = NeonPurple,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                // Dead pet - revive option
                Spacer(modifier = Modifier.height(12.dp))
                val reviveCost = pet.reviveCost
                val canAfford = coins >= reviveCost
                Surface(
                    onClick = { viewModel.revivePet(globalIndex) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (canAfford) CoinGold else Disabled,
                    modifier = Modifier.fillMaxWidth().height(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "✨ ${stringResource(R.string.nursery_revive)} • ${GameState.fmt(reviveCost)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (canAfford) DeepSpace else TextMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NurseryStatBar(
    emoji: String,
    label: String,
    value: Int,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = emoji, fontSize = 12.sp)
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextMuted,
            modifier = Modifier.width(60.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Disabled.copy(alpha = 0.2f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(value / 100f)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        when {
                            value > 60 -> color
                            value > 30 -> Warning
                            else -> Error
                        }
                    )
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "$value%",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = when {
                value > 60 -> color
                value > 30 -> Warning
                else -> Error
            }
        )
    }
}

@Composable
fun NurseryActionBtn(
    emoji: String,
    label: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(10.dp),
        color = if (enabled) color.copy(alpha = 0.2f) else Disabled.copy(alpha = 0.1f),
        modifier = modifier.height(44.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(text = emoji, fontSize = 16.sp)
            Text(
                text = label,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = if (enabled) color else TextMuted
            )
        }
    }
}
