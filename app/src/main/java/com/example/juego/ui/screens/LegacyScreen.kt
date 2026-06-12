package com.example.juego.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.juego.GameState
import com.example.juego.R
import com.example.juego.ui.components.AnimatedBackground
import com.example.juego.ui.components.GlassCard
import com.example.juego.ui.components.Haptics
import com.example.juego.ui.theme.*
import com.example.juego.ui.viewmodel.GameUiState
import com.example.juego.ui.viewmodel.GameViewModel

private data class LegacyUpgradeUi(
    val id: String,
    val emoji: String,
    val nameRes: Int,
    val descRes: Int
)

private val legacyUpgrades = listOf(
    LegacyUpgradeUi(GameState.LEGACY_ECHO, "🔱", R.string.legacy_echo_name, R.string.legacy_echo_desc),
    LegacyUpgradeUi(GameState.LEGACY_NETWORK, "🕸️", R.string.legacy_network_name, R.string.legacy_network_desc),
    LegacyUpgradeUi(GameState.LEGACY_LUCK, "🍀", R.string.legacy_luck_name, R.string.legacy_luck_desc),
    LegacyUpgradeUi(GameState.LEGACY_PACT, "📜", R.string.legacy_pact_name, R.string.legacy_pact_desc),
    LegacyUpgradeUi(GameState.LEGACY_SEED, "💰", R.string.legacy_seed_name, R.string.legacy_seed_desc),
    LegacyUpgradeUi(GameState.LEGACY_MAGNET, "☄️", R.string.legacy_magnet_name, R.string.legacy_magnet_desc)
)

/**
 * El Legado de Aurora: mejoras permanentes compradas con Renombre.
 * Sobreviven a la Cláusula del Fénix — el único poder que Vex no puede quemar.
 */
@Composable
fun LegacyScreen(
    viewModel: GameViewModel,
    uiState: GameUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedBackground(worldTheme = uiState.currentWorld?.theme)

        Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
            Text(
                text = stringResource(R.string.legacy_title),
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary,
                modifier = Modifier.padding(24.dp, 16.dp, 24.dp, 2.dp)
            )
            Text(
                text = stringResource(R.string.legacy_subtitle),
                fontSize = 13.sp,
                color = TextMuted,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(Modifier.height(10.dp))

            // Saldo de Renombre
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                cornerRadius = 12.dp,
                glassColor = PrestigeCyan.copy(alpha = 0.1f),
                borderColor = PrestigeCyan.copy(alpha = 0.25f)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        stringResource(R.string.legacy_renown_balance),
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "⭐ ${GameState.fmt(uiState.prestigePoints)}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = PrestigeCyan
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(legacyUpgrades) { upgrade ->
                    val level = uiState.legacyLevels[upgrade.id] ?: 0
                    val maxLevel = GameState.getLegacyMaxLevel(upgrade.id)
                    val maxed = level >= maxLevel
                    val cost = uiState.legacyCosts[upgrade.id] ?: 0.0
                    val canAfford = !maxed && uiState.prestigePoints >= cost

                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        glassColor = if (maxed) CoinGold.copy(alpha = 0.08f) else GlassWhite,
                        borderColor = if (maxed) CoinGold.copy(alpha = 0.3f) else GlassBorder,
                        cornerRadius = 14.dp
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        Brush.radialGradient(
                                            listOf(
                                                PrestigeCyan.copy(alpha = 0.25f),
                                                PrestigeCyan.copy(alpha = 0.05f)
                                            )
                                        )
                                    )
                            ) {
                                Text(upgrade.emoji, fontSize = 24.sp)
                            }

                            Spacer(Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = stringResource(upgrade.nameRes),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = stringResource(R.string.legacy_level, level, maxLevel),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (maxed) CoinGold else PrestigeCyan
                                    )
                                }
                                Text(
                                    text = stringResource(upgrade.descRes),
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                                Spacer(Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { level.toFloat() / maxLevel },
                                    color = if (maxed) CoinGold else PrestigeCyan,
                                    trackColor = GlassWhite,
                                    modifier = Modifier
                                        .fillMaxWidth(0.8f)
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                )
                            }

                            Spacer(Modifier.width(8.dp))

                            if (maxed) {
                                Text(
                                    text = stringResource(R.string.label_max),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = CoinGold
                                )
                            } else {
                                Button(
                                    onClick = {
                                        if (viewModel.buyLegacyUpgrade(upgrade.id)) {
                                            Haptics.medium(context)
                                        }
                                    },
                                    enabled = canAfford,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (canAfford) PrestigeCyan else Disabled,
                                        contentColor = DeepSpace
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text(
                                        text = "⭐ ${GameState.fmt(cost)}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
