package com.example.juego.ui.story

import androidx.annotation.StringRes
import com.example.juego.R

/**
 * «El Pacto de los Fundadores» — la historia de Tap Empire.
 *
 * Cada capítulo se dispara al desbloquear una mecánica y hace de tutorial
 * narrativo. Las claves coinciden con el sistema de tutoriales del ViewModel
 * (showTutorialOnce), por lo que cada capítulo se muestra una sola vez y queda
 * archivado en La Crónica.
 */

enum class Speaker { NARRATOR, LIA, VEX, AURORA }

data class StoryPage(
    val speaker: Speaker,
    @StringRes val textRes: Int
)

data class StoryChapter(
    val key: String,
    val number: Int,
    @StringRes val titleRes: Int,
    val pages: List<StoryPage>
)

val storyChapters: List<StoryChapter> = listOf(
    StoryChapter(
        key = "welcome", number = 1, titleRes = R.string.story_ch1_title,
        pages = listOf(
            StoryPage(Speaker.NARRATOR, R.string.story_ch1_p1),
            StoryPage(Speaker.LIA, R.string.story_ch1_p2),
            StoryPage(Speaker.LIA, R.string.story_ch1_p3),
            StoryPage(Speaker.LIA, R.string.story_ch1_p4)
        )
    ),
    StoryChapter(
        key = "first_generator", number = 2, titleRes = R.string.story_ch2_title,
        pages = listOf(
            StoryPage(Speaker.NARRATOR, R.string.story_ch2_p1),
            StoryPage(Speaker.VEX, R.string.story_ch2_p2),
            StoryPage(Speaker.LIA, R.string.story_ch2_p3)
        )
    ),
    StoryChapter(
        key = "worlds", number = 3, titleRes = R.string.story_ch3_title,
        pages = listOf(
            StoryPage(Speaker.LIA, R.string.story_ch3_p1),
            StoryPage(Speaker.VEX, R.string.story_ch3_p2),
            StoryPage(Speaker.LIA, R.string.story_ch3_p3)
        )
    ),
    StoryChapter(
        key = "minigames", number = 4, titleRes = R.string.story_ch4_title,
        pages = listOf(
            StoryPage(Speaker.NARRATOR, R.string.story_ch4_p1),
            StoryPage(Speaker.VEX, R.string.story_ch4_p2),
            StoryPage(Speaker.LIA, R.string.story_ch4_p3)
        )
    ),
    StoryChapter(
        key = "workers_unlocked", number = 5, titleRes = R.string.story_ch5_title,
        pages = listOf(
            StoryPage(Speaker.NARRATOR, R.string.story_ch5_p1),
            StoryPage(Speaker.LIA, R.string.story_ch5_p2),
            StoryPage(Speaker.LIA, R.string.story_ch5_p3)
        )
    ),
    StoryChapter(
        key = "pets", number = 6, titleRes = R.string.story_ch6_title,
        pages = listOf(
            StoryPage(Speaker.LIA, R.string.story_ch6_p1),
            StoryPage(Speaker.NARRATOR, R.string.story_ch6_p2),
            StoryPage(Speaker.LIA, R.string.story_ch6_p3)
        )
    ),
    StoryChapter(
        key = "first_breeding", number = 7, titleRes = R.string.story_ch7_title,
        pages = listOf(
            StoryPage(Speaker.LIA, R.string.story_ch7_p1),
            StoryPage(Speaker.LIA, R.string.story_ch7_p2),
            StoryPage(Speaker.VEX, R.string.story_ch7_p3)
        )
    ),
    StoryChapter(
        key = "prestige_available", number = 8, titleRes = R.string.story_ch8_title,
        pages = listOf(
            StoryPage(Speaker.NARRATOR, R.string.story_ch8_p1),
            StoryPage(Speaker.VEX, R.string.story_ch8_p2),
            StoryPage(Speaker.LIA, R.string.story_ch8_p3),
            StoryPage(Speaker.LIA, R.string.story_ch8_p4)
        )
    ),
    StoryChapter(
        key = "first_prestige", number = 9, titleRes = R.string.story_ch9_title,
        pages = listOf(
            StoryPage(Speaker.NARRATOR, R.string.story_ch9_p1),
            StoryPage(Speaker.AURORA, R.string.story_ch9_p2),
            StoryPage(Speaker.LIA, R.string.story_ch9_p3)
        )
    ),
    StoryChapter(
        key = "first_event", number = 10, titleRes = R.string.story_ch10_title,
        pages = listOf(
            StoryPage(Speaker.NARRATOR, R.string.story_ch10_p1),
            StoryPage(Speaker.LIA, R.string.story_ch10_p2),
            StoryPage(Speaker.LIA, R.string.story_ch10_p3)
        )
    ),
    StoryChapter(
        key = "comet_caught", number = 11, titleRes = R.string.story_ch11_title,
        pages = listOf(
            StoryPage(Speaker.NARRATOR, R.string.story_ch11_p1),
            StoryPage(Speaker.LIA, R.string.story_ch11_p2),
            StoryPage(Speaker.LIA, R.string.story_ch11_p3)
        )
    ),
    StoryChapter(
        key = "nexus_unlocked", number = 12, titleRes = R.string.story_ch12_title,
        pages = listOf(
            StoryPage(Speaker.NARRATOR, R.string.story_ch12_p1),
            StoryPage(Speaker.VEX, R.string.story_ch12_p2),
            StoryPage(Speaker.LIA, R.string.story_ch12_p3),
            StoryPage(Speaker.LIA, R.string.story_ch12_p4)
        )
    ),
    StoryChapter(
        key = "pact_won", number = 13, titleRes = R.string.story_ch13_title,
        pages = listOf(
            StoryPage(Speaker.NARRATOR, R.string.story_ch13_p1),
            StoryPage(Speaker.VEX, R.string.story_ch13_p2),
            StoryPage(Speaker.LIA, R.string.story_ch13_p3),
            StoryPage(Speaker.AURORA, R.string.story_ch13_p4),
            StoryPage(Speaker.VEX, R.string.story_ch13_p5),
            StoryPage(Speaker.NARRATOR, R.string.story_ch13_p6)
        )
    )
)

fun storyChapterFor(key: String): StoryChapter? = storyChapters.find { it.key == key }
