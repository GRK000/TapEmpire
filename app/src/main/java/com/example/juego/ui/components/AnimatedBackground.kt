package com.example.juego.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.juego.World
import com.example.juego.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun AnimatedBackground(
    worldTheme: World.WorldTheme?,
    modifier: Modifier = Modifier
) {
    val colors = getWorldColors(worldTheme)
    val infiniteTransition = rememberInfiniteTransition(label = "bg")

    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradient_offset"
    )

    val particles = remember { List(40) { BackgroundParticle() } }

    Box(modifier = modifier.fillMaxSize()) {
        // Animated gradient background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            colors.first.copy(alpha = 0.3f + offset * 0.15f),
                            DeepSpace,
                            Void,
                            colors.second.copy(alpha = 0.15f + (1f - offset) * 0.1f)
                        )
                    )
                )
        )

        // Floating particles
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val time = offset * Math.PI.toFloat() * 2f

            particles.forEachIndexed { i, p ->
                val x = ((p.baseX + sin(time * p.speed + p.phase) * p.amplitude) * w) % w
                val y = ((p.baseY + cos(time * p.speed * 0.7f + p.phase) * p.amplitude * 0.5f) * h) % h

                drawCircle(
                    color = colors.first.copy(alpha = p.alpha * (0.5f + offset * 0.5f)),
                    radius = p.size,
                    center = Offset(
                        if (x < 0) x + w else x,
                        if (y < 0) y + h else y
                    )
                )
            }
        }
    }
}

private data class BackgroundParticle(
    val baseX: Float = Random.nextFloat(),
    val baseY: Float = Random.nextFloat(),
    val speed: Float = 0.3f + Random.nextFloat() * 0.7f,
    val phase: Float = Random.nextFloat() * 6.28f,
    val amplitude: Float = 0.02f + Random.nextFloat() * 0.06f,
    val size: Float = 1f + Random.nextFloat() * 3f,
    val alpha: Float = 0.15f + Random.nextFloat() * 0.4f
)

fun getWorldColors(worldTheme: World.WorldTheme?): Pair<Color, Color> {
    return when (worldTheme) {
        World.WorldTheme.GARAGE -> GarageColor to GarageColorDark
        World.WorldTheme.SILICON_VALLEY -> SiliconColor to SiliconColorDark
        World.WorldTheme.TOKYO_TECH -> TokyoColor to TokyoColorDark
        World.WorldTheme.DUBAI_FUTURE -> DubaiColor to DubaiColorDark
        World.WorldTheme.MARS_COLONY -> MarsColor to MarsColorDark
        World.WorldTheme.QUANTUM_REALM -> QuantumColor to QuantumColorDark
        World.WorldTheme.CYBER_CITY -> CyberCityColor to CyberCityColorDark
        World.WorldTheme.ATLANTIS_DEEP -> AtlantisColor to AtlantisColorDark
        World.WorldTheme.VALHALLA_FORGE -> ValhallColor to ValhallColorDark
        World.WorldTheme.NEXUS_PRIME -> NexusColor to NexusColorDark
        else -> NeonPurple to NeonPurpleDark
    }
}
