package com.example.juego.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.annotation.StringRes
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.juego.GameEvent
import com.example.juego.GameState
import com.example.juego.R
import com.example.juego.ui.screens.*
import com.example.juego.ui.screens.minigames.*
import com.example.juego.ui.theme.*
import com.example.juego.ui.viewmodel.GameViewModel
import kotlinx.coroutines.delay

class ComposeMainActivity : ComponentActivity() {

    private val gameViewModel: GameViewModel by lazy {
        androidx.lifecycle.ViewModelProvider(this)[GameViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TapEmpireTheme {
                TapEmpireApp(gameViewModel = gameViewModel)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        gameViewModel.saveState()
    }
}

sealed class NavRoute(val route: String, @StringRes val titleRes: Int, val icon: ImageVector, val emoji: String) {
    object Home : NavRoute("home", R.string.nav_tap, Icons.Default.TouchApp, "💰")
    object Worlds : NavRoute("worlds", R.string.nav_worlds, Icons.Default.Public, "🌍")
    object Generators : NavRoute("generators", R.string.nav_generators, Icons.Default.Factory, "🏭")
    object MiniGames : NavRoute("minigames", R.string.nav_minigames, Icons.Default.SportsEsports, "🎮")
    object More : NavRoute("more", R.string.nav_more, Icons.Default.MoreHoriz, "⚙️")
}

val bottomNavItems = listOf(
    NavRoute.Home,
    NavRoute.Worlds,
    NavRoute.Generators,
    NavRoute.MiniGames,
    NavRoute.More
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TapEmpireApp(
    gameViewModel: GameViewModel
) {
    val uiState by gameViewModel.uiState.collectAsStateWithLifecycle()
    val offlineEarnings by gameViewModel.offlineEarnings.collectAsStateWithLifecycle()
    val achievementPopup by gameViewModel.achievementPopup.collectAsStateWithLifecycle()

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Show bottom nav only on main tabs
    val mainRoutes = bottomNavItems.map { it.route }
    val showBottomBar = currentRoute in mainRoutes

    // Offline earnings dialog
    offlineEarnings?.let { earnings ->
        AlertDialog(
            onDismissRequest = { gameViewModel.dismissOfflineEarnings() },
            title = {
                Text(stringResource(R.string.offline_title), fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    stringResource(R.string.offline_text, GameState.fmt(earnings)),
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = { gameViewModel.dismissOfflineEarnings() },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                ) {
                    Text(stringResource(R.string.btn_great))
                }
            },
            containerColor = Onyx,
            titleContentColor = CoinGold,
            textContentColor = TextSecondary
        )
    }

    // Achievement popup
    achievementPopup?.let { ach ->
        AlertDialog(
            onDismissRequest = { gameViewModel.dismissAchievementPopup() },
            title = {
                Text(
                    stringResource(R.string.achievement_title, ach.emoji),
                    fontWeight = FontWeight.Bold,
                    color = CoinGold
                )
            },
            text = {
                Column {
                    Text(ach.name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 18.sp)
                    Text(ach.description, color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "💰 +${GameState.fmt(ach.reward)}",
                        fontWeight = FontWeight.Bold,
                        color = CoinGold,
                        fontSize = 16.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { gameViewModel.dismissAchievementPopup() },
                    colors = ButtonDefaults.buttonColors(containerColor = CoinGold)
                ) {
                    Text(stringResource(R.string.btn_great), color = DeepSpace)
                }
            },
            containerColor = Onyx,
            titleContentColor = CoinGold
        )
    }

    // Mini-game reward popup
    val miniGameReward by gameViewModel.lastMiniGameReward.collectAsStateWithLifecycle()
    miniGameReward?.let { reward ->
        AlertDialog(
            onDismissRequest = { gameViewModel.dismissMiniGameReward() },
            title = {
                Text(stringResource(R.string.minigame_reward_title), fontWeight = FontWeight.Bold, color = CoinGold)
            },
            text = {
                Text(
                    stringResource(R.string.minigame_reward_text, GameState.fmt(reward)),
                    color = TextSecondary,
                    fontSize = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { gameViewModel.dismissMiniGameReward() },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                ) {
                    Text(stringResource(R.string.btn_great))
                }
            },
            containerColor = Onyx,
            titleContentColor = CoinGold,
            textContentColor = TextSecondary
        )
    }

    // Tutorial popup system
    val tutorialKey by gameViewModel.tutorialToShow.collectAsStateWithLifecycle()
    tutorialKey?.let { key ->
        val (title, body, icon) = getTutorialContent(key)
        AlertDialog(
            onDismissRequest = { gameViewModel.dismissTutorial() },
            title = {
                Text("$icon $title", fontWeight = FontWeight.Bold, color = NeonCyan, fontSize = 20.sp)
            },
            text = {
                Text(body, color = TextSecondary, fontSize = 14.sp, lineHeight = 20.sp)
            },
            confirmButton = {
                Button(
                    onClick = { gameViewModel.dismissTutorial() },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                ) {
                    Text(stringResource(R.string.tutorial_got_it), color = DeepSpace)
                }
            },
            containerColor = Onyx,
            titleContentColor = NeonCyan
        )
    }

    // === NEW EVENT NOTIFICATION BANNER ===
    val eventNotification by gameViewModel.eventNotification.collectAsStateWithLifecycle()
    var showEventBanner by remember { mutableStateOf(false) }
    var currentEventNotif by remember { mutableStateOf<GameEvent?>(null) }

    LaunchedEffect(eventNotification) {
        eventNotification?.let { event ->
            currentEventNotif = event
            showEventBanner = true
            delay(6000) // auto-dismiss after 6 seconds
            showEventBanner = false
            delay(400) // wait for exit animation
            gameViewModel.dismissEventNotification()
        }
    }

    // === AUTO-RESOLVED EVENT WARNING DIALOG ===
    val autoResolvedEvent by gameViewModel.autoResolvedNotification.collectAsStateWithLifecycle()
    autoResolvedEvent?.let { event ->
        val chosen = event.choices.getOrNull(event.choiceIndex)
        val titleText = getEventTitleForBanner(event.type)
        AlertDialog(
            onDismissRequest = { gameViewModel.dismissAutoResolvedNotification() },
            title = {
                Text("⚠️ ${stringResource(R.string.event_auto_resolved_title)}",
                    fontWeight = FontWeight.Bold, color = Error, fontSize = 18.sp)
            },
            text = {
                Column {
                    Text(
                        stringResource(R.string.event_auto_resolved_body, titleText),
                        color = TextSecondary, fontSize = 14.sp, lineHeight = 20.sp
                    )
                    if (chosen != null) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "${stringResource(R.string.event_auto_resolved_chosen)}:",
                            fontWeight = FontWeight.Bold, color = Error, fontSize = 13.sp
                        )
                        Text(
                            "${chosen.emoji} ${getEventTitleForChoice(chosen.labelKey)}",
                            fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        if (chosen.effects.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            chosen.effects.forEach { (key, value) ->
                                val (emoji, label) = getEffectDisplay(key, value)
                                Text("$emoji $label", fontSize = 12.sp,
                                    color = if (value >= 0) NeonGreen else Error)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { gameViewModel.dismissAutoResolvedNotification() },
                    colors = ButtonDefaults.buttonColors(containerColor = Error)
                ) {
                    Text(stringResource(R.string.event_auto_resolved_ok), color = TextPrimary)
                }
            },
            containerColor = Onyx,
            titleContentColor = Error
        )
    }

    // Event banner overlay (shown on top of everything)
    Box(modifier = Modifier.fillMaxSize()) {

    Scaffold(
        containerColor = DeepSpace,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = Onyx.copy(alpha = 0.95f),
                    contentColor = TextPrimary,
                    tonalElevation = 0.dp
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    item.icon,
                                    contentDescription = stringResource(item.titleRes),
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = stringResource(item.titleRes),
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = NeonCyan,
                                selectedTextColor = NeonCyan,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted,
                                indicatorColor = NeonCyan.copy(alpha = 0.12f)
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues),
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None }
        ) {
            composable("home") {
                HomeScreen(viewModel = gameViewModel, uiState = uiState)
            }
            composable("worlds") {
                WorldsScreen(viewModel = gameViewModel, uiState = uiState)
            }
            composable("generators") {
                GeneratorsScreen(viewModel = gameViewModel, uiState = uiState)
            }
            composable("minigames") {
                MiniGamesScreen(
                    viewModel = gameViewModel,
                    uiState = uiState,
                    onNavigateToGame = { route -> navController.navigate(route) }
                )
            }
            composable("more") {
                MoreScreen(
                    uiState = uiState,
                    onNavigate = { route -> navController.navigate(route) }
                )
            }
            composable("shop") {
                ShopScreen(viewModel = gameViewModel, uiState = uiState)
            }
            composable("achievements") {
                AchievementsScreen(uiState = uiState)
            }
            composable("prestige") {
                PrestigeScreen(viewModel = gameViewModel, uiState = uiState)
            }
            composable("missions") {
                MissionsScreen(viewModel = gameViewModel, uiState = uiState)
            }
            composable("pets") {
                PetsScreen(viewModel = gameViewModel, uiState = uiState)
            }
            composable("nursery") {
                NurseryScreen(viewModel = gameViewModel, uiState = uiState)
            }
            composable("stats") {
                StatsScreen(uiState = uiState)
            }
            composable("business_map") {
                BusinessMapScreen(viewModel = gameViewModel, uiState = uiState)
            }
            composable("workers") {
                WorkersScreen(viewModel = gameViewModel, uiState = uiState)
            }
            composable("contracts") {
                ContractsScreen(viewModel = gameViewModel, uiState = uiState)
            }
            composable("events") {
                EventsScreen(viewModel = gameViewModel, uiState = uiState)
            }
            composable("premium_shop") {
                PremiumShopScreen(viewModel = gameViewModel, uiState = uiState)
            }

            // === PLAYABLE MINI-GAMES ===
            composable("minigame_star_catcher") {
                val idx = gameViewModel.activeMiniGameIndex.collectAsStateWithLifecycle()
                StarCatcherScreen(
                    onGameComplete = { perf ->
                        gameViewModel.collectMiniGameReward(idx.value, perf)
                        navController.popBackStack()
                    },
                    onCancel = {
                        gameViewModel.cancelMiniGame()
                        navController.popBackStack()
                    }
                )
            }
            composable("minigame_cosmic_billiards") {
                val idx = gameViewModel.activeMiniGameIndex.collectAsStateWithLifecycle()
                CosmicBilliardsScreen(
                    onGameComplete = { perf ->
                        gameViewModel.collectMiniGameReward(idx.value, perf)
                        navController.popBackStack()
                    },
                    onCancel = {
                        gameViewModel.cancelMiniGame()
                        navController.popBackStack()
                    }
                )
            }
            composable("minigame_asteroid_dodge") {
                val idx = gameViewModel.activeMiniGameIndex.collectAsStateWithLifecycle()
                AsteroidDodgeScreen(
                    onGameComplete = { perf ->
                        gameViewModel.collectMiniGameReward(idx.value, perf)
                        navController.popBackStack()
                    },
                    onCancel = {
                        gameViewModel.cancelMiniGame()
                        navController.popBackStack()
                    }
                )
            }
            composable("minigame_gravity_slingshot") {
                val idx = gameViewModel.activeMiniGameIndex.collectAsStateWithLifecycle()
                GravitySlingshotScreen(
                    onGameComplete = { perf ->
                        gameViewModel.collectMiniGameReward(idx.value, perf)
                        navController.popBackStack()
                    },
                    onCancel = {
                        gameViewModel.cancelMiniGame()
                        navController.popBackStack()
                    }
                )
            }
            composable("minigame_tap_frenzy") {
                val idx = gameViewModel.activeMiniGameIndex.collectAsStateWithLifecycle()
                TapFrenzyScreen(
                    onGameComplete = { perf ->
                        gameViewModel.collectMiniGameReward(idx.value, perf)
                        navController.popBackStack()
                    },
                    onCancel = {
                        gameViewModel.cancelMiniGame()
                        navController.popBackStack()
                    }
                )
            }
            composable("minigame_memory_match") {
                val idx = gameViewModel.activeMiniGameIndex.collectAsStateWithLifecycle()
                MemoryMatchScreen(
                    onGameComplete = { perf ->
                        gameViewModel.collectMiniGameReward(idx.value, perf)
                        navController.popBackStack()
                    },
                    onCancel = {
                        gameViewModel.cancelMiniGame()
                        navController.popBackStack()
                    }
                )
            }
            composable("minigame_coin_rain") {
                val idx = gameViewModel.activeMiniGameIndex.collectAsStateWithLifecycle()
                CoinRainScreen(
                    onGameComplete = { perf ->
                        gameViewModel.collectMiniGameReward(idx.value, perf)
                        navController.popBackStack()
                    },
                    onCancel = {
                        gameViewModel.cancelMiniGame()
                        navController.popBackStack()
                    }
                )
            }
            composable("minigame_boss_battle") {
                val idx = gameViewModel.activeMiniGameIndex.collectAsStateWithLifecycle()
                BossBattleScreen(
                    onGameComplete = { perf ->
                        gameViewModel.collectMiniGameReward(idx.value, perf)
                        navController.popBackStack()
                    },
                    onCancel = {
                        gameViewModel.cancelMiniGame()
                        navController.popBackStack()
                    }
                )
            }
            composable("minigame_lucky_box") {
                val idx = gameViewModel.activeMiniGameIndex.collectAsStateWithLifecycle()
                LuckyBoxScreen(
                    onGameComplete = { perf ->
                        gameViewModel.collectMiniGameReward(idx.value, perf)
                        navController.popBackStack()
                    },
                    onCancel = {
                        gameViewModel.cancelMiniGame()
                        navController.popBackStack()
                    }
                )
            }
            composable("minigame_fortune_wheel") {
                val idx = gameViewModel.activeMiniGameIndex.collectAsStateWithLifecycle()
                FortuneWheelScreen(
                    onGameComplete = { perf ->
                        gameViewModel.collectMiniGameReward(idx.value, perf)
                        navController.popBackStack()
                    },
                    onCancel = {
                        gameViewModel.cancelMiniGame()
                        navController.popBackStack()
                    }
                )
            }
        }
    }

    // Event notification banner overlay (on top of everything)
    AnimatedVisibility(
        visible = showEventBanner && currentEventNotif != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = Modifier.align(Alignment.TopCenter)
    ) {
        currentEventNotif?.let { event ->
            val categoryColor = when (event.type.category) {
                GameEvent.EventCategory.GLOBAL -> Error
                GameEvent.EventCategory.WORLD_LOCAL -> NeonCyan
                GameEvent.EventCategory.CORPORATE -> CoinGold
            }
            val categoryLabel = when (event.type.category) {
                GameEvent.EventCategory.GLOBAL -> stringResource(R.string.event_cat_global)
                GameEvent.EventCategory.WORLD_LOCAL -> stringResource(R.string.event_cat_local)
                GameEvent.EventCategory.CORPORATE -> stringResource(R.string.event_cat_corporate)
            }
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .clickable {
                        showEventBanner = false
                        gameViewModel.dismissEventNotification()
                        navController.navigate("events") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                        }
                    },
                shape = RoundedCornerShape(16.dp),
                color = Onyx.copy(alpha = 0.95f),
                shadowElevation = 8.dp,
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Pulsing emoji
                    val infiniteTransition = rememberInfiniteTransition(label = "eventPulse")
                    val scale by infiniteTransition.animateFloat(
                        initialValue = 1f, targetValue = 1.2f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ), label = "pulse"
                    )
                    Text(
                        event.type.emoji, fontSize = 28.sp,
                        modifier = Modifier.graphicsLayer { scaleX = scale; scaleY = scale }
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.event_new_alert),
                            fontSize = 11.sp, fontWeight = FontWeight.Bold,
                            color = categoryColor
                        )
                        Text(
                            getEventTitleForBanner(event.type),
                            fontSize = 15.sp, fontWeight = FontWeight.Black,
                            color = TextPrimary, maxLines = 1
                        )
                        Text(
                            "$categoryLabel • ${stringResource(R.string.event_tap_to_view)}",
                            fontSize = 10.sp, color = TextMuted
                        )
                    }
                    // Category indicator dot
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(categoryColor)
                    )
                }
            }
        }
    }

    } // close Box
}

/**
 * Quick title lookup for event notification banner (avoids dependency on EventsScreen's full map)
 */
@Composable
fun getEventTitleForBanner(type: GameEvent.EventType): String {
    val resId = when (type) {
        GameEvent.EventType.GLOBAL_ECONOMIC_CRISIS -> R.string.event_title_crisis
        GameEvent.EventType.GLOBAL_ECONOMIC_BOOM -> R.string.event_title_boom
        GameEvent.EventType.LEGAL_REFORM -> R.string.event_title_legal
        GameEvent.EventType.PANDEMIC_OUTBREAK -> R.string.event_title_pandemic
        GameEvent.EventType.GOLDEN_AGE -> R.string.event_title_golden
        GameEvent.EventType.ALIEN_CONTACT -> R.string.event_title_alien
        GameEvent.EventType.GALACTIC_WAR -> R.string.event_title_war
        GameEvent.EventType.TRADE_AGREEMENT -> R.string.event_title_trade
        GameEvent.EventType.CLIMATE_DISASTER -> R.string.event_title_climate
        GameEvent.EventType.MARKET_CRASH -> R.string.event_title_market_crash
        GameEvent.EventType.GALACTIC_FESTIVAL -> R.string.event_title_festival
        GameEvent.EventType.DIPLOMATIC_INCIDENT -> R.string.event_title_diplomatic
        GameEvent.EventType.DARK_MATTER_SURGE -> R.string.event_title_dark_matter
        GameEvent.EventType.SCIENTIFIC_DISCOVERY -> R.string.event_title_science
        GameEvent.EventType.TECH_BREAKTHROUGH -> R.string.event_title_tech
        GameEvent.EventType.RESOURCE_SHORTAGE -> R.string.event_title_resource
        GameEvent.EventType.ENERGY_BLACKOUT -> R.string.event_title_blackout
        GameEvent.EventType.TERRAFORMING_SUCCESS -> R.string.event_title_terraforming
        GameEvent.EventType.SPACE_DEBRIS -> R.string.event_title_debris
        GameEvent.EventType.CYBER_ATTACK -> R.string.event_title_cyber
        GameEvent.EventType.INNOVATION_GRANT -> R.string.event_title_grant
        GameEvent.EventType.SUPPLY_CHAIN_CRISIS -> R.string.event_title_supply
        GameEvent.EventType.MUTANT_OUTBREAK -> R.string.event_title_mutant
        GameEvent.EventType.SECTOR_STRIKE -> R.string.event_title_strike
        GameEvent.EventType.COMPETITOR_INVASION -> R.string.event_title_competitor
        GameEvent.EventType.UNION_MOVEMENT -> R.string.event_title_union
        GameEvent.EventType.ANIMAL_WELFARE_SCANDAL -> R.string.event_title_welfare
        GameEvent.EventType.COMPANY_OF_YEAR -> R.string.event_title_award
        GameEvent.EventType.CORRUPTION_SCANDAL -> R.string.event_title_corruption
        GameEvent.EventType.TAX_AUDIT -> R.string.event_title_tax
        GameEvent.EventType.WORKER_TRAINING -> R.string.event_title_training
        GameEvent.EventType.ESPIONAGE -> R.string.event_title_espionage
        GameEvent.EventType.LABOR_SHORTAGE -> R.string.event_title_labor
        GameEvent.EventType.PET_COMPETITION -> R.string.event_title_petcomp
        GameEvent.EventType.INVESTOR_INTEREST -> R.string.event_title_investor
    }
    return stringResource(resId)
}

/**
 * Lookup choice label for auto-resolved event dialog.
 * Reuses getEventString from EventsScreen.
 */
@Composable
fun getEventTitleForChoice(labelKey: String): String {
    return getEventString(labelKey)
}

/**
 * Returns (title, body, icon) for each tutorial key.
 */
@Composable
private fun getTutorialContent(key: String): Triple<String, String, String> {
    return when (key) {
        "welcome" -> Triple(
            stringResource(R.string.tutorial_welcome_title),
            stringResource(R.string.tutorial_welcome_body),
            "👋"
        )
        "workers_unlocked" -> Triple(
            stringResource(R.string.tutorial_workers_title),
            stringResource(R.string.tutorial_workers_body),
            "👷"
        )
        "first_generator" -> Triple(
            stringResource(R.string.tutorial_generators_title),
            stringResource(R.string.tutorial_generators_body),
            "🏭"
        )
        "prestige_available" -> Triple(
            stringResource(R.string.tutorial_prestige_title),
            stringResource(R.string.tutorial_prestige_body),
            "⭐"
        )
        "minigames" -> Triple(
            stringResource(R.string.tutorial_minigames_title),
            stringResource(R.string.tutorial_minigames_body),
            "🎮"
        )
        "pets" -> Triple(
            stringResource(R.string.tutorial_pets_title),
            stringResource(R.string.tutorial_pets_body),
            "🐾"
        )
        "worlds" -> Triple(
            stringResource(R.string.tutorial_worlds_title),
            stringResource(R.string.tutorial_worlds_body),
            "🌍"
        )
        else -> Triple("Tutorial", "", "📖")
    }
}
