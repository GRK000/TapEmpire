package com.example.juego.ui.components

import androidx.compose.animation.core.*
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import com.example.juego.Pet
import com.example.juego.ui.theme.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Avatar vectorial único por especie, estilo chibi neón.
 *
 * Lore: las quimeras del Proyecto Quimera. Cuando dos especies se cruzan,
 * el híbrido es "la suma de ambos": cuerpo y color base del progenitor
 * dominante, rasgos (orejas, alas, cola, accesorio) del otro progenitor.
 */

enum class BodyShape { ROUND, TALL, LONG, BLOB }
enum class EarStyle { NONE, POINTED, FLOPPY, ROUND, LONG_UP, FIN }
enum class WingStyle { NONE, FEATHER, DRAGON }
enum class TailStyle { NONE, CURL, WAG, FLAME, TENTACLES, FISH }
enum class Accessory { NONE, VISOR, ANTENNA, HORNS, HALO, CROWN, THIRD_EYE, RINGS, UNICORN_HORN }

data class PetVisual(
    val bodyShape: BodyShape,
    val ear: EarStyle,
    val wing: WingStyle,
    val tail: TailStyle,
    val accessory: Accessory,
    val bodyColor: Color,
    val accentColor: Color,
    val eyeColor: Color
)

/** Cada una de las 18 especies tiene una combinación única de rasgos y paleta. */
fun visualFor(type: Pet.PetType): PetVisual = when (type) {
    Pet.PetType.ROBO_CAT -> PetVisual(
        BodyShape.ROUND, EarStyle.POINTED, WingStyle.NONE, TailStyle.CURL, Accessory.VISOR,
        Color(0xFF35C7E8), Color(0xFF9AF1FF), Color(0xFF003344))
    Pet.PetType.CYBER_DOG -> PetVisual(
        BodyShape.ROUND, EarStyle.FLOPPY, WingStyle.NONE, TailStyle.WAG, Accessory.ANTENNA,
        Color(0xFFFF9A3D), Color(0xFFFFD45E), Color(0xFF3A1F00))
    Pet.PetType.QUANTUM_BIRD -> PetVisual(
        BodyShape.TALL, EarStyle.NONE, WingStyle.FEATHER, TailStyle.NONE, Accessory.RINGS,
        Color(0xFFB678FF), Color(0xFFE2C5FF), Color(0xFF2A0050))
    Pet.PetType.NANO_BUNNY -> PetVisual(
        BodyShape.ROUND, EarStyle.LONG_UP, WingStyle.NONE, TailStyle.NONE, Accessory.NONE,
        Color(0xFF6CE890), Color(0xFFBBFFD0), Color(0xFF003D14))
    Pet.PetType.HOLO_DRAGON -> PetVisual(
        BodyShape.TALL, EarStyle.NONE, WingStyle.DRAGON, TailStyle.FLAME, Accessory.HORNS,
        Color(0xFFFF5A5A), Color(0xFFFFC44D), Color(0xFF400000))
    Pet.PetType.ASTRAL_PHOENIX -> PetVisual(
        BodyShape.TALL, EarStyle.NONE, WingStyle.FEATHER, TailStyle.FLAME, Accessory.CROWN,
        Color(0xFFFFB300), Color(0xFFFF6E40), Color(0xFF4A2300))
    Pet.PetType.COSMIC_UNICORN -> PetVisual(
        BodyShape.TALL, EarStyle.POINTED, WingStyle.NONE, TailStyle.CURL, Accessory.UNICORN_HORN,
        Color(0xFFD06CFF), Color(0xFFFF9AD5), Color(0xFF3A0055))
    Pet.PetType.INFINITY_WHALE -> PetVisual(
        BodyShape.LONG, EarStyle.NONE, WingStyle.NONE, TailStyle.FISH, Accessory.NONE,
        Color(0xFF3D8BFF), Color(0xFF8FD0FF), Color(0xFF001E4D))
    Pet.PetType.NEBULA_FOX -> PetVisual(
        BodyShape.ROUND, EarStyle.POINTED, WingStyle.NONE, TailStyle.CURL, Accessory.NONE,
        Color(0xFFFF6EC7), Color(0xFFB678FF), Color(0xFF44002E))
    Pet.PetType.VOID_SERPENT -> PetVisual(
        BodyShape.LONG, EarStyle.NONE, WingStyle.NONE, TailStyle.CURL, Accessory.THIRD_EYE,
        Color(0xFF2FE08A), Color(0xFF0E5C38), Color(0xFFD0FFE8))
    Pet.PetType.CHRONO_OWL -> PetVisual(
        BodyShape.ROUND, EarStyle.POINTED, WingStyle.FEATHER, TailStyle.NONE, Accessory.HALO,
        Color(0xFFE8B43D), Color(0xFF5ED5E8), Color(0xFF3A2600))
    Pet.PetType.STELLAR_JELLYFISH -> PetVisual(
        BodyShape.BLOB, EarStyle.NONE, WingStyle.NONE, TailStyle.TENTACLES, Accessory.NONE,
        Color(0xFFB678FF), Color(0xFFFF6EC7), Color(0xFFFFFFFF))
    Pet.PetType.TITAN_LEVIATHAN -> PetVisual(
        BodyShape.LONG, EarStyle.FIN, WingStyle.NONE, TailStyle.FISH, Accessory.HORNS,
        Color(0xFF2266DD), Color(0xFF5EE8E8), Color(0xFFCCF2FF))
    Pet.PetType.ETHEREAL_GRIFFIN -> PetVisual(
        BodyShape.TALL, EarStyle.POINTED, WingStyle.FEATHER, TailStyle.WAG, Accessory.CROWN,
        Color(0xFFF2CC4D), Color(0xFFFFF2B0), Color(0xFF4A3300))
    Pet.PetType.DIMENSIONAL_KRAKEN -> PetVisual(
        BodyShape.BLOB, EarStyle.NONE, WingStyle.NONE, TailStyle.TENTACLES, Accessory.THIRD_EYE,
        Color(0xFF7A2FE0), Color(0xFFB678FF), Color(0xFFE8D5FF))
    Pet.PetType.PRIMORDIAL_HYDRA -> PetVisual(
        BodyShape.TALL, EarStyle.FIN, WingStyle.DRAGON, TailStyle.TENTACLES, Accessory.HORNS,
        Color(0xFF3DD66C), Color(0xFF35C7E8), Color(0xFF00330F))
    Pet.PetType.CELESTIAL_SPHINX -> PetVisual(
        BodyShape.ROUND, EarStyle.POINTED, WingStyle.FEATHER, TailStyle.CURL, Accessory.THIRD_EYE,
        Color(0xFFE8B43D), Color(0xFFB678FF), Color(0xFF2A0050))
    Pet.PetType.OMEGA_CHIMERA -> PetVisual(
        BodyShape.TALL, EarStyle.POINTED, WingStyle.DRAGON, TailStyle.FLAME, Accessory.CROWN,
        Color(0xFFFF5A5A), Color(0xFFFFB300), Color(0xFFFFE8CC))
}

/** Híbrido = suma de ambos: cuerpo/base del dominante, rasgos del otro progenitor. */
fun hybridVisual(dominant: PetVisual, other: PetVisual): PetVisual = PetVisual(
    bodyShape = dominant.bodyShape,
    ear = other.ear,
    wing = other.wing,
    tail = other.tail,
    accessory = other.accessory,
    bodyColor = dominant.bodyColor,
    accentColor = other.bodyColor,
    eyeColor = other.eyeColor
)

/**
 * Resuelve el aspecto de una mascota. Si es híbrida y sus padres existen en la
 * lista, combina los rasgos de ambos linajes.
 */
fun resolvePetVisual(pet: Pet, allPets: List<Pet>): PetVisual {
    val base = visualFor(pet.type)
    if (!pet.isHybrid) return base
    val p1 = allPets.find { it.petId == pet.parentId1 }
    val p2 = allPets.find { it.petId == pet.parentId2 }
    if (p1 == null || p2 == null) return base
    if (p1.type == p2.type) return base
    val dominant = if (p1.type == pet.type) p1 else p2
    val other = if (dominant === p1) p2 else p1
    return hybridVisual(visualFor(dominant.type), visualFor(other.type))
}

@Composable
fun PetAvatar(
    pet: Pet,
    allPets: List<Pet> = emptyList(),
    size: Dp,
    modifier: Modifier = Modifier
) {
    val visual = remember(pet.petId, pet.type, pet.isHybrid) { resolvePetVisual(pet, allPets) }
    val isGhost = pet.isGhost
    val isHybrid = pet.isHybrid

    val transition = rememberInfiniteTransition(label = "pet_avatar")
    val time by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(animation = tween(4000, easing = LinearEasing)),
        label = "pet_time"
    )

    Canvas(modifier = modifier.size(size)) {
        val s = this.size.minDimension
        val cx = this.size.width / 2f
        val cy = this.size.height / 2f
        val breathe = 1f + sin(time * 2f) * 0.025f
        drawPetFigure(visual, cx, cy, s * breathe, time, isGhost, isHybrid)
    }
}

private fun DrawScope.drawPetFigure(
    v: PetVisual,
    cx: Float,
    cy: Float,
    s: Float,
    time: Float,
    ghost: Boolean,
    hybrid: Boolean
) {
    val alpha = if (ghost) 0.45f else 1f
    val body = if (ghost) Color(0xFFCCEEFF).copy(alpha = alpha) else v.bodyColor.copy(alpha = alpha)
    val accent = if (ghost) Color(0xFFFFFFFF).copy(alpha = alpha * 0.7f) else v.accentColor.copy(alpha = alpha)
    val eye = if (ghost) Color(0xFF335577) else v.eyeColor

    // Dimensiones del cuerpo según silueta
    val bodyW: Float
    val bodyH: Float
    when (v.bodyShape) {
        BodyShape.ROUND -> { bodyW = s * 0.62f; bodyH = s * 0.58f }
        BodyShape.TALL -> { bodyW = s * 0.52f; bodyH = s * 0.68f }
        BodyShape.LONG -> { bodyW = s * 0.74f; bodyH = s * 0.46f }
        BodyShape.BLOB -> { bodyW = s * 0.64f; bodyH = s * 0.52f }
    }
    val bodyTop = cy - bodyH / 2f
    val headY = bodyTop + bodyH * 0.30f

    // === COLA (detrás del cuerpo) ===
    when (v.tail) {
        TailStyle.CURL -> {
            val p = Path().apply {
                moveTo(cx + bodyW * 0.38f, cy + bodyH * 0.18f)
                cubicTo(
                    cx + bodyW * 0.75f, cy + bodyH * 0.10f,
                    cx + bodyW * 0.82f, cy - bodyH * 0.35f + sin(time) * s * 0.02f,
                    cx + bodyW * 0.58f, cy - bodyH * 0.42f + sin(time) * s * 0.02f
                )
            }
            drawPath(p, accent, style = Stroke(width = s * 0.07f, cap = StrokeCap.Round))
        }
        TailStyle.WAG -> {
            val wag = sin(time * 4f) * 0.4f
            drawLine(
                accent,
                Offset(cx + bodyW * 0.40f, cy + bodyH * 0.10f),
                Offset(cx + bodyW * 0.68f + cos(wag) * s * 0.06f, cy - bodyH * 0.18f + sin(wag) * s * 0.08f),
                strokeWidth = s * 0.07f, cap = StrokeCap.Round
            )
        }
        TailStyle.FLAME -> {
            for (i in 0..2) {
                val fx = cx + bodyW * (0.46f + i * 0.10f)
                val fy = cy + bodyH * 0.05f - i * s * 0.05f + sin(time * 3f + i) * s * 0.02f
                drawCircle(accent.copy(alpha = alpha * (0.8f - i * 0.22f)), s * (0.07f - i * 0.015f), Offset(fx, fy))
            }
        }
        TailStyle.TENTACLES -> {
            for (i in 0..3) {
                val baseX = cx - bodyW * 0.28f + i * bodyW * 0.19f
                val sway = sin(time * 2f + i * 0.9f) * s * 0.04f
                val p = Path().apply {
                    moveTo(baseX, cy + bodyH * 0.34f)
                    quadraticTo(baseX + sway, cy + bodyH * 0.62f, baseX + sway * 1.6f, cy + bodyH * 0.80f)
                }
                drawPath(p, body, style = Stroke(width = s * 0.05f, cap = StrokeCap.Round))
            }
        }
        TailStyle.FISH -> {
            val sway = sin(time * 2.5f) * s * 0.03f
            val p = Path().apply {
                moveTo(cx - bodyW * 0.42f, cy)
                lineTo(cx - bodyW * 0.66f + sway, cy - bodyH * 0.28f)
                lineTo(cx - bodyW * 0.66f + sway, cy + bodyH * 0.28f)
                close()
            }
            drawPath(p, accent)
        }
        TailStyle.NONE -> {}
    }

    // === ALAS (detrás del cuerpo) ===
    when (v.wing) {
        WingStyle.FEATHER -> {
            val flap = sin(time * 2.5f) * s * 0.04f
            for (side in listOf(-1f, 1f)) {
                val p = Path().apply {
                    moveTo(cx + side * bodyW * 0.34f, cy - bodyH * 0.05f)
                    cubicTo(
                        cx + side * bodyW * 0.85f, cy - bodyH * 0.42f + flap,
                        cx + side * bodyW * 0.95f, cy + bodyH * 0.05f + flap,
                        cx + side * bodyW * 0.42f, cy + bodyH * 0.22f
                    )
                }
                drawPath(p, accent.copy(alpha = alpha * 0.85f))
            }
        }
        WingStyle.DRAGON -> {
            val flap = sin(time * 2f) * s * 0.05f
            for (side in listOf(-1f, 1f)) {
                val tipX = cx + side * bodyW * 0.92f
                val tipY = cy - bodyH * 0.50f + flap
                val p = Path().apply {
                    moveTo(cx + side * bodyW * 0.30f, cy - bodyH * 0.10f)
                    lineTo(tipX, tipY)
                    lineTo(cx + side * bodyW * 0.72f, cy + bodyH * 0.02f + flap * 0.5f)
                    lineTo(cx + side * bodyW * 0.40f, cy + bodyH * 0.16f)
                    close()
                }
                drawPath(p, accent.copy(alpha = alpha * 0.85f))
            }
        }
        WingStyle.NONE -> {}
    }

    // === OREJAS ===
    when (v.ear) {
        EarStyle.POINTED -> {
            for (side in listOf(-1f, 1f)) {
                val p = Path().apply {
                    moveTo(cx + side * bodyW * 0.30f, bodyTop + bodyH * 0.10f)
                    lineTo(cx + side * bodyW * 0.46f, bodyTop - bodyH * 0.22f)
                    lineTo(cx + side * bodyW * 0.10f, bodyTop + bodyH * 0.02f)
                    close()
                }
                drawPath(p, body)
            }
        }
        EarStyle.FLOPPY -> {
            for (side in listOf(-1f, 1f)) {
                drawOval(
                    accent,
                    Offset(cx + side * bodyW * 0.34f - bodyW * 0.12f, bodyTop - bodyH * 0.08f + sin(time + side) * s * 0.01f),
                    Size(bodyW * 0.24f, bodyH * 0.36f)
                )
            }
        }
        EarStyle.ROUND -> {
            for (side in listOf(-1f, 1f)) {
                drawCircle(body, bodyW * 0.14f, Offset(cx + side * bodyW * 0.32f, bodyTop - bodyH * 0.02f))
            }
        }
        EarStyle.LONG_UP -> {
            for (side in listOf(-1f, 1f)) {
                drawOval(
                    body,
                    Offset(cx + side * bodyW * 0.22f - bodyW * 0.09f, bodyTop - bodyH * 0.42f),
                    Size(bodyW * 0.18f, bodyH * 0.52f)
                )
                drawOval(
                    accent,
                    Offset(cx + side * bodyW * 0.22f - bodyW * 0.045f, bodyTop - bodyH * 0.34f),
                    Size(bodyW * 0.09f, bodyH * 0.36f)
                )
            }
        }
        EarStyle.FIN -> {
            val p = Path().apply {
                moveTo(cx - bodyW * 0.14f, bodyTop + bodyH * 0.04f)
                lineTo(cx, bodyTop - bodyH * 0.26f)
                lineTo(cx + bodyW * 0.14f, bodyTop + bodyH * 0.04f)
                close()
            }
            drawPath(p, accent)
        }
        EarStyle.NONE -> {}
    }

    // === CUERPO ===
    drawOval(
        Brush.verticalGradient(
            colors = listOf(body, body.copy(alpha = alpha * 0.8f)),
            startY = bodyTop, endY = bodyTop + bodyH
        ),
        Offset(cx - bodyW / 2f, bodyTop),
        Size(bodyW, bodyH)
    )
    // Barriga
    drawOval(
        accent.copy(alpha = alpha * 0.5f),
        Offset(cx - bodyW * 0.22f, cy + bodyH * 0.02f),
        Size(bodyW * 0.44f, bodyH * 0.38f)
    )

    // === OJOS (grandes, estilo chibi, con parpadeo) ===
    val blink = if (sin(time * 1.3f) > 0.97f) 0.15f else 1f
    val eyeR = s * 0.085f
    val eyeDX = bodyW * 0.20f
    for (side in listOf(-1f, 1f)) {
        val ex = cx + side * eyeDX
        // Esclerótica
        drawOval(
            Color.White.copy(alpha = alpha),
            Offset(ex - eyeR, headY - eyeR * blink),
            Size(eyeR * 2f, eyeR * 2f * blink)
        )
        if (blink > 0.5f) {
            // Iris + pupila + brillo
            drawCircle(eye.copy(alpha = alpha), eyeR * 0.62f, Offset(ex, headY))
            drawCircle(Color.White.copy(alpha = alpha * 0.9f), eyeR * 0.20f, Offset(ex - eyeR * 0.22f, headY - eyeR * 0.25f))
        }
    }
    // Mejillas
    for (side in listOf(-1f, 1f)) {
        drawCircle(
            Color(0xFFFF8FAB).copy(alpha = alpha * 0.4f),
            s * 0.045f,
            Offset(cx + side * bodyW * 0.32f, headY + s * 0.09f)
        )
    }
    // Boca (sonrisa mínima)
    drawArc(
        eye.copy(alpha = alpha * 0.8f),
        startAngle = 20f, sweepAngle = 140f, useCenter = false,
        topLeft = Offset(cx - s * 0.045f, headY + s * 0.06f),
        size = Size(s * 0.09f, s * 0.06f),
        style = Stroke(width = s * 0.015f, cap = StrokeCap.Round)
    )

    // === ACCESORIO ===
    when (v.accessory) {
        Accessory.VISOR -> {
            drawOval(
                accent.copy(alpha = alpha * 0.45f),
                Offset(cx - bodyW * 0.36f, headY - s * 0.10f),
                Size(bodyW * 0.72f, s * 0.13f)
            )
        }
        Accessory.ANTENNA -> {
            drawLine(accent, Offset(cx, bodyTop), Offset(cx, bodyTop - s * 0.14f), strokeWidth = s * 0.02f, cap = StrokeCap.Round)
            drawCircle(accent.copy(alpha = alpha * (0.6f + sin(time * 4f) * 0.4f)), s * 0.035f, Offset(cx, bodyTop - s * 0.16f))
        }
        Accessory.HORNS -> {
            for (side in listOf(-1f, 1f)) {
                drawLine(
                    accent,
                    Offset(cx + side * bodyW * 0.20f, bodyTop + bodyH * 0.04f),
                    Offset(cx + side * bodyW * 0.36f, bodyTop - bodyH * 0.18f),
                    strokeWidth = s * 0.04f, cap = StrokeCap.Round
                )
            }
        }
        Accessory.HALO -> {
            drawOval(
                accent.copy(alpha = alpha * (0.6f + sin(time * 2f) * 0.2f)),
                Offset(cx - bodyW * 0.26f, bodyTop - s * 0.13f),
                Size(bodyW * 0.52f, s * 0.07f),
                style = Stroke(width = s * 0.022f)
            )
        }
        Accessory.CROWN -> {
            val cw = bodyW * 0.34f
            val cyTop = bodyTop - s * 0.10f
            val p = Path().apply {
                moveTo(cx - cw / 2f, cyTop + s * 0.07f)
                lineTo(cx - cw / 2f, cyTop)
                lineTo(cx - cw * 0.25f, cyTop + s * 0.035f)
                lineTo(cx, cyTop - s * 0.02f)
                lineTo(cx + cw * 0.25f, cyTop + s * 0.035f)
                lineTo(cx + cw / 2f, cyTop)
                lineTo(cx + cw / 2f, cyTop + s * 0.07f)
                close()
            }
            drawPath(p, CoinGold.copy(alpha = alpha))
        }
        Accessory.THIRD_EYE -> {
            drawCircle(Color.White.copy(alpha = alpha), s * 0.045f, Offset(cx, headY - s * 0.115f))
            drawCircle(accent.copy(alpha = alpha), s * 0.028f, Offset(cx, headY - s * 0.115f))
        }
        Accessory.RINGS -> {
            drawOval(
                accent.copy(alpha = alpha * 0.5f),
                Offset(cx - bodyW * 0.62f, cy - s * 0.045f),
                Size(bodyW * 1.24f, s * 0.09f),
                style = Stroke(width = s * 0.018f)
            )
        }
        Accessory.UNICORN_HORN -> {
            val p = Path().apply {
                moveTo(cx - s * 0.035f, bodyTop + bodyH * 0.02f)
                lineTo(cx, bodyTop - s * 0.17f)
                lineTo(cx + s * 0.035f, bodyTop + bodyH * 0.02f)
                close()
            }
            drawPath(p, CoinGold.copy(alpha = alpha))
        }
        Accessory.NONE -> {}
    }

    // === MARCA DE HÍBRIDO: destellos de ADN orbitando ===
    if (hybrid) {
        for (i in 0..2) {
            val a = time + i * (2f * PI.toFloat() / 3f)
            val r = s * 0.46f
            drawCircle(
                CoinGold.copy(alpha = 0.5f + sin(time * 3f + i) * 0.3f),
                s * 0.02f,
                Offset(cx + cos(a) * r, cy + sin(a) * r * 0.7f)
            )
        }
    }
}
