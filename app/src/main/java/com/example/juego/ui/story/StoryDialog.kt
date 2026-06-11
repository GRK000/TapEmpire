package com.example.juego.ui.story

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.juego.R
import com.example.juego.ui.theme.*

/** Identidad visual de cada voz de la historia. */
data class SpeakerStyle(val emoji: String, val nameRes: Int, val color: Color)

@Composable
fun speakerStyle(speaker: Speaker): SpeakerStyle = when (speaker) {
    Speaker.NARRATOR -> SpeakerStyle("📜", R.string.story_speaker_narrator, TextMuted)
    Speaker.LIA -> SpeakerStyle("💠", R.string.story_speaker_lia, NeonCyan)
    Speaker.VEX -> SpeakerStyle("🎩", R.string.story_speaker_vex, NeonRed)
    Speaker.AURORA -> SpeakerStyle("✨", R.string.story_speaker_aurora, CoinGold)
}

/**
 * Diálogo de historia por páginas: retrato del personaje, texto y avance.
 * Hace a la vez de escena narrativa y de tutorial.
 */
@Composable
fun StoryDialog(
    chapter: StoryChapter,
    onFinished: () -> Unit
) {
    var pageIndex by remember(chapter.key) { mutableIntStateOf(0) }
    val page = chapter.pages[pageIndex]
    val style = speakerStyle(page.speaker)
    val isLast = pageIndex == chapter.pages.lastIndex

    Dialog(
        onDismissRequest = { /* la historia se cierra con sus botones */ },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Onyx,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Cabecera: número y título del capítulo
                Text(
                    text = stringResource(R.string.story_chapter_label, chapter.number),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CoinGold
                )
                Text(
                    text = stringResource(chapter.titleRes),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )

                Spacer(Modifier.height(14.dp))

                // Página animada
                AnimatedContent(
                    targetState = pageIndex,
                    transitionSpec = {
                        (slideInHorizontally { it / 3 } + fadeIn())
                            .togetherWith(slideOutHorizontally { -it / 3 } + fadeOut())
                    },
                    label = "story_page"
                ) { idx ->
                    val p = chapter.pages[idx]
                    val st = speakerStyle(p.speaker)
                    Column {
                        // Retrato + nombre
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(st.color.copy(alpha = 0.35f), st.color.copy(alpha = 0.08f))
                                        )
                                    )
                                    .border(1.5.dp, st.color.copy(alpha = 0.6f), CircleShape)
                            ) {
                                Text(st.emoji, fontSize = 22.sp)
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = stringResource(st.nameRes),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = st.color
                            )
                        }

                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = stringResource(p.textRes),
                            fontSize = 14.sp,
                            lineHeight = 21.sp,
                            color = TextSecondary,
                            modifier = Modifier
                                .heightIn(min = 90.dp, max = 240.dp)
                                .verticalScroll(rememberScrollState())
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Puntos de progreso
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    chapter.pages.forEachIndexed { i, _ ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .size(if (i == pageIndex) 8.dp else 6.dp)
                                .clip(CircleShape)
                                .background(if (i <= pageIndex) style.color else GlassWhite)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (!isLast) {
                        TextButton(onClick = onFinished) {
                            Text(
                                stringResource(R.string.story_skip),
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    Button(
                        onClick = {
                            if (isLast) onFinished() else pageIndex++
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLast) CoinGold else NeonCyan
                        )
                    ) {
                        Text(
                            text = stringResource(
                                if (isLast) R.string.story_finish else R.string.story_continue
                            ),
                            color = DeepSpace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
