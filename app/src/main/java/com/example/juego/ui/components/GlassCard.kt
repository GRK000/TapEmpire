package com.example.juego.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.juego.ui.theme.GlassBorder
import com.example.juego.ui.theme.GlassWhite

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    glassColor: Color = GlassWhite,
    borderColor: Color = GlassBorder,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Column(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        glassColor,
                        glassColor.copy(alpha = glassColor.alpha * 0.5f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        borderColor,
                        borderColor.copy(alpha = borderColor.alpha * 0.3f)
                    )
                ),
                shape = shape
            )
            .padding(16.dp),
        content = content
    )
}

@Composable
fun GlassCardRow(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    glassColor: Color = GlassWhite,
    borderColor: Color = GlassBorder,
    content: @Composable RowScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Row(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        glassColor,
                        glassColor.copy(alpha = glassColor.alpha * 0.5f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        borderColor,
                        borderColor.copy(alpha = borderColor.alpha * 0.3f)
                    )
                ),
                shape = shape
            )
            .padding(16.dp),
        content = content
    )
}
