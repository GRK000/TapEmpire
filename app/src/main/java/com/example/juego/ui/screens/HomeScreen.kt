package com.example.juego.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.juego.ComboSystem
import com.example.juego.GameState
import com.example.juego.World
import com.example.juego.ui.components.*
import com.example.juego.ui.theme.*
import com.example.juego.ui.viewmodel.GameUiState
import com.example.juego.ui.viewmodel.GameViewModel
import com.example.juego.ui.viewmodel.TapFeedback
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun HomeScreen(
    viewModel: GameViewModel,
    uiState: GameUiState,
    modifier: Modifier = Modifier
) {
    val tapFeedbacks = remember { mutableStateListOf<TapFloater>() }

    val comboEffect = remember(uiState.comboCount) {
        when {
            uiState.comboCount >= 50 -> ComboSystem.ComboEffect.LEGENDARY
            uiState.comboCount >= 30 -> ComboSystem.ComboEffect.RAINBOW
            uiState.comboCount >= 15 -> ComboSystem.ComboEffect.LIGHTNING
            uiState.comboCount >= 5 -> ComboSystem.ComboEffect.FIRE
            uiState.comboCount > 1 -> ComboSystem.ComboEffect.BASIC
            else -> ComboSystem.ComboEffect.NONE
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Animated background per world
        AnimatedBackground(
            worldTheme = uiState.currentWorld?.theme
        )

        // Pet visual overlay (behind UI, shows pet illustration)
        PetBackgroundOverlay(pet = uiState.activePet)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp, bottom = 8.dp)
                .systemBarsPadding()
        ) {
            // World name
            val worldTheme = uiState.currentWorld?.theme
            if (worldTheme != null) {
                Text(
                    text = "${worldTheme.emoji} ${worldTheme.name}",
                    fontSize = 14.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Coin display
            CoinDisplay(
                coins = uiState.coins,
                perTap = uiState.perTap,
                perSecond = uiState.perSecond,
                prestigeMultiplier = uiState.prestigeMultiplier
            )

            // Monthly salary cost indicator
            if (uiState.totalMonthlySalary > 0) {
                Text(
                    text = "👷 -${GameState.fmt(uiState.totalMonthlySalary)}/mo",
                    fontSize = 11.sp,
                    color = Color(0xFFFF6B6B).copy(alpha = 0.8f)
                )
            }

            // Gems & boost indicator
            val monet = uiState.monetizationState
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (monet.gems > 0) {
                    Text(
                        text = "💎 ${monet.gems}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GemPurple
                    )
                }
                if (monet.boostActive) {
                    Text(
                        text = "⚡ x${String.format(java.util.Locale.US, "%.0f", monet.boostMultiplier)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonGreen
                    )
                }
                if (monet.vipTier != com.example.juego.MonetizationManager.VipTier.NONE) {
                    Text(
                        text = "${monet.vipTier.badge} VIP",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CoinGold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Event banner
            EventBanner(event = uiState.activeEvent)

            Spacer(modifier = Modifier.height(8.dp))

            // Combo bar
            ComboBar(
                comboCount = uiState.comboCount,
                comboMultiplier = uiState.comboMultiplier,
                comboActive = uiState.comboActive,
                comboTimePercent = uiState.comboTimePercent
            )

            Spacer(modifier = Modifier.weight(1f))

            // Floating tap indicators
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(250.dp)
            ) {
                tapFeedbacks.forEach { floater ->
                    key(floater.id) {
                        TapFloaterAnimation(floater) {
                            tapFeedbacks.removeAll { it.id == floater.id }
                        }
                    }
                }

                // TAP BUTTON
                TapButton(
                    comboEffect = comboEffect,
                    onTap = {
                        val feedback = viewModel.onTap()
                        tapFeedbacks.add(
                            TapFloater(
                                id = System.nanoTime(),
                                amount = feedback.amount,
                                isCritical = feedback.isCritical,
                                comboCount = feedback.comboCount,
                                offsetX = (Random.nextFloat() - 0.5f) * 120f,
                                offsetY = Random.nextFloat() * 20f
                            )
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Pet widget
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                PetWidget(pet = uiState.activePet)
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

data class TapFloater(
    val id: Long,
    val amount: Double,
    val isCritical: Boolean,
    val comboCount: Int,
    val offsetX: Float,
    val offsetY: Float
)

@Composable
fun TapFloaterAnimation(
    floater: TapFloater,
    onFinished: () -> Unit
) {
    var visible by remember { mutableStateOf(true) }

    LaunchedEffect(floater.id) {
        delay(700)
        visible = false
        delay(300)
        onFinished()
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(300),
        label = "floater_alpha"
    )

    val offsetY by animateFloatAsState(
        targetValue = if (visible) floater.offsetY else floater.offsetY - 120f,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "floater_y"
    )

    val text = if (floater.isCritical) {
        "💥 +${GameState.fmt(floater.amount)} 💥"
    } else if (floater.comboCount >= 10) {
        "🔥 +${GameState.fmt(floater.amount)}"
    } else {
        "+${GameState.fmt(floater.amount)}"
    }

    val color = when {
        floater.isCritical -> NeonRed
        floater.comboCount >= 10 -> ComboFire
        else -> CoinGold
    }

    val fontSize = when {
        floater.isCritical -> 24.sp
        floater.comboCount >= 10 -> 22.sp
        else -> 18.sp
    }

    Text(
        text = text,
        color = color.copy(alpha = alpha),
        fontSize = fontSize,
        fontWeight = FontWeight.Black,
        modifier = Modifier.offset(x = floater.offsetX.dp, y = offsetY.dp)
    )
}
