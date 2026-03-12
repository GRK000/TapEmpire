package com.example.juego.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.*
import com.example.juego.GameEvent
import com.example.juego.GameState
import com.example.juego.R
import com.example.juego.ui.components.AnimatedBackground
import com.example.juego.ui.components.GlassCard
import com.example.juego.ui.theme.*
import com.example.juego.ui.viewmodel.GameUiState
import com.example.juego.ui.viewmodel.GameViewModel
import java.util.Locale
import kotlinx.coroutines.delay

@Composable
fun EventsScreen(
    viewModel: GameViewModel,
    uiState: GameUiState,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        AnimatedBackground(worldTheme = uiState.currentWorld?.theme)

        LazyColumn(
            modifier = Modifier.fillMaxSize().systemBarsPadding(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            item {
                Text(
                    text = "⚡ ${stringResource(R.string.events_title)}",
                    fontSize = 28.sp, fontWeight = FontWeight.Black, color = TextPrimary,
                    modifier = Modifier.padding(8.dp, 16.dp, 8.dp, 4.dp)
                )
            }

            // Galactic Stability
            item { StabilityMeter(uiState.galacticStability) }

            // Active events
            val activeEvents: List<GameEvent> = uiState.activeGameEvents
            if (activeEvents.isNotEmpty()) {
                item {
                    Text("🔴 ${stringResource(R.string.events_active)} (${activeEvents.size})",
                        fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary,
                        modifier = Modifier.padding(top = 8.dp))
                }
                items(activeEvents, key = { it.id }) { event: GameEvent ->
                    ActiveEventCard(event, viewModel)
                }
            } else {
                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        glassColor = GlassWhite, borderColor = GlassBorder, cornerRadius = 16.dp
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🌌", fontSize = 40.sp)
                                Spacer(Modifier.height(8.dp))
                                Text(stringResource(R.string.events_none), fontSize = 14.sp,
                                    color = TextMuted, textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
            }

            // Event history
            val eventHistory: List<GameEvent> = uiState.eventHistory
            if (eventHistory.isNotEmpty()) {
                item {
                    Text("📜 ${stringResource(R.string.events_history)}",
                        fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextMuted,
                        modifier = Modifier.padding(top = 12.dp))
                }
                items(eventHistory.reversed().take(20)) { event: GameEvent ->
                    HistoryEventCard(event)
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun StabilityMeter(stability: Double) {
    val color = when {
        stability >= 70 -> NeonGreen
        stability >= 40 -> Warning
        else -> Error
    }
    val label = when {
        stability >= 85 -> stringResource(R.string.stability_golden)
        stability >= 70 -> stringResource(R.string.stability_stable)
        stability >= 40 -> stringResource(R.string.stability_unstable)
        stability >= 20 -> stringResource(R.string.stability_critical)
        else -> stringResource(R.string.stability_chaos)
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        glassColor = color.copy(alpha = 0.06f),
        borderColor = color.copy(alpha = 0.2f),
        cornerRadius = 16.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🌌", fontSize = 18.sp)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.stability_title), fontSize = 15.sp,
                    fontWeight = FontWeight.Black, color = TextPrimary)
                Spacer(Modifier.weight(1f))
                Text("${stability.toInt()}%", fontSize = 18.sp,
                    fontWeight = FontWeight.Black, color = color)
            }
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier.fillMaxWidth().height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Disabled.copy(alpha = 0.2f))
            ) {
                Box(
                    modifier = Modifier.fillMaxHeight()
                        .fillMaxWidth((stability / 100.0).toFloat().coerceIn(0f, 1f))
                        .clip(RoundedCornerShape(5.dp))
                        .background(color)
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun ActiveEventCard(event: GameEvent, viewModel: GameViewModel) {
    // Live countdown
    var remainingMs by remember(event.id) { mutableLongStateOf(event.remainingMs) }
    LaunchedEffect(event.id) {
        while (remainingMs > 0) {
            delay(1000)
            remainingMs = event.remainingMs
        }
    }
    val minutes = remainingMs / 60000
    val seconds = (remainingMs % 60000) / 1000

    val categoryColor = when (event.type.category) {
        GameEvent.EventCategory.GLOBAL -> Error
        GameEvent.EventCategory.WORLD_LOCAL -> NeonCyan
        GameEvent.EventCategory.CORPORATE -> CoinGold
    }
    val categoryLabel = when (event.type.category) {
        GameEvent.EventCategory.GLOBAL -> stringResource(R.string.event_cat_global)
        GameEvent.EventCategory.WORLD_LOCAL -> stringResource(R.string.event_cat_local)
        GameEvent.EventCategory.CORPORATE -> stringResource(R.string.event_cat_corporate)
    }

    val titleKey = "event_title_${event.type.name.lowercase()}"
    val descKey = "event_desc_${event.type.name.lowercase()}"
    val title = getEventString(titleKey)
    val desc = getEventString(descKey)

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        glassColor = categoryColor.copy(alpha = 0.08f),
        borderColor = categoryColor.copy(alpha = 0.3f),
        cornerRadius = 18.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(event.type.emoji, fontSize = 24.sp)
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontSize = 16.sp, fontWeight = FontWeight.Black, color = TextPrimary)
                    Text(categoryLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = categoryColor,
                        modifier = Modifier.clip(RoundedCornerShape(4.dp))
                            .background(categoryColor.copy(alpha = 0.1f))
                            .padding(horizontal = 6.dp, vertical = 1.dp))
                }
                // Timer
                Text("⏱️ $minutes:${String.format(Locale.US, "%02d", seconds)}", fontSize = 13.sp,
                    fontWeight = FontWeight.Bold, color = if (minutes < 2) Error else TextSecondary)
            }

            Spacer(Modifier.height(8.dp))
            Text(desc, fontSize = 12.sp, color = TextSecondary, lineHeight = 16.sp)

            // Active effects preview
            val baseEffects = event.baseEffects
            if (baseEffects.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    baseEffects.forEach { (key, value) ->
                        val (emoji, label) = getEffectDisplay(key, value)
                        Text("$emoji $label", fontSize = 10.sp,
                            color = if (value >= 0) NeonGreen else Error,
                            modifier = Modifier.clip(RoundedCornerShape(4.dp))
                                .background(if (value >= 0) NeonGreen.copy(alpha = 0.08f) else Error.copy(alpha = 0.06f))
                                .padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }

            // Choices
            if (event.needsChoice()) {
                Spacer(Modifier.height(12.dp))
                Text("⚡ ${stringResource(R.string.events_choose)}", fontSize = 13.sp,
                    fontWeight = FontWeight.Bold, color = NeonCyan)
                Spacer(Modifier.height(6.dp))
                event.choices.forEachIndexed { index, choice ->
                    val choiceLabel = getEventString(choice.labelKey)
                    Surface(
                        onClick = { viewModel.resolveEvent(event.id, index) },
                        shape = RoundedCornerShape(12.dp),
                        color = categoryColor.copy(alpha = 0.08f),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("${choice.emoji} $choiceLabel", fontSize = 13.sp,
                                fontWeight = FontWeight.Bold, color = TextPrimary)
                            Spacer(Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                choice.effects.forEach { (key, value) ->
                                    val (emoji, label) = getEffectDisplay(key, value)
                                    Text("$emoji$label", fontSize = 9.sp,
                                        color = if (value >= 0) NeonGreen else Error)
                                }
                            }
                        }
                    }
                }
            } else if (event.isResolved) {
                Spacer(Modifier.height(8.dp))
                val chosenLabel = event.choices.getOrNull(event.choiceIndex)?.let {
                    "${it.emoji} ${getEventString(it.labelKey)}"
                } ?: ""
                Text("✅ $chosenLabel", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeonGreen)
            }
        }
    }
}

@Composable
fun HistoryEventCard(event: GameEvent) {
    val title = getEventString("event_title_${event.type.name.lowercase()}")
    val desc = getEventString("event_desc_${event.type.name.lowercase()}")
    val chosen = event.choices.getOrNull(event.choiceIndex)
    var expanded by remember { mutableStateOf(false) }

    val categoryColor = when (event.type.category) {
        GameEvent.EventCategory.GLOBAL -> Error
        GameEvent.EventCategory.WORLD_LOCAL -> NeonCyan
        GameEvent.EventCategory.CORPORATE -> CoinGold
    }
    val categoryLabel = when (event.type.category) {
        GameEvent.EventCategory.GLOBAL -> stringResource(R.string.event_cat_global)
        GameEvent.EventCategory.WORLD_LOCAL -> stringResource(R.string.event_cat_local)
        GameEvent.EventCategory.CORPORATE -> stringResource(R.string.event_cat_corporate)
    }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        glassColor = if (expanded) categoryColor.copy(alpha = 0.06f) else GlassWhite,
        borderColor = if (expanded) categoryColor.copy(alpha = 0.2f) else GlassBorder,
        cornerRadius = 12.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(event.type.emoji, fontSize = 20.sp)
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(categoryLabel, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = categoryColor,
                        modifier = Modifier.clip(RoundedCornerShape(4.dp))
                            .background(categoryColor.copy(alpha = 0.1f))
                            .padding(horizontal = 5.dp, vertical = 1.dp))
                }
                if (chosen != null) {
                    Text(
                        if (event.isAutoResolved) "⚠️ →${chosen.emoji}" else "→ ${chosen.emoji}",
                        fontSize = 14.sp
                    )
                } else if (!event.isResolved && event.type.hasChoices) {
                    Text("⚠️", fontSize = 14.sp)
                }
                Text(if (expanded) "▲" else "▼", fontSize = 10.sp, color = TextMuted,
                    modifier = Modifier.padding(start = 6.dp))
            }

            // Expandable details
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    // Description
                    Text(desc, fontSize = 11.sp, color = TextSecondary, lineHeight = 15.sp)

                    // Base effects
                    if (event.baseEffects.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text("📊 ${stringResource(R.string.event_detail_effects)}", fontSize = 11.sp,
                            fontWeight = FontWeight.Bold, color = TextMuted)
                        Spacer(Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()) {
                            event.baseEffects.forEach { (key, value) ->
                                val (emoji, label) = getEffectDisplay(key, value)
                                Text("$emoji $label", fontSize = 9.sp,
                                    color = if (value >= 0) NeonGreen else Error,
                                    modifier = Modifier.clip(RoundedCornerShape(4.dp))
                                        .background(if (value >= 0) NeonGreen.copy(alpha = 0.08f) else Error.copy(alpha = 0.06f))
                                        .padding(horizontal = 5.dp, vertical = 2.dp))
                            }
                        }
                    }

                    // Choice made
                    if (chosen != null) {
                        Spacer(Modifier.height(8.dp))
                        if (event.isAutoResolved) {
                            // Auto-resolved: player didn't choose
                            Text("⚠️ ${stringResource(R.string.event_auto_resolved)}", fontSize = 11.sp,
                                fontWeight = FontWeight.Bold, color = Error)
                            Text("${chosen.emoji} ${getEventString(chosen.labelKey)}", fontSize = 12.sp,
                                fontWeight = FontWeight.Bold, color = Error.copy(alpha = 0.8f),
                                modifier = Modifier.padding(top = 2.dp))
                        } else {
                            Text("✅ ${stringResource(R.string.event_detail_choice)}", fontSize = 11.sp,
                                fontWeight = FontWeight.Bold, color = NeonGreen)
                            Text("${chosen.emoji} ${getEventString(chosen.labelKey)}", fontSize = 12.sp,
                                fontWeight = FontWeight.Bold, color = TextPrimary,
                                modifier = Modifier.padding(top = 2.dp))
                        }
                        if (chosen.effects.isNotEmpty()) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(top = 4.dp)) {
                                chosen.effects.forEach { (key, value) ->
                                    val (emoji, label) = getEffectDisplay(key, value)
                                    Text("$emoji$label", fontSize = 9.sp,
                                        color = if (value >= 0) NeonGreen else Error)
                                }
                            }
                        }
                    } else if (event.isResolved) {
                        Spacer(Modifier.height(8.dp))
                        Text("✅ ${stringResource(R.string.event_detail_resolved)}", fontSize = 11.sp,
                            color = NeonGreen)
                    }

                    // Duration
                    Spacer(Modifier.height(6.dp))
                    val durationMin = event.durationMs / 60000
                    Text("⏱️ ${stringResource(R.string.event_detail_duration)}: ${durationMin}min",
                        fontSize = 10.sp, color = TextMuted)
                }
            }
        }
    }
}

// === Helpers ===

@Composable
fun getEventString(key: String): String {
    // Map of event string keys to resource IDs
    val map = mapOf(
        // === TITLES (35) ===
        "event_title_global_economic_crisis" to R.string.event_title_crisis,
        "event_title_global_economic_boom" to R.string.event_title_boom,
        "event_title_legal_reform" to R.string.event_title_legal,
        "event_title_pandemic_outbreak" to R.string.event_title_pandemic,
        "event_title_golden_age" to R.string.event_title_golden,
        "event_title_alien_contact" to R.string.event_title_alien,
        "event_title_galactic_war" to R.string.event_title_war,
        "event_title_trade_agreement" to R.string.event_title_trade,
        "event_title_climate_disaster" to R.string.event_title_climate,
        "event_title_market_crash" to R.string.event_title_market_crash,
        "event_title_galactic_festival" to R.string.event_title_festival,
        "event_title_diplomatic_incident" to R.string.event_title_diplomatic,
        "event_title_dark_matter_surge" to R.string.event_title_dark_matter,
        "event_title_scientific_discovery" to R.string.event_title_science,
        "event_title_tech_breakthrough" to R.string.event_title_tech,
        "event_title_resource_shortage" to R.string.event_title_resource,
        "event_title_energy_blackout" to R.string.event_title_blackout,
        "event_title_terraforming_success" to R.string.event_title_terraforming,
        "event_title_space_debris" to R.string.event_title_debris,
        "event_title_cyber_attack" to R.string.event_title_cyber,
        "event_title_innovation_grant" to R.string.event_title_grant,
        "event_title_supply_chain_crisis" to R.string.event_title_supply,
        "event_title_mutant_outbreak" to R.string.event_title_mutant,
        "event_title_sector_strike" to R.string.event_title_strike,
        "event_title_competitor_invasion" to R.string.event_title_competitor,
        "event_title_union_movement" to R.string.event_title_union,
        "event_title_animal_welfare_scandal" to R.string.event_title_welfare,
        "event_title_company_of_year" to R.string.event_title_award,
        "event_title_corruption_scandal" to R.string.event_title_corruption,
        "event_title_tax_audit" to R.string.event_title_tax,
        "event_title_worker_training" to R.string.event_title_training,
        "event_title_espionage" to R.string.event_title_espionage,
        "event_title_labor_shortage" to R.string.event_title_labor,
        "event_title_pet_competition" to R.string.event_title_petcomp,
        "event_title_investor_interest" to R.string.event_title_investor,

        // === DESCRIPTIONS (35) ===
        "event_desc_global_economic_crisis" to R.string.event_desc_crisis,
        "event_desc_global_economic_boom" to R.string.event_desc_boom,
        "event_desc_legal_reform" to R.string.event_desc_legal,
        "event_desc_pandemic_outbreak" to R.string.event_desc_pandemic,
        "event_desc_golden_age" to R.string.event_desc_golden,
        "event_desc_alien_contact" to R.string.event_desc_alien,
        "event_desc_galactic_war" to R.string.event_desc_war,
        "event_desc_trade_agreement" to R.string.event_desc_trade,
        "event_desc_climate_disaster" to R.string.event_desc_climate,
        "event_desc_market_crash" to R.string.event_desc_market_crash,
        "event_desc_galactic_festival" to R.string.event_desc_festival,
        "event_desc_diplomatic_incident" to R.string.event_desc_diplomatic,
        "event_desc_dark_matter_surge" to R.string.event_desc_dark_matter,
        "event_desc_scientific_discovery" to R.string.event_desc_science,
        "event_desc_tech_breakthrough" to R.string.event_desc_tech,
        "event_desc_resource_shortage" to R.string.event_desc_resource,
        "event_desc_energy_blackout" to R.string.event_desc_blackout,
        "event_desc_terraforming_success" to R.string.event_desc_terraforming,
        "event_desc_space_debris" to R.string.event_desc_debris,
        "event_desc_cyber_attack" to R.string.event_desc_cyber,
        "event_desc_innovation_grant" to R.string.event_desc_grant,
        "event_desc_supply_chain_crisis" to R.string.event_desc_supply,
        "event_desc_mutant_outbreak" to R.string.event_desc_mutant,
        "event_desc_sector_strike" to R.string.event_desc_strike,
        "event_desc_competitor_invasion" to R.string.event_desc_competitor,
        "event_desc_union_movement" to R.string.event_desc_union,
        "event_desc_animal_welfare_scandal" to R.string.event_desc_welfare,
        "event_desc_company_of_year" to R.string.event_desc_award,
        "event_desc_corruption_scandal" to R.string.event_desc_corruption,
        "event_desc_tax_audit" to R.string.event_desc_tax,
        "event_desc_worker_training" to R.string.event_desc_training,
        "event_desc_espionage" to R.string.event_desc_espionage,
        "event_desc_labor_shortage" to R.string.event_desc_labor,
        "event_desc_pet_competition" to R.string.event_desc_petcomp,
        "event_desc_investor_interest" to R.string.event_desc_investor,

        // === CHOICE LABELS (original 15 events) ===
        "event_crisis_cut" to R.string.event_crisis_cut,
        "event_crisis_loan" to R.string.event_crisis_loan,
        "event_crisis_ignore" to R.string.event_crisis_ignore,
        "event_boom_invest" to R.string.event_boom_invest,
        "event_boom_save" to R.string.event_boom_save,
        "event_legal_comply" to R.string.event_legal_comply,
        "event_legal_lobby" to R.string.event_legal_lobby,
        "event_pandemic_quarantine" to R.string.event_pandemic_quarantine,
        "event_pandemic_ignore" to R.string.event_pandemic_ignore,
        "event_alien_diplomacy" to R.string.event_alien_diplomacy,
        "event_alien_exploit" to R.string.event_alien_exploit,
        "event_alien_ignore" to R.string.event_alien_ignore,
        "event_science_patent" to R.string.event_science_patent,
        "event_science_open" to R.string.event_science_open,
        "event_tech_implement" to R.string.event_tech_implement,
        "event_tech_sell" to R.string.event_tech_sell,
        "event_resource_import" to R.string.event_resource_import,
        "event_resource_ration" to R.string.event_resource_ration,
        "event_strike_negotiate" to R.string.event_strike_negotiate,
        "event_strike_replace" to R.string.event_strike_replace,
        "event_competitor_war" to R.string.event_competitor_war,
        "event_competitor_ally" to R.string.event_competitor_ally,
        "event_union_concede" to R.string.event_union_concede,
        "event_union_suppress" to R.string.event_union_suppress,
        "event_welfare_reform" to R.string.event_welfare_reform,
        "event_welfare_pr" to R.string.event_welfare_pr,
        "event_award_celebrate" to R.string.event_award_celebrate,
        "event_award_humble" to R.string.event_award_humble,
        "event_corrupt_investigate" to R.string.event_corrupt_investigate,
        "event_corrupt_cover" to R.string.event_corrupt_cover,

        // === CHOICE LABELS (new 20 events) ===
        "event_war_arms" to R.string.event_war_arms,
        "event_war_neutral" to R.string.event_war_neutral,
        "event_war_diplomacy" to R.string.event_war_diplomacy,
        "event_trade_accept" to R.string.event_trade_accept,
        "event_trade_renegotiate" to R.string.event_trade_renegotiate,
        "event_climate_rebuild" to R.string.event_climate_rebuild,
        "event_climate_relocate" to R.string.event_climate_relocate,
        "event_climate_adapt" to R.string.event_climate_adapt,
        "event_crash_buylow" to R.string.event_crash_buylow,
        "event_crash_liquidate" to R.string.event_crash_liquidate,
        "event_crash_wait" to R.string.event_crash_wait,
        "event_festival_sponsor" to R.string.event_festival_sponsor,
        "event_festival_ignore" to R.string.event_festival_ignore,
        "event_diplo_apologize" to R.string.event_diplo_apologize,
        "event_diplo_deny" to R.string.event_diplo_deny,
        "event_diplo_mediate" to R.string.event_diplo_mediate,
        "event_blackout_generators" to R.string.event_blackout_generators,
        "event_blackout_wait" to R.string.event_blackout_wait,
        "event_terra_expand" to R.string.event_terra_expand,
        "event_terra_sell" to R.string.event_terra_sell,
        "event_debris_clean" to R.string.event_debris_clean,
        "event_debris_shield" to R.string.event_debris_shield,
        "event_debris_ignore" to R.string.event_debris_ignore,
        "event_cyber_firewall" to R.string.event_cyber_firewall,
        "event_cyber_counter" to R.string.event_cyber_counter,
        "event_cyber_pay" to R.string.event_cyber_pay,
        "event_grant_research" to R.string.event_grant_research,
        "event_grant_infrastructure" to R.string.event_grant_infrastructure,
        "event_supply_local" to R.string.event_supply_local,
        "event_supply_stockpile" to R.string.event_supply_stockpile,
        "event_supply_wait" to R.string.event_supply_wait,
        "event_mutant_quarantine" to R.string.event_mutant_quarantine,
        "event_mutant_study" to R.string.event_mutant_study,
        "event_tax_comply" to R.string.event_tax_comply,
        "event_tax_offshore" to R.string.event_tax_offshore,
        "event_tax_negotiate" to R.string.event_tax_negotiate,
        "event_training_full" to R.string.event_training_full,
        "event_training_partial" to R.string.event_training_partial,
        "event_training_skip" to R.string.event_training_skip,
        "event_spy_counterintel" to R.string.event_spy_counterintel,
        "event_spy_doubleagent" to R.string.event_spy_doubleagent,
        "event_labor_raise" to R.string.event_labor_raise,
        "event_labor_automate" to R.string.event_labor_automate,
        "event_petcomp_enter" to R.string.event_petcomp_enter,
        "event_petcomp_host" to R.string.event_petcomp_host,
        "event_petcomp_skip" to R.string.event_petcomp_skip,
        "event_investor_accept" to R.string.event_investor_accept,
        "event_investor_decline" to R.string.event_investor_decline
    )
    val resId = map[key] ?: return key
    return stringResource(resId)
}

fun getEffectDisplay(key: String, value: Double): Pair<String, String> {
    val sign = if (value >= 0) "+" else ""
    return when (key) {
        GameEvent.EFF_PRODUCTION_MULT -> "📈" to "${sign}${(value * 100).toInt()}% prod"
        GameEvent.EFF_STRIKE_CHANCE -> "✊" to "${sign}${(value * 100).toInt()}% strike"
        GameEvent.EFF_CONTRACT_COST -> "📋" to "${sign}${(value * 100).toInt()}% cost"
        GameEvent.EFF_MUTATION_CHANCE -> "🧬" to "${sign}${(value * 100).toInt()}% mut"
        GameEvent.EFF_WELFARE -> "🐾" to "${sign}${value.toInt()} welfare"
        GameEvent.EFF_STABILITY -> "🌌" to "${sign}${value.toInt()} stab"
        GameEvent.EFF_MOOD_ALL -> "😊" to if (value > 0) "+mood" else "-mood"
        GameEvent.EFF_COINS_PERCENT -> "💰" to "${sign}${(value * 100).toInt()}% coins"
        GameEvent.EFF_COINS_FLAT -> "💰" to "${sign}${GameState.fmt(value)}"
        else -> "❓" to key
    }
}
