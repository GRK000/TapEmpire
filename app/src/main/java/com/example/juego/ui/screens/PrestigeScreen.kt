package com.example.juego.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.juego.GameState
import com.example.juego.R
import com.example.juego.ui.components.AnimatedBackground
import com.example.juego.ui.components.GlassCard
import com.example.juego.ui.theme.*
import com.example.juego.ui.viewmodel.GameUiState
import com.example.juego.ui.viewmodel.GameViewModel

@Composable
fun PrestigeScreen(
    viewModel: GameViewModel,
    uiState: GameUiState,
    modifier: Modifier = Modifier
) {
    var showConfirm by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedBackground(worldTheme = uiState.currentWorld?.theme)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(24.dp)
        ) {
            Text(
                text = stringResource(R.string.prestige_title),
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Current prestige info
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                glassColor = PrestigeCyan.copy(alpha = 0.1f),
                borderColor = PrestigeCyan.copy(alpha = 0.3f),
                cornerRadius = 20.dp
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.prestige_current_level, uiState.prestigeLevel),
                        fontSize = 14.sp,
                        color = TextMuted
                    )
                    Text(
                        text = "${uiState.prestigeLevel}",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = PrestigeCyan
                    )
                    Text(
                        text = stringResource(R.string.prestige_multiplier, String.format("%.2f", uiState.prestigeMultiplier)),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = CoinGold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = stringResource(R.string.prestige_points, GameState.fmt(uiState.prestigePoints)),
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Prestige action
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 16.dp
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.prestige_subtitle),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (uiState.canPrestige) {
                        val points = viewModel.getPrestigePointsAvailable()
                        Text(
                            text = stringResource(R.string.prestige_available, GameState.fmt(points)),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonGreen
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { showConfirm = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrestigeCyan
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.prestige_btn),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    } else {
                        Text(
                            text = stringResource(R.string.prestige_not_available),
                            fontSize = 13.sp,
                            color = Error,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Benefits info
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 12.dp,
                glassColor = NeonPurple.copy(alpha = 0.08f),
                borderColor = NeonPurple.copy(alpha = 0.2f)
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.prestige_benefits_title),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonPurple
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    listOf(
                        stringResource(R.string.prestige_benefit_1),
                        stringResource(R.string.prestige_benefit_2),
                        stringResource(R.string.prestige_benefit_3),
                        stringResource(R.string.prestige_benefit_4)
                    ).forEach {
                        Text(text = it, fontSize = 13.sp, color = TextSecondary)
                    }
                }
            }
        }
    }

    // Confirmation dialog
    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = {
                Text(stringResource(R.string.prestige_confirm_title), fontWeight = FontWeight.Bold)
            },
            text = {
                Text(stringResource(R.string.prestige_confirm_text), color = TextSecondary)
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.performPrestige()
                        showConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrestigeCyan)
                ) {
                    Text(stringResource(R.string.prestige_confirm_yes))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showConfirm = false }) {
                    Text(stringResource(R.string.btn_cancel), color = TextSecondary)
                }
            },
            containerColor = Onyx,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary
        )
    }
}
