package com.example.juego.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.juego.GameState
import com.example.juego.ui.theme.*

@Composable
fun CoinDisplay(
    coins: Double,
    perTap: Double,
    perSecond: Double,
    prestigeMultiplier: Double,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = "💰 ${GameState.fmt(coins)}",
            fontSize = 34.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            color = CoinGold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "👆 ${GameState.fmt(perTap)}/tap",
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                color = TextSecondary
            )
            Text(
                text = "⚙️ ${GameState.fmt(perSecond)}/s",
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                color = NeonCyan
            )
            if (prestigeMultiplier > 1.0) {
                Text(
                    text = "⭐ x${String.format("%.2f", prestigeMultiplier)}",
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    color = PrestigeCyan
                )
            }
        }
    }
}
