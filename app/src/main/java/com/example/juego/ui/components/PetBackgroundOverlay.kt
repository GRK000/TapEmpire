package com.example.juego.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.juego.Pet
import com.example.juego.ui.theme.*
import kotlin.math.*
import kotlin.random.Random

/**
 * Draws an animated pet illustration as a background overlay on the HomeScreen.
 * Each pet archetype has a unique visual drawn with Canvas.
 */
@Composable
fun PetBackgroundOverlay(
    pet: Pet?,
    modifier: Modifier = Modifier
) {
    if (pet == null || !pet.isOwned || !pet.isAlive) return

    val infiniteTransition = rememberInfiniteTransition(label = "pet_overlay")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing)
        ),
        label = "pet_time"
    )
    val breathe by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pet_breathe"
    )

    val archetype = getPetArchetype(pet.type)
    val particles = remember(pet.type) { List(20) { FloatingOrb(archetype.primaryColor) } }

    Canvas(modifier = modifier.fillMaxSize()) {
        val cx = size.width * 0.5f
        val cy = size.height * 0.42f
        val baseSize = minOf(size.width, size.height) * 0.28f

        // Aura glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    archetype.primaryColor.copy(alpha = 0.12f * breathe),
                    archetype.primaryColor.copy(alpha = 0.04f),
                    Color.Transparent
                ),
                center = Offset(cx, cy),
                radius = baseSize * 2.2f
            ),
            center = Offset(cx, cy),
            radius = baseSize * 2.2f
        )

        // Floating orbs / particles
        particles.forEachIndexed { i, orb ->
            val angle = time * orb.speed + orb.phase
            val orbX = cx + cos(angle) * orb.orbitRadius * baseSize / 100f
            val orbY = cy + sin(angle * 0.7f + orb.phase) * orb.orbitRadius * baseSize / 120f
            drawCircle(
                color = orb.color.copy(alpha = orb.alpha * (0.5f + sin(time + i) * 0.5f)),
                radius = orb.size,
                center = Offset(orbX, orbY)
            )
        }

        // Draw pet silhouette based on archetype
        when (archetype.shape) {
            PetShape.FELINE -> drawFelineSilhouette(cx, cy, baseSize * breathe, archetype, time)
            PetShape.CANINE -> drawCanineSilhouette(cx, cy, baseSize * breathe, archetype, time)
            PetShape.AVIAN -> drawAvianSilhouette(cx, cy, baseSize * breathe, archetype, time)
            PetShape.AQUATIC -> drawAquaticSilhouette(cx, cy, baseSize * breathe, archetype, time)
            PetShape.DRAGON -> drawDragonSilhouette(cx, cy, baseSize * breathe, archetype, time)
            PetShape.MYTHIC -> drawMythicSilhouette(cx, cy, baseSize * breathe, archetype, time)
        }

        // Inner sparkle ring
        for (i in 0 until 8) {
            val a = time * 0.5f + i * PI.toFloat() / 4f
            val r = baseSize * 1.3f + sin(time * 2f + i) * 10f
            drawCircle(
                color = archetype.secondaryColor.copy(alpha = 0.25f + sin(time + i * 0.8f) * 0.15f),
                radius = 3f + sin(time * 3f + i) * 2f,
                center = Offset(cx + cos(a) * r, cy + sin(a) * r)
            )
        }
    }
}

// === PET ARCHETYPES ===

enum class PetShape { FELINE, CANINE, AVIAN, AQUATIC, DRAGON, MYTHIC }

data class PetArchetype(
    val shape: PetShape,
    val primaryColor: Color,
    val secondaryColor: Color
)

fun getPetArchetype(type: Pet.PetType): PetArchetype = when (type) {
    Pet.PetType.ROBO_CAT -> PetArchetype(PetShape.FELINE, NeonCyan, Color(0xFF55DDFF))
    Pet.PetType.CYBER_DOG -> PetArchetype(PetShape.CANINE, NeonOrange, CoinGold)
    Pet.PetType.QUANTUM_BIRD -> PetArchetype(PetShape.AVIAN, NeonPurple, NeonPurpleLight)
    Pet.PetType.NANO_BUNNY -> PetArchetype(PetShape.FELINE, NeonGreen, Color(0xFF88FFAA))
    Pet.PetType.HOLO_DRAGON -> PetArchetype(PetShape.DRAGON, NeonRed, CoinGold)
    Pet.PetType.ASTRAL_PHOENIX -> PetArchetype(PetShape.AVIAN, CoinGold, NeonOrange)
    Pet.PetType.COSMIC_UNICORN -> PetArchetype(PetShape.MYTHIC, GemPurple, NeonPink)
    Pet.PetType.INFINITY_WHALE -> PetArchetype(PetShape.AQUATIC, PrestigeCyan, NeonCyan)
    Pet.PetType.NEBULA_FOX -> PetArchetype(PetShape.CANINE, NeonPink, GemPurple)
    Pet.PetType.VOID_SERPENT -> PetArchetype(PetShape.AQUATIC, NeonGreen, Color(0xFF33FF99))
    Pet.PetType.CHRONO_OWL -> PetArchetype(PetShape.AVIAN, CoinGold, NeonCyan)
    Pet.PetType.STELLAR_JELLYFISH -> PetArchetype(PetShape.AQUATIC, GemPurple, NeonPink)
    Pet.PetType.TITAN_LEVIATHAN -> PetArchetype(PetShape.AQUATIC, Color(0xFF0077FF), NeonCyan)
    Pet.PetType.ETHEREAL_GRIFFIN -> PetArchetype(PetShape.MYTHIC, CoinGold, Color(0xFFFFEE88))
    Pet.PetType.DIMENSIONAL_KRAKEN -> PetArchetype(PetShape.AQUATIC, Color(0xFF6622CC), NeonPurple)
    Pet.PetType.PRIMORDIAL_HYDRA -> PetArchetype(PetShape.DRAGON, NeonGreen, NeonCyan)
    Pet.PetType.CELESTIAL_SPHINX -> PetArchetype(PetShape.FELINE, CoinGold, GemPurple)
    Pet.PetType.OMEGA_CHIMERA -> PetArchetype(PetShape.MYTHIC, NeonRed, CoinGold)
}

// === SHAPE DRAWING FUNCTIONS ===

private fun DrawScope.drawFelineSilhouette(cx: Float, cy: Float, size: Float, arch: PetArchetype, time: Float) {
    val c = arch.primaryColor.copy(alpha = 0.2f)
    val s = arch.secondaryColor.copy(alpha = 0.15f)
    // Body
    drawCircle(c, size * 0.45f, Offset(cx, cy + size * 0.1f))
    // Head
    drawCircle(c, size * 0.3f, Offset(cx, cy - size * 0.35f))
    // Ears (triangles)
    val earY = cy - size * 0.6f
    drawCircle(s, size * 0.12f, Offset(cx - size * 0.22f, earY))
    drawCircle(s, size * 0.12f, Offset(cx + size * 0.22f, earY))
    // Eyes
    val eyeGlow = 0.3f + sin(time * 2f) * 0.15f
    drawCircle(arch.secondaryColor.copy(alpha = eyeGlow), size * 0.06f, Offset(cx - size * 0.12f, cy - size * 0.35f))
    drawCircle(arch.secondaryColor.copy(alpha = eyeGlow), size * 0.06f, Offset(cx + size * 0.12f, cy - size * 0.35f))
    // Tail
    val tailPath = Path().apply {
        moveTo(cx + size * 0.35f, cy + size * 0.3f)
        cubicTo(cx + size * 0.7f, cy + size * 0.1f, cx + size * 0.8f, cy - size * 0.2f + sin(time) * size * 0.1f, cx + size * 0.6f, cy - size * 0.3f + sin(time) * size * 0.1f)
    }
    drawPath(tailPath, c, style = Stroke(width = size * 0.08f, cap = StrokeCap.Round))
}

private fun DrawScope.drawCanineSilhouette(cx: Float, cy: Float, size: Float, arch: PetArchetype, time: Float) {
    val c = arch.primaryColor.copy(alpha = 0.2f)
    val s = arch.secondaryColor.copy(alpha = 0.15f)
    // Body
    drawOval(c, Offset(cx - size * 0.4f, cy - size * 0.1f), androidx.compose.ui.geometry.Size(size * 0.8f, size * 0.55f))
    // Head
    drawCircle(c, size * 0.28f, Offset(cx, cy - size * 0.4f))
    // Snout
    drawOval(s, Offset(cx - size * 0.12f, cy - size * 0.38f), androidx.compose.ui.geometry.Size(size * 0.24f, size * 0.15f))
    // Ears (floppy)
    drawCircle(s, size * 0.14f, Offset(cx - size * 0.25f, cy - size * 0.55f + sin(time) * size * 0.03f))
    drawCircle(s, size * 0.14f, Offset(cx + size * 0.25f, cy - size * 0.55f + sin(time + 1f) * size * 0.03f))
    // Eyes
    val eyeGlow = 0.35f + sin(time * 2.5f) * 0.15f
    drawCircle(arch.secondaryColor.copy(alpha = eyeGlow), size * 0.05f, Offset(cx - size * 0.1f, cy - size * 0.42f))
    drawCircle(arch.secondaryColor.copy(alpha = eyeGlow), size * 0.05f, Offset(cx + size * 0.1f, cy - size * 0.42f))
    // Tail wag
    val tailAngle = sin(time * 3f) * 0.5f
    val tailEndX = cx + size * 0.5f + cos(tailAngle) * size * 0.2f
    val tailEndY = cy - size * 0.15f + sin(tailAngle) * size * 0.15f
    drawLine(c, Offset(cx + size * 0.35f, cy), Offset(tailEndX, tailEndY), strokeWidth = size * 0.07f, cap = StrokeCap.Round)
}

private fun DrawScope.drawAvianSilhouette(cx: Float, cy: Float, size: Float, arch: PetArchetype, time: Float) {
    val c = arch.primaryColor.copy(alpha = 0.2f)
    val s = arch.secondaryColor.copy(alpha = 0.15f)
    // Body
    drawOval(c, Offset(cx - size * 0.2f, cy - size * 0.15f), androidx.compose.ui.geometry.Size(size * 0.4f, size * 0.5f))
    // Head
    drawCircle(c, size * 0.2f, Offset(cx, cy - size * 0.4f))
    // Wings
    val wingFlap = sin(time * 2f) * size * 0.12f
    val leftWing = Path().apply {
        moveTo(cx - size * 0.15f, cy - size * 0.05f)
        cubicTo(cx - size * 0.7f, cy - size * 0.3f + wingFlap, cx - size * 0.8f, cy + size * 0.1f + wingFlap, cx - size * 0.3f, cy + size * 0.15f)
    }
    val rightWing = Path().apply {
        moveTo(cx + size * 0.15f, cy - size * 0.05f)
        cubicTo(cx + size * 0.7f, cy - size * 0.3f + wingFlap, cx + size * 0.8f, cy + size * 0.1f + wingFlap, cx + size * 0.3f, cy + size * 0.15f)
    }
    drawPath(leftWing, s, style = Stroke(width = size * 0.06f, cap = StrokeCap.Round))
    drawPath(rightWing, s, style = Stroke(width = size * 0.06f, cap = StrokeCap.Round))
    // Eye
    drawCircle(arch.secondaryColor.copy(alpha = 0.35f), size * 0.05f, Offset(cx, cy - size * 0.42f))
    // Beak
    drawCircle(CoinGold.copy(alpha = 0.25f), size * 0.06f, Offset(cx, cy - size * 0.32f))
}

private fun DrawScope.drawAquaticSilhouette(cx: Float, cy: Float, size: Float, arch: PetArchetype, time: Float) {
    val c = arch.primaryColor.copy(alpha = 0.18f)
    val s = arch.secondaryColor.copy(alpha = 0.12f)
    // Body
    val sway = sin(time) * size * 0.05f
    drawOval(c, Offset(cx - size * 0.45f + sway, cy - size * 0.2f), androidx.compose.ui.geometry.Size(size * 0.9f, size * 0.45f))
    // Tail fin
    val tailPath = Path().apply {
        moveTo(cx - size * 0.45f + sway, cy)
        lineTo(cx - size * 0.75f + sway + sin(time * 2.5f) * size * 0.08f, cy - size * 0.15f)
        lineTo(cx - size * 0.75f + sway + sin(time * 2.5f) * size * 0.08f, cy + size * 0.15f)
        close()
    }
    drawPath(tailPath, s)
    // Dorsal fin
    drawOval(s, Offset(cx - size * 0.1f + sway, cy - size * 0.35f), androidx.compose.ui.geometry.Size(size * 0.25f, size * 0.18f))
    // Eye
    drawCircle(arch.secondaryColor.copy(alpha = 0.4f), size * 0.06f, Offset(cx + size * 0.2f + sway, cy - size * 0.05f))
    // Bubbles
    for (i in 0..4) {
        val bx = cx + size * 0.4f + sin(time + i * 1.5f) * size * 0.1f
        val by = cy - size * 0.4f - (time * 10f + i * 30f) % (size * 0.8f)
        drawCircle(arch.secondaryColor.copy(alpha = 0.15f), size * 0.03f + i * 1f, Offset(bx, by))
    }
}

private fun DrawScope.drawDragonSilhouette(cx: Float, cy: Float, size: Float, arch: PetArchetype, time: Float) {
    val c = arch.primaryColor.copy(alpha = 0.2f)
    val s = arch.secondaryColor.copy(alpha = 0.15f)
    // Body
    drawOval(c, Offset(cx - size * 0.35f, cy - size * 0.1f), androidx.compose.ui.geometry.Size(size * 0.7f, size * 0.5f))
    // Neck + Head
    drawOval(c, Offset(cx - size * 0.15f, cy - size * 0.55f), androidx.compose.ui.geometry.Size(size * 0.3f, size * 0.35f))
    // Horns
    drawLine(s, Offset(cx - size * 0.1f, cy - size * 0.65f), Offset(cx - size * 0.2f, cy - size * 0.85f), strokeWidth = size * 0.04f, cap = StrokeCap.Round)
    drawLine(s, Offset(cx + size * 0.1f, cy - size * 0.65f), Offset(cx + size * 0.2f, cy - size * 0.85f), strokeWidth = size * 0.04f, cap = StrokeCap.Round)
    // Wings
    val wingFlap = sin(time * 1.5f) * size * 0.15f
    val leftWing = Path().apply {
        moveTo(cx - size * 0.2f, cy - size * 0.05f)
        cubicTo(cx - size * 0.8f, cy - size * 0.5f + wingFlap, cx - size * 1f, cy - size * 0.1f + wingFlap, cx - size * 0.4f, cy + size * 0.15f)
    }
    val rightWing = Path().apply {
        moveTo(cx + size * 0.2f, cy - size * 0.05f)
        cubicTo(cx + size * 0.8f, cy - size * 0.5f + wingFlap, cx + size * 1f, cy - size * 0.1f + wingFlap, cx + size * 0.4f, cy + size * 0.15f)
    }
    drawPath(leftWing, s, style = Stroke(width = size * 0.05f, cap = StrokeCap.Round))
    drawPath(rightWing, s, style = Stroke(width = size * 0.05f, cap = StrokeCap.Round))
    // Eyes
    val eyeGlow = 0.4f + sin(time * 3f) * 0.2f
    drawCircle(arch.secondaryColor.copy(alpha = eyeGlow), size * 0.06f, Offset(cx - size * 0.06f, cy - size * 0.48f))
    drawCircle(arch.secondaryColor.copy(alpha = eyeGlow), size * 0.06f, Offset(cx + size * 0.06f, cy - size * 0.48f))
    // Fire breath particles
    for (i in 0..3) {
        val fx = cx + size * 0.15f + i * size * 0.08f + sin(time * 4f + i) * size * 0.03f
        val fy = cy - size * 0.4f + sin(time * 3f + i * 0.5f) * size * 0.05f
        drawCircle(NeonOrange.copy(alpha = 0.2f - i * 0.04f), size * 0.04f - i * 1.5f, Offset(fx, fy))
    }
}

private fun DrawScope.drawMythicSilhouette(cx: Float, cy: Float, size: Float, arch: PetArchetype, time: Float) {
    val c = arch.primaryColor.copy(alpha = 0.18f)
    val s = arch.secondaryColor.copy(alpha = 0.12f)
    // Ethereal body (multiple overlapping circles for smoky effect)
    for (i in 0..5) {
        val ox = sin(time + i * 1.2f) * size * 0.05f
        val oy = cos(time * 0.8f + i * 0.9f) * size * 0.05f
        drawCircle(c, size * (0.35f - i * 0.03f), Offset(cx + ox, cy + oy + size * 0.05f))
    }
    // Crown / halo
    val haloRadius = size * 0.45f + sin(time * 2f) * size * 0.03f
    drawCircle(
        arch.secondaryColor.copy(alpha = 0.15f + sin(time) * 0.05f),
        haloRadius,
        Offset(cx, cy - size * 0.25f),
        style = Stroke(width = size * 0.03f)
    )
    // Mystical eyes (three for mythic creatures)
    val eyeAlpha = 0.35f + sin(time * 2f) * 0.15f
    drawCircle(arch.secondaryColor.copy(alpha = eyeAlpha), size * 0.05f, Offset(cx - size * 0.15f, cy - size * 0.2f))
    drawCircle(arch.secondaryColor.copy(alpha = eyeAlpha), size * 0.07f, Offset(cx, cy - size * 0.28f))
    drawCircle(arch.secondaryColor.copy(alpha = eyeAlpha), size * 0.05f, Offset(cx + size * 0.15f, cy - size * 0.2f))
    // Ethereal tendrils
    for (i in 0..5) {
        val angle = i * PI.toFloat() / 3f + time * 0.3f
        val endX = cx + cos(angle) * size * 0.8f
        val endY = cy + sin(angle) * size * 0.6f + size * 0.2f
        val ctrlX = cx + cos(angle + sin(time + i) * 0.3f) * size * 0.5f
        val ctrlY = cy + sin(angle + cos(time + i) * 0.3f) * size * 0.4f
        val path = Path().apply {
            moveTo(cx, cy + size * 0.1f)
            quadraticTo(ctrlX, ctrlY, endX, endY)
        }
        drawPath(path, s, style = Stroke(width = size * 0.02f + sin(time + i) * 1f, cap = StrokeCap.Round))
    }
}

// Floating orb data
private data class FloatingOrb(
    val color: Color,
    val orbitRadius: Float = 40f + Random.nextFloat() * 80f,
    val speed: Float = 0.3f + Random.nextFloat() * 0.7f,
    val phase: Float = Random.nextFloat() * (2 * PI).toFloat(),
    val size: Float = 2f + Random.nextFloat() * 5f,
    val alpha: Float = 0.1f + Random.nextFloat() * 0.25f
)
