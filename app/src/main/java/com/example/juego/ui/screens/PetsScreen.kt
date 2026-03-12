package com.example.juego.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.juego.GameState
import com.example.juego.Pet
import com.example.juego.R
import com.example.juego.ui.components.AnimatedBackground
import com.example.juego.ui.components.GlassCard
import com.example.juego.ui.theme.*
import com.example.juego.ui.viewmodel.GameUiState
import com.example.juego.ui.viewmodel.GameViewModel

@Composable
fun PetsScreen(
    viewModel: GameViewModel,
    uiState: GameUiState,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        AnimatedBackground(worldTheme = uiState.currentWorld?.theme)

        Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
            Text(
                text = stringResource(R.string.pets_title),
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary,
                modifier = Modifier.padding(24.dp, 16.dp, 24.dp, 4.dp)
            )

            // Active pet info
            uiState.activePet?.let { pet ->
                if (pet.isOwned) {
                    ActivePetCard(pet = pet, viewModel = viewModel, coins = uiState.coins)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(uiState.pets) { index, pet ->
                    PetCard(
                        pet = pet,
                        isActive = pet == uiState.activePet,
                        coins = uiState.coins,
                        onBuy = { viewModel.buyPet(index) },
                        onSetActive = { viewModel.setActivePet(index) }
                    )
                }
            }
        }
    }
}

@Composable
fun ActivePetCard(
    pet: Pet,
    viewModel: GameViewModel,
    coins: Double
) {
    val bounce by rememberInfiniteTransition(label = "pet_active")
        .animateFloat(
            initialValue = 0f,
            targetValue = -6f,
            animationSpec = infiniteRepeatable(
                animation = tween(500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bounce"
        )

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        glassColor = NeonPink.copy(alpha = 0.1f),
        borderColor = NeonPink.copy(alpha = 0.3f),
        cornerRadius = 16.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (pet.isAlive) pet.type.emoji else "💀",
                fontSize = 40.sp,
                modifier = Modifier.offset(y = bounce.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pet.customName ?: pet.type.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "${pet.statusEmoji} ${pet.mood.description} • Lv.${pet.level}",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Stat bars
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatMini("❤️", pet.health, Error)
                    StatMini("😊", pet.happiness, CoinGold)
                    StatMini("🍔", 100 - pet.hunger, NeonGreen)
                    StatMini("⚡", pet.energy, NeonCyan)
                }

                if (pet.diseases.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "🏥 ${pet.diseases.joinToString { "${it.emoji}${it.name}" }}",
                        fontSize = 11.sp,
                        color = Error
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Actions
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            PetActionButton(
                text = stringResource(R.string.pets_feed),
                enabled = pet.isAlive,
                onClick = { viewModel.feedPet() },
                modifier = Modifier.weight(1f)
            )
            PetActionButton(
                text = stringResource(R.string.pets_pet),
                enabled = pet.canPet(),
                onClick = { viewModel.petPet() },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatMini(emoji: String, value: Int, color: androidx.compose.ui.graphics.Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = emoji, fontSize = 10.sp)
        Box(
            modifier = Modifier
                .width(24.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Disabled.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(value / 100f)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
        }
    }
}

@Composable
fun PetActionButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = NeonPurple.copy(alpha = 0.6f),
            disabledContainerColor = Disabled.copy(alpha = 0.2f)
        ),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        modifier = modifier.height(32.dp)
    ) {
        Text(text = text, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun PetCard(
    pet: Pet,
    isActive: Boolean,
    coins: Double,
    onBuy: () -> Unit,
    onSetActive: () -> Unit
) {
    val accentColor = if (pet.isOwned) NeonPink else Disabled

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (pet.isOwned && !isActive) Modifier.clickable { onSetActive() } else Modifier),
        glassColor = if (isActive) NeonPink.copy(alpha = 0.1f) else GlassWhite,
        borderColor = if (isActive) NeonPink.copy(alpha = 0.4f) else GlassBorder,
        cornerRadius = 14.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (pet.isOwned) pet.type.emoji else "❓",
                fontSize = 32.sp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = pet.type.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (pet.isOwned) TextPrimary else TextMuted
                    )
                    if (isActive) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.pets_active),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonPink,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(NeonPink.copy(alpha = 0.15f))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }
                Text(
                    text = pet.type.description,
                    fontSize = 11.sp,
                    color = TextMuted
                )
                Text(
                    text = stringResource(R.string.pets_bonus, pet.type.bonusType.name.lowercase().replace("_", " ")),
                    fontSize = 11.sp,
                    color = accentColor
                )
            }

            if (!pet.isOwned) {
                val canAfford = coins >= pet.purchaseCost
                Button(
                    onClick = onBuy,
                    enabled = canAfford,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (canAfford) NeonPink else Disabled
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(
                        text = GameState.fmt(pet.purchaseCost),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
