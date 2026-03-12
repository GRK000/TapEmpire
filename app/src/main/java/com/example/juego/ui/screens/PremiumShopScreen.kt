package com.example.juego.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.juego.MonetizationManager
import com.example.juego.MonetizationManager.*
import com.example.juego.R
import com.example.juego.ui.components.AnimatedBackground
import com.example.juego.ui.components.GlassCard
import com.example.juego.ui.theme.*
import com.example.juego.ui.viewmodel.GameUiState
import com.example.juego.ui.viewmodel.GameViewModel
import kotlinx.coroutines.delay

@Composable
fun PremiumShopScreen(
    viewModel: GameViewModel,
    uiState: GameUiState,
    modifier: Modifier = Modifier
) {
    val monetization = uiState.monetizationState

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedBackground(worldTheme = uiState.currentWorld?.theme)

        LazyColumn(
            modifier = Modifier.fillMaxSize().systemBarsPadding(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            item {
                Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                    Text(
                        text = stringResource(R.string.premium_shop_title),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("💎", fontSize = 20.sp)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "${monetization.gems}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = GemPurple
                        )
                        Spacer(Modifier.width(16.dp))
                        if (monetization.vipTier != VipTier.NONE) {
                            Text(
                                text = "${monetization.vipTier.badge} VIP ${monetization.vipTier.name}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = CoinGold,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(CoinGold.copy(alpha = 0.12f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // === STARTER PACK (Phase 1) ===
            if (monetization.starterPackAvailable) {
                item {
                    StarterPackCard(
                        remainingMs = monetization.starterPackRemainingMs,
                        onBuy = { viewModel.purchasePack("starter_pack") }
                    )
                }
            }

            // === VIP SECTION (Phase 2) ===
            item {
                VipProgressCard(monetization = monetization, onClaimGems = { viewModel.claimDailyVipGems() })
            }

            // === ACTIVE BOOSTS ===
            if (monetization.boostActive) {
                item {
                    ActiveBoostCard(
                        multiplier = monetization.boostMultiplier,
                        remainingMs = monetization.boostRemainingMs
                    )
                }
            }

            // === GEM STORE (quick actions) ===
            item {
                Text(
                    text = stringResource(R.string.premium_gem_actions),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    item {
                        GemActionCard(
                            emoji = "⏱️",
                            title = stringResource(R.string.premium_auto_tap),
                            subtitle = stringResource(R.string.premium_auto_tap_desc),
                            cost = "50 💎",
                            enabled = monetization.gems >= 50,
                            onClick = { viewModel.buyAutoTap() }
                        )
                    }
                    item {
                        GemActionCard(
                            emoji = "💰",
                            title = stringResource(R.string.premium_instant_coins),
                            subtitle = stringResource(R.string.premium_instant_coins_desc),
                            cost = "100 💎",
                            enabled = monetization.gems >= 100,
                            onClick = { viewModel.buyInstantCoins() }
                        )
                    }
                    if (monetization.whaleUnlocked) {
                        item {
                            GemActionCard(
                                emoji = "🐋",
                                title = stringResource(R.string.premium_whale_boost),
                                subtitle = stringResource(R.string.premium_whale_boost_desc),
                                cost = "500 💎",
                                enabled = monetization.gems >= 500,
                                onClick = { viewModel.activateWhaleMultiplier() }
                            )
                        }
                    }
                }
            }

            // === PACKS ===
            item {
                Text(
                    text = stringResource(R.string.premium_packs),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            val filteredPacks: List<ShopPack> = monetization.availablePacks.filter { it.type != PackType.STARTER }
            items(filteredPacks.size) { index: Int ->
                val pack: ShopPack = filteredPacks[index]
                PackCard(
                    pack = pack,
                    vipTier = monetization.vipTier,
                    onBuy = { viewModel.purchasePack(pack.id) }
                )
            }

            // Spacer bottom
            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

// === STARTER PACK CARD ===
@Composable
fun StarterPackCard(remainingMs: Long, onBuy: () -> Unit) {
    var timeLeft by remember { mutableLongStateOf(remainingMs) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            timeLeft = (timeLeft - 1000).coerceAtLeast(0)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "starterGlow")
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse), label = "glow"
    )

    val hours = (timeLeft / 3600000).toInt()
    val minutes = ((timeLeft % 3600000) / 60000).toInt()
    val seconds = ((timeLeft % 60000) / 1000).toInt()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                2.dp,
                Brush.linearGradient(
                    listOf(CoinGold.copy(alpha = borderAlpha), NeonPink.copy(alpha = borderAlpha))
                ),
                RoundedCornerShape(18.dp)
            )
            .clickable { onBuy() },
        shape = RoundedCornerShape(18.dp),
        color = Onyx
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🚀", fontSize = 32.sp)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        stringResource(R.string.pack_starter_title),
                        fontSize = 18.sp, fontWeight = FontWeight.Black, color = CoinGold
                    )
                    Text(
                        stringResource(R.string.pack_starter_badge),
                        fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonPink
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Value items
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ValueChip("💎 200", stringResource(R.string.premium_gems_label))
                ValueChip("⚡ x10", stringResource(R.string.premium_boost_24h))
                ValueChip("💰", stringResource(R.string.premium_mega_coins))
            }

            Spacer(Modifier.height(12.dp))

            // Price with fake discount
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Text(
                    text = "$9.99",
                    fontSize = 14.sp,
                    color = TextMuted,
                    textDecoration = TextDecoration.LineThrough
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "$3.99",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = NeonGreen
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "-60%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepSpace,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(NeonGreen)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            // Timer
            Text(
                text = "⏰ ${String.format("%02d:%02d:%02d", hours, minutes, seconds)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Error
            )
        }
    }
}

@Composable
fun ValueChip(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Black, color = TextPrimary)
        Text(label, fontSize = 9.sp, color = TextMuted)
    }
}

// === VIP PROGRESS CARD ===
@Composable
fun VipProgressCard(monetization: MonetizationUiState, onClaimGems: () -> Unit) {
    val tier = monetization.vipTier
    val nextTier = monetization.nextVipTier
    val progress = monetization.vipProgress / 100f

    GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 16.dp) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (tier != VipTier.NONE) "${tier.badge} VIP ${tier.name}" else "⭐ VIP",
                    fontSize = 16.sp, fontWeight = FontWeight.Black,
                    color = if (tier != VipTier.NONE) CoinGold else TextSecondary
                )
                Spacer(Modifier.weight(1f))
                if (monetization.canClaimDailyGems) {
                    Surface(
                        onClick = onClaimGems,
                        shape = RoundedCornerShape(12.dp),
                        color = GemPurple
                    ) {
                        Text(
                            stringResource(R.string.premium_claim_gems, tier.dailyGemBonus),
                            fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Progress bar
            if (nextTier != null) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = CoinGold,
                    trackColor = Disabled.copy(alpha = 0.3f)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(R.string.premium_vip_next, nextTier.badge, nextTier.name,
                        MonetizationManager.formatPrice(monetization.centsToNextTier)),
                    fontSize = 10.sp, color = TextMuted
                )
            } else if (tier != VipTier.NONE) {
                Text(
                    stringResource(R.string.premium_vip_max),
                    fontSize = 12.sp, color = CoinGold, fontWeight = FontWeight.Bold
                )
            }

            // VIP perks summary
            if (tier != VipTier.NONE) {
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PerkChip("x${String.format("%.0f%%", (tier.productionMultiplier - 1) * 100)}", stringResource(R.string.premium_production))
                    PerkChip("+${tier.dailyGemBonus}💎", stringResource(R.string.premium_daily))
                }
            }
        }
    }
}

@Composable
fun PerkChip(value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(NeonPurple.copy(alpha = 0.1f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Black, color = NeonPurple)
        Text(label, fontSize = 8.sp, color = TextMuted)
    }
}

// === ACTIVE BOOST CARD ===
@Composable
fun ActiveBoostCard(multiplier: Double, remainingMs: Long) {
    var timeLeft by remember { mutableLongStateOf(remainingMs) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            timeLeft = (timeLeft - 1000).coerceAtLeast(0)
        }
    }
    val minutes = (timeLeft / 60000).toInt()
    val seconds = ((timeLeft % 60000) / 1000).toInt()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = NeonGreen.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("⚡", fontSize = 24.sp)
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.premium_boost_active, String.format("%.0fx", multiplier)),
                    fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NeonGreen
                )
                Text(
                    "${minutes}m ${seconds}s",
                    fontSize = 12.sp, color = TextMuted
                )
            }
        }
    }
}

// === GEM ACTION CARD ===
@Composable
fun GemActionCard(emoji: String, title: String, subtitle: String, cost: String, enabled: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .width(130.dp)
            .clickable(enabled = enabled) { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = if (enabled) Obsidian else Disabled.copy(alpha = 0.2f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 28.sp)
            Spacer(Modifier.height(4.dp))
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary, textAlign = TextAlign.Center)
            Text(subtitle, fontSize = 9.sp, color = TextMuted, textAlign = TextAlign.Center)
            Spacer(Modifier.height(6.dp))
            Text(
                cost, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GemPurple,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(GemPurple.copy(alpha = 0.1f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
    }
}

// === PACK CARD ===
@Composable
fun PackCard(pack: ShopPack, vipTier: VipTier, onBuy: () -> Unit) {
    val isWhale = pack.type == PackType.WHALE_EXCLUSIVE
    val borderColor = when (pack.type) {
        PackType.WHALE_EXCLUSIVE -> CoinGold
        PackType.VALUE_PACK -> NeonPink
        PackType.BOOST_PACK -> NeonGreen
        PackType.GEM_PACK -> GemPurple
        else -> GlassBorder
    }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isWhale) Modifier.border(1.dp, CoinGold.copy(alpha = 0.4f), RoundedCornerShape(14.dp)) else Modifier),
        cornerRadius = 14.dp,
        borderColor = borderColor.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(borderColor.copy(alpha = 0.12f))
            ) {
                Text(pack.emoji, fontSize = 24.sp)
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        getPackTitle(pack),
                        fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary
                    )
                    if (isWhale) {
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "🐋 EXCLUSIVE",
                            fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CoinGold,
                            modifier = Modifier.clip(RoundedCornerShape(4.dp))
                                .background(CoinGold.copy(alpha = 0.15f))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }
                Text(getPackDesc(pack), fontSize = 11.sp, color = TextMuted)

                // Content tags
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (pack.gems > 0) {
                        MiniTag("💎 ${pack.gems}", GemPurple)
                    }
                    if (pack.coinMultiplier > 1.0) {
                        MiniTag("⚡ x${String.format("%.0f", pack.coinMultiplier)}", NeonGreen)
                    }
                    if (pack.boostDurationMs > 0) {
                        val hours = (pack.boostDurationMs / 3600000).toInt()
                        val mins = ((pack.boostDurationMs % 3600000) / 60000).toInt()
                        val dur = if (hours > 0) "${hours}h" else "${mins}m"
                        MiniTag("⏱️ $dur", NeonCyan)
                    }
                }
            }

            Spacer(Modifier.width(8.dp))

            // Buy button
            Button(
                onClick = onBuy,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isWhale) CoinGold else NeonPurple
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    MonetizationManager.formatPrice(pack.priceCents),
                    fontSize = 13.sp, fontWeight = FontWeight.Black,
                    color = if (isWhale) DeepSpace else TextPrimary
                )
            }
        }
    }
}

@Composable
fun MiniTag(text: String, color: Color) {
    Text(
        text, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = color,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 5.dp, vertical = 1.dp)
    )
}

// === STRING HELPERS (compose-side) ===
@Composable
fun getPackTitle(pack: ShopPack): String {
    val map: Map<String, Int> = mapOf(
        "starter_pack" to R.string.pack_starter_title,
        "gems_small" to R.string.pack_gems_small_title,
        "gems_medium" to R.string.pack_gems_med_title,
        "gems_large" to R.string.pack_gems_large_title,
        "gems_mega" to R.string.pack_gems_mega_title,
        "boost_2x_1h" to R.string.pack_boost_2x_title,
        "boost_5x_1h" to R.string.pack_boost_5x_title,
        "boost_10x_30m" to R.string.pack_boost_10x_title,
        "value_silver" to R.string.pack_value_silver_title,
        "value_gold" to R.string.pack_value_gold_title,
        "whale_ultra" to R.string.pack_whale_ultra_title,
        "whale_omega" to R.string.pack_whale_omega_title
    )
    val resId = map[pack.id]
    return if (resId != null) stringResource(resId) else pack.id
}

@Composable
fun getPackDesc(pack: ShopPack): String {
    val map: Map<String, Int> = mapOf(
        "starter_pack" to R.string.pack_starter_desc,
        "gems_small" to R.string.pack_gems_small_desc,
        "gems_medium" to R.string.pack_gems_med_desc,
        "gems_large" to R.string.pack_gems_large_desc,
        "gems_mega" to R.string.pack_gems_mega_desc,
        "boost_2x_1h" to R.string.pack_boost_2x_desc,
        "boost_5x_1h" to R.string.pack_boost_5x_desc,
        "boost_10x_30m" to R.string.pack_boost_10x_desc,
        "value_silver" to R.string.pack_value_silver_desc,
        "value_gold" to R.string.pack_value_gold_desc,
        "whale_ultra" to R.string.pack_whale_ultra_desc,
        "whale_omega" to R.string.pack_whale_omega_desc
    )
    val resId = map[pack.id]
    return if (resId != null) stringResource(resId) else ""
}

// === DATA CLASS for UI ===
data class MonetizationUiState(
    val gems: Int = 0,
    val vipTier: VipTier = VipTier.NONE,
    val nextVipTier: VipTier? = null,
    val vipProgress: Int = 0,
    val centsToNextTier: Int = 0,
    val canClaimDailyGems: Boolean = false,
    val starterPackAvailable: Boolean = false,
    val starterPackRemainingMs: Long = 0,
    val whaleUnlocked: Boolean = false,
    val boostActive: Boolean = false,
    val boostMultiplier: Double = 1.0,
    val boostRemainingMs: Long = 0,
    val autoTapActive: Boolean = false,
    val availablePacks: List<ShopPack> = emptyList(),
    val totalSpentCents: Int = 0
)
