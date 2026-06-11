package com.example.juego.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.juego.R
import com.example.juego.ui.components.AnimatedBackground
import com.example.juego.ui.components.GlassCard
import com.example.juego.ui.story.StoryChapter
import com.example.juego.ui.story.StoryDialog
import com.example.juego.ui.story.storyChapters
import com.example.juego.ui.theme.*
import com.example.juego.ui.viewmodel.GameUiState
import com.example.juego.ui.viewmodel.GameViewModel

/**
 * La Crónica: códice donde se releen los capítulos de «El Pacto de los
 * Fundadores» ya vividos. Los capítulos futuros aparecen sellados.
 */
@Composable
fun StoryScreen(
    viewModel: GameViewModel,
    uiState: GameUiState,
    modifier: Modifier = Modifier
) {
    val unlockedKeys by viewModel.unlockedStoryKeys.collectAsState()
    var replaying by remember { mutableStateOf<StoryChapter?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedBackground(worldTheme = uiState.currentWorld?.theme)

        Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
            Text(
                text = stringResource(R.string.story_chronicle_title),
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary,
                modifier = Modifier.padding(24.dp, 16.dp, 24.dp, 2.dp)
            )
            Text(
                text = stringResource(R.string.story_chronicle_subtitle),
                fontSize = 13.sp,
                color = TextMuted,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(Modifier.height(12.dp))

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(storyChapters) { chapter ->
                    val unlocked = chapter.key in unlockedKeys
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (unlocked) Modifier.clickable { replaying = chapter }
                                else Modifier
                            ),
                        glassColor = if (unlocked) CoinGold.copy(alpha = 0.08f) else GlassWhite,
                        borderColor = if (unlocked) CoinGold.copy(alpha = 0.25f) else GlassBorder,
                        cornerRadius = 14.dp
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (unlocked) "📖" else "🔒",
                                fontSize = 26.sp
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.story_chapter_label, chapter.number),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (unlocked) CoinGold else TextMuted
                                )
                                Text(
                                    text = if (unlocked) stringResource(chapter.titleRes)
                                    else stringResource(R.string.story_chapter_locked),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (unlocked) TextPrimary else TextMuted
                                )
                                if (unlocked) {
                                    Text(
                                        text = stringResource(R.string.story_chapter_replay),
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        replaying?.let { chapter ->
            StoryDialog(chapter = chapter, onFinished = { replaying = null })
        }
    }
}
