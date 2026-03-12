package com.example.juego.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.juego.Achievement
import com.example.juego.DailyMission
import com.example.juego.GameEvent
import com.example.juego.GameState
import com.example.juego.Generator
import com.example.juego.MiniGame
import com.example.juego.MonetizationManager
import com.example.juego.Pet
import com.example.juego.SpecialEvent
import com.example.juego.Upgrade
import com.example.juego.Worker
import com.example.juego.WorkerBox
import com.example.juego.World
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val state = GameState.getInstance()
    private val prefs = application.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    // === UI STATE ===
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    // === TAP FEEDBACK ===
    private val _tapEvent = MutableStateFlow<TapFeedback?>(null)
    val tapEvent: StateFlow<TapFeedback?> = _tapEvent.asStateFlow()

    // === ACHIEVEMENT POPUP ===
    private val _achievementPopup = MutableStateFlow<Achievement?>(null)
    val achievementPopup: StateFlow<Achievement?> = _achievementPopup.asStateFlow()

    // === OFFLINE EARNINGS ===
    private val _offlineEarnings = MutableStateFlow<Double?>(null)
    val offlineEarnings: StateFlow<Double?> = _offlineEarnings.asStateFlow()

    // === MINI-GAME REWARD ===
    private val _lastMiniGameReward = MutableStateFlow<Double?>(null)
    val lastMiniGameReward: StateFlow<Double?> = _lastMiniGameReward.asStateFlow()

    // === EVENT NOTIFICATION ===
    private val _eventNotification = MutableStateFlow<GameEvent?>(null)
    val eventNotification: StateFlow<GameEvent?> = _eventNotification.asStateFlow()

    // === AUTO-RESOLVED EVENT NOTIFICATION ===
    private val _autoResolvedNotification = MutableStateFlow<GameEvent?>(null)
    val autoResolvedNotification: StateFlow<GameEvent?> = _autoResolvedNotification.asStateFlow()

    fun dismissEventNotification() { _eventNotification.value = null }
    fun dismissAutoResolvedNotification() { _autoResolvedNotification.value = null }

    // === TUTORIAL SYSTEM ===
    private val _tutorialToShow = MutableStateFlow<String?>(null)
    val tutorialToShow: StateFlow<String?> = _tutorialToShow.asStateFlow()

    // Track which tutorials have been shown
    private val shownTutorials = mutableSetOf<String>()

    fun dismissTutorial() { _tutorialToShow.value = null }

    private fun showTutorialOnce(key: String) {
        if (key !in shownTutorials) {
            shownTutorials.add(key)
            _tutorialToShow.value = key
            // Persist
            prefs.edit().putStringSet("shown_tutorials", shownTutorials).apply()
        }
    }

    fun dismissMiniGameReward() { _lastMiniGameReward.value = null }

    init {
        // Load shown tutorials from prefs
        prefs.getStringSet("shown_tutorials", emptySet())?.let { shownTutorials.addAll(it) }
        loadState()
        checkOfflineEarnings()
        startGameLoop()
        // Welcome tutorial on first launch
        showTutorialOnce("welcome")
    }

    private fun startGameLoop() {
        viewModelScope.launch {
            while (true) {
                state.updateProduction(System.currentTimeMillis())
                // Track production-based missions
                state.updateMissionProgress(DailyMission.MissionType.REACH_PRODUCTION, state.productionPerSecond)
                state.updateMissionProgress(DailyMission.MissionType.REACH_TAP_TOTAL, state.totalTaps.toDouble())
                checkNewAchievements()

                // Auto-tap from monetization
                if (state.monetization.isAutoTapActive) {
                    state.tap()  // 1 tap per loop tick (~20/sec)
                }

                // Update nursery welfare
                state.updateNurseryWelfare()

                // Update galactic stability & check events
                state.updateGalacticStability()
                val newEvent = state.checkEventTriggers()
                if (newEvent != null) {
                    _eventNotification.value = newEvent
                }

                // Check for auto-resolved events (player didn't choose)
                val autoResolved = state.getAndClearAutoResolvedEvents()
                if (autoResolved.isNotEmpty()) {
                    // Notify about the first one (queue if multiple)
                    for (ev in autoResolved) {
                        _autoResolvedNotification.value = ev
                    }
                }

                // NOTE: Incubation hatching is now manual via hatchPet()

                // Progressive tutorial triggers (only check if not already shown)
                try {
                    if ("prestige_available" !in shownTutorials && state.canPrestige()) showTutorialOnce("prestige_available")
                    if ("minigames" !in shownTutorials && state.miniGames?.isNotEmpty() == true && state.totalMiniGamesPlayed > 0) showTutorialOnce("minigames")
                    if ("pets" !in shownTutorials && state.pets?.any { it.isOwned } == true) showTutorialOnce("pets")
                } catch (_: Exception) { /* state not fully initialized yet */ }

                refreshUiState()
                delay(50)
            }
        }
    }

    fun onTap(): TapFeedback {
        val result = state.tap()
        val feedback = TapFeedback(
            amount = result.amount,
            isCritical = result.isCritical,
            comboCount = result.comboCount,
            comboMultiplier = result.comboMultiplier
        )
        _tapEvent.value = feedback
        checkNewAchievements()
        refreshUiState()
        return feedback
    }

    fun clearTapEvent() {
        _tapEvent.value = null
    }

    fun buyGenerator(index: Int): Boolean {
        val success = state.buyGenerator(index)
        if (success) {
            if (state.generators.any { it.owned > 0 }) showTutorialOnce("first_generator")
            refreshUiState()
        }
        return success
    }

    fun buyUpgrade(index: Int): Boolean {
        val success = state.buyUpgrade(index)
        if (success) refreshUiState()
        return success
    }

    fun switchWorld(index: Int): Boolean {
        val success = state.switchWorld(index)
        if (success) {
            if (index > 0) showTutorialOnce("worlds")
            refreshUiState()
        }
        return success
    }

    fun buyWorld(index: Int): Boolean {
        val success = state.buyWorld(index)
        if (success) refreshUiState()
        return success
    }

    fun buyPet(index: Int): Boolean {
        val success = state.buyPet(index)
        if (success) refreshUiState()
        return success
    }

    fun setActivePet(index: Int) {
        state.setActivePet(index)
        refreshUiState()
    }

    fun feedPet(): Boolean {
        val success = state.feedPet()
        if (success) refreshUiState()
        return success
    }

    fun petPet(): Boolean {
        val success = state.petPet()
        if (success) refreshUiState()
        return success
    }

    // Indexed pet care (Nursery)
    fun feedPetAt(index: Int): Boolean {
        val success = state.feedPetAt(index)
        if (success) refreshUiState()
        return success
    }

    fun petPetAt(index: Int): Boolean {
        val success = state.petPetAt(index)
        if (success) refreshUiState()
        return success
    }

    fun cleanPet(index: Int): Boolean {
        val success = state.cleanPet(index)
        if (success) refreshUiState()
        return success
    }

    fun playPet(index: Int): Boolean {
        val success = state.playPet(index)
        if (success) refreshUiState()
        return success
    }

    fun restPet(index: Int): Boolean {
        val success = state.restPet(index)
        if (success) refreshUiState()
        return success
    }

    fun treatPetDisease(index: Int, disease: Pet.Disease): Boolean {
        val success = state.treatPetDisease(index, disease)
        if (success) refreshUiState()
        return success
    }

    fun revivePet(index: Int): Boolean {
        val success = state.revivePet(index)
        if (success) refreshUiState()
        return success
    }

    // === BREEDING & NURSERY ===

    fun breedPets(index1: Int, index2: Int): Pet? {
        val offspring = state.breedPets(index1, index2)
        if (offspring != null) refreshUiState()
        return offspring
    }

    /** Manually hatch a pet whose incubation is complete */
    fun hatchPet(petIndex: Int): Boolean {
        val pets = state.pets
        if (petIndex !in pets.indices) return false
        val pet = pets[petIndex]
        if (!pet.isIncubating || !pet.isReadyToHatch) return false
        pet.hatch()
        if (pet.hadMutation()) {
            showTutorialOnce("mutation_${pet.petId}")
        }
        state.checkAchievements()
        refreshUiState()
        return true
    }

    fun assignWorkerToNursery(workerId: String): Boolean {
        val success = state.assignWorkerToNursery(workerId)
        if (success) refreshUiState()
        return success
    }

    fun unassignWorkerFromNursery(workerId: String) {
        for (w in state.ownedWorkers) {
            if (w.id == workerId && w.assignedGeneratorId == GameState.NURSERY_ASSIGNMENT_ID) {
                w.assignedGeneratorId = null
                break
            }
        }
        refreshUiState()
    }

    // === EVENTS ===

    fun resolveEvent(eventId: String, choiceIndex: Int) {
        state.resolveEvent(eventId, choiceIndex)
        refreshUiState()
    }

    fun performPrestige() {
        state.performPrestige()
        refreshUiState()
    }

    fun canPrestige() = state.canPrestige()
    fun getPrestigePointsAvailable() = state.prestigePointsAvailable

    fun claimMission(index: Int): Boolean {
        val success = state.claimMissionReward(index)
        if (success) {
            _stateVersion++
            refreshUiState()
        }
        return success
    }

    // === BUSINESS PROPERTY UPGRADES (via Business Map) ===
    fun upgradeWorkers(index: Int): Boolean {
        val success = state.upgradeGeneratorWorkers(index)
        if (success) refreshUiState()
        return success
    }

    fun upgradeSize(index: Int): Boolean {
        val success = state.upgradeGeneratorSize(index)
        if (success) refreshUiState()
        return success
    }

    fun upgradeLocation(index: Int): Boolean {
        val success = state.upgradeGeneratorLocation(index)
        if (success) refreshUiState()
        return success
    }

    fun openBranch(index: Int): Boolean {
        val success = state.openBranch(index)
        if (success) refreshUiState()
        return success
    }

    // === WORKER SYSTEM ===
    /** Opens a box and returns the worker WITHOUT adding to roster - must negotiate first */
    fun buyWorkerBox(type: WorkerBox.BoxType): Worker? {
        val cost = WorkerBox.getCost(type, state.productionPerSecond)
        if (state.coins < cost) return null
        state.coins = state.coins - cost
        val worker = WorkerBox.open(type, state.productionPerSecond)
        refreshUiState()
        return worker // NOT added to roster yet - needs contract negotiation
    }

    /** After successful negotiation, add worker to roster */
    fun hireWorker(worker: Worker) {
        state.ownedWorkers.add(worker)
        refreshUiState()
    }

    /** Negotiate contract with a worker. Returns 0=accepted, 1=rejected (can retry), 2=final */
    fun negotiateContract(worker: Worker, template: Worker.ContractTemplate): Int {
        return state.negotiateContract(worker, template)
    }

    fun assignWorker(workerId: String, generatorIndex: Int): Boolean {
        val success = state.assignWorker(workerId, generatorIndex)
        if (success) refreshUiState()
        return success
    }

    fun unassignWorker(workerId: String): Boolean {
        val success = state.unassignWorker(workerId)
        if (success) refreshUiState()
        return success
    }

    fun fireWorker(workerId: String): Boolean {
        val success = state.fireWorker(workerId)
        if (success) refreshUiState()
        return success
    }

    // === CONTRACT MANAGEMENT ===
    fun addContractTemplate(template: Worker.ContractTemplate) {
        state.addContractTemplate(template)
        refreshUiState()
    }

    fun removeContractTemplate(templateId: String) {
        state.removeContractTemplate(templateId)
        refreshUiState()
    }

    fun resolveWorkerStrike(workerId: String, template: Worker.ContractTemplate): Boolean {
        val success = state.resolveWorkerStrike(workerId, template)
        if (success) refreshUiState()
        return success
    }

    fun resolveMassStrike(template: Worker.ContractTemplate): Int {
        val resolved = state.resolveMassStrike(template)
        if (resolved > 0) refreshUiState()
        return resolved
    }

    // State version counter to force Compose recomposition on mutable Java object changes
    private var _stateVersion = 0

    // Track which mini-game index is currently being played
    private val _activeMiniGameIndex = MutableStateFlow(-1)
    val activeMiniGameIndex: StateFlow<Int> = _activeMiniGameIndex.asStateFlow()

    fun startMiniGame(index: Int): Boolean {
        val games = state.miniGames
        if (index in games.indices && games[index].isAvailable) {
            games[index].startGame()
            state.totalMiniGamesPlayed = state.totalMiniGamesPlayed + 1
            _activeMiniGameIndex.value = index
            refreshUiState()
            return true
        }
        return false
    }

    fun collectMiniGameReward(index: Int, performanceMultiplier: Double): Double {
        val games = state.miniGames
        if (index in games.indices) {
            val reward = games[index].calculateReward(performanceMultiplier, state.productionPerSecond)
            state.coins = state.coins + reward
            state.totalCoinsEarned = state.totalCoinsEarned + reward
            // Mission tracking
            state.updateMissionProgress(DailyMission.MissionType.PLAY_MINIGAME, 1.0)
            state.updateMissionProgress(DailyMission.MissionType.EARN_FROM_MINIGAMES, reward)
            state.updateMissionProgress(DailyMission.MissionType.PLAY_ALL_MINIGAMES, 1.0)
            state.updateMissionProgress(DailyMission.MissionType.EARN_COINS, reward)
            _activeMiniGameIndex.value = -1
            _lastMiniGameReward.value = reward
            refreshUiState()
            return reward
        }
        return 0.0
    }

    fun cancelMiniGame() {
        _activeMiniGameIndex.value = -1
    }

    fun getActiveMiniGameType(): MiniGame.MiniGameType? {
        val idx = _activeMiniGameIndex.value
        if (idx < 0) return null
        return state.miniGames.getOrNull(idx)?.type
    }

    fun dismissOfflineEarnings() {
        _offlineEarnings.value = null
    }

    fun dismissAchievementPopup() {
        _achievementPopup.value = null
    }

    // === MONETIZATION ===

    fun purchasePack(packId: String) {
        // In production, this would go through Google Play Billing first.
        // For now, process directly (simulate successful purchase).
        val monetization = state.monetization
        if (monetization.processPurchase(packId, state)) {
            refreshUiState()
            saveState()
        }
    }

    fun claimDailyVipGems(): Int {
        val amount = state.monetization.claimDailyGems()
        if (amount > 0) {
            refreshUiState()
            saveState()
        }
        return amount
    }

    fun buyAutoTap(): Boolean {
        val success = state.monetization.buyAutoTap()
        if (success) refreshUiState()
        return success
    }

    fun buyInstantCoins(): Boolean {
        val success = state.monetization.buyInstantCoins(state)
        if (success) refreshUiState()
        return success
    }

    fun activateWhaleMultiplier(): Boolean {
        val success = state.monetization.activateExclusiveMultiplier()
        if (success) refreshUiState()
        return success
    }

    private fun checkNewAchievements() {
        val recent = state.andClearRecentlyUnlocked
        for (ach in recent) {
            _achievementPopup.value = ach
        }
    }

    private fun checkOfflineEarnings() {
        val lastSave = prefs.getLong(K_LAST_SAVE, 0)
        if (lastSave > 0) {
            val earnings = state.calculateOfflineEarnings(lastSave)
            if (earnings > 0) {
                state.addOfflineEarnings(earnings)
                _offlineEarnings.value = earnings
            }
        }
    }

    private fun refreshUiState() {
        val combo = state.comboSystem
        val event = state.activeEvent
        val activePet = state.activePet

        // Deep-copy daily missions so Compose detects changes on mutable Java objects
        val missionSnapshots = state.dailyMissions.map { m ->
            MissionSnapshot(
                id = m.id,
                type = m.type,
                description = m.description,
                targetValue = m.targetValue,
                currentProgress = m.currentProgress,
                coinReward = m.coinReward,
                bonusReward = m.bonusReward,
                completed = m.isCompleted,
                claimed = m.isClaimed,
                difficulty = m.difficulty,
                progressPercent = m.progressPercent
            )
        }

        _uiState.value = GameUiState(
            coins = state.coins,
            totalCoinsEarned = state.totalCoinsEarned,
            perTap = state.perTap,
            perSecond = state.productionPerSecond,
            totalTaps = state.totalTaps,
            prestigeLevel = state.prestigeLevel,
            prestigePoints = state.prestigePoints,
            prestigeMultiplier = state.prestigeMultiplier,
            canPrestige = state.canPrestige(),
            currentWorldIndex = state.currentWorldIndex,
            currentWorld = state.currentWorld,
            worlds = state.worlds.toList(),
            generators = state.generators.toList(),
            upgrades = state.upgrades.toList(),
            achievements = state.achievements.toList(),
            miniGames = state.miniGames.toList(),
            dailyMissions = state.dailyMissions.toList(),
            missionSnapshots = missionSnapshots,
            pets = state.pets.toList(),
            activePet = activePet,
            comboCount = combo.currentCombo,
            comboMultiplier = combo.multiplier,
            comboActive = combo.isComboActive,
            comboTimePercent = combo.comboTimePercent,
            activeEvent = event,
            criticalChance = state.criticalChance,
            criticalMultiplier = state.criticalMultiplier,
            // Global stats
            globalTotalCoinsEarned = state.globalTotalCoinsEarned,
            globalTotalTaps = state.globalTotalTaps,
            globalTotalGeneratorsBought = state.globalTotalGeneratorsBought,
            totalPrestigesPerformed = state.totalPrestigesPerformed,
            globalTotalCriticalHits = state.globalTotalCriticalHits,
            totalGeneratorsBought = state.totalGeneratorsBought,
            totalMiniGamesPlayed = state.totalMiniGamesPlayed,
            stateVersion = _stateVersion,
            ownedWorkers = state.ownedWorkers.toList(),
            totalMonthlySalary = state.totalMonthlySalary,
            contractTemplates = state.contractTemplates.toList(),
            workersUnlocked = state.worlds.size >= 3 && state.worlds[2].isUnlocked,
            nurseryWelfareLevel = state.nurseryWelfareLevel,
            nurseryWorkerCount = state.nurseryWorkerCount,
            nurseryWorkersNeeded = state.nurseryWorkersNeeded,
            galacticStability = state.galacticStability,
            activeGameEvents = state.activeGameEvents.filter { it.isActive }.toList(),
            eventHistory = state.eventHistory.toList(),
            maxComboReached = state.maxComboReached,
            monetizationState = run {
                val m = state.monetization
                com.example.juego.ui.screens.MonetizationUiState(
                    gems = m.gems,
                    vipTier = m.vipTier,
                    nextVipTier = m.nextVipTier,
                    vipProgress = m.vipProgress,
                    centsToNextTier = m.centsToNextTier,
                    canClaimDailyGems = m.canClaimDailyGems(),
                    starterPackAvailable = m.isStarterPackAvailable,
                    starterPackRemainingMs = m.starterPackRemainingMs,
                    whaleUnlocked = m.isWhaleOffersUnlocked,
                    boostActive = m.isBoostActive,
                    boostMultiplier = m.boostMultiplier,
                    boostRemainingMs = m.boostRemainingMs,
                    autoTapActive = m.isAutoTapActive,
                    availablePacks = m.availablePacks,
                    totalSpentCents = m.totalSpentCents
                )
            }
        )

        // Trigger workers tutorial when unlocked for the first time
        if (state.worlds.size >= 3 && state.worlds[2].isUnlocked) {
            showTutorialOnce("workers_unlocked")
        }
    }

    fun saveState() {
        try {
            val json = JSONObject().apply {
                put("coins", state.coins)
                put("totalCoinsEarned", state.totalCoinsEarned)
                put("totalTaps", state.totalTaps)
                put("prestigeLevel", state.prestigeLevel)
                put("prestigePoints", state.prestigePoints)
                put("currentWorldIndex", state.currentWorldIndex)

                // Global stats
                put("globalTotalCoinsEarned", state.globalTotalCoinsEarned)
                put("globalTotalTaps", state.globalTotalTaps)
                put("globalTotalGeneratorsBought", state.globalTotalGeneratorsBought)
                put("totalPrestigesPerformed", state.totalPrestigesPerformed)
                put("globalTotalCriticalHits", state.globalTotalCriticalHits)

                put("generators", JSONArray().apply {
                    for (gen in state.generators) {
                        put(JSONObject().apply {
                            put("id", gen.id)
                            put("owned", gen.owned)
                            put("level", gen.level)
                            put("productionMultiplier", gen.productionMultiplier)
                            put("unlocked", gen.isUnlocked)
                            put("workers", gen.workers)
                            put("localSizeM2", gen.localSizeM2)
                            put("locationTier", gen.locationTier)
                            put("branches", gen.branches)
                        })
                    }
                })

                put("upgrades", JSONArray().apply {
                    for (upg in state.upgrades) {
                        put(JSONObject().apply {
                            put("currentLevel", upg.currentLevel)
                        })
                    }
                })

                put("achievements", JSONArray().apply {
                    for (ach in state.achievements) {
                        put(JSONObject().apply {
                            put("unlocked", ach.isUnlocked)
                            put("unlockedTimestamp", ach.unlockedTimestamp)
                        })
                    }
                })

                put("worlds", JSONArray().apply {
                    for (world in state.worlds) {
                        put(JSONObject().apply {
                            put("unlocked", world.isUnlocked)
                        })
                    }
                })

                put("miniGames", JSONArray().apply {
                    for (game in state.miniGames) {
                        put(JSONObject().apply {
                            put("timesPlayedToday", game.timesPlayedToday)
                            put("cooldownEndTime", game.cooldownEndTime)
                        })
                    }
                })

                put("lastMissionResetTime", state.lastMissionResetTime)
                put("dailyMissions", JSONArray().apply {
                    for (mission in state.dailyMissions) {
                        put(JSONObject().apply {
                            put("id", mission.id)
                            put("type", mission.type.name)
                            put("description", mission.description)
                            put("targetValue", mission.targetValue)
                            put("currentProgress", mission.currentProgress)
                            put("coinReward", mission.coinReward)
                            put("bonusReward", mission.bonusReward)
                            put("completed", mission.isCompleted)
                            put("claimed", mission.isClaimed)
                            put("difficulty", mission.difficulty)
                        })
                    }
                })

                // Workers
                put("lastSalaryPayTime", state.lastSalaryPayTime)
                put("workers", JSONArray().apply {
                    for (w in state.ownedWorkers) {
                        put(JSONObject().apply {
                            put("id", w.id)
                            put("name", w.name)
                            put("workerClass", w.workerClass.name)
                            put("rarity", w.rarity.name)
                            put("role", w.role.name)
                            put("yearsExperience", w.yearsExperience)
                            put("baseSalary", w.baseSalary)
                            put("productivity", w.productivity)
                            put("mood", w.mood.name)
                            put("maritalStatus", w.maritalStatus)
                            put("numberOfChildren", w.numberOfChildren)
                            put("assignedGeneratorId", w.assignedGeneratorId ?: "")
                            put("contractDurationMonths", w.contractDurationMonths)
                            put("strikeRights", w.hasStrikeRights())
                            put("unpaidTicks", w.unpaidTicks)
                            put("contractSalary", w.contractSalary)
                            put("contractSchedule", w.contractSchedule ?: "full_time")
                            put("contractCategory", w.contractCategory ?: "standard")
                            put("contractSigned", w.isContractSigned)
                            put("education", w.education ?: "None")
                        })
                    }
                })

                // Contract templates
                put("contractTemplates", JSONArray().apply {
                    for (ct in state.contractTemplates) {
                        put(JSONObject().apply {
                            put("id", ct.id)
                            put("name", ct.name)
                            put("salary", ct.salary)
                            put("schedule", ct.schedule)
                            put("category", ct.category)
                            put("strikeRights", ct.strikeRights)
                            put("durationMonths", ct.durationMonths)
                        })
                    }
                })

                put("activePetIndex", state.activePetIndex)
                put("pets", JSONArray().apply {
                    for (pet in state.pets) {
                        put(JSONObject().apply {
                            put("owned", pet.isOwned)
                            put("alive", pet.isAlive)
                            put("ghost", pet.isGhost)
                            put("level", pet.level)
                            put("experience", pet.experience)
                            put("experienceToNextLevel", pet.experienceToNextLevel)
                            put("customName", pet.customName ?: "")
                            put("mood", pet.mood.name)
                            put("health", pet.health)
                            put("happiness", pet.happiness)
                            put("hunger", pet.hunger)
                            put("energy", pet.energy)
                            put("mentalHealth", pet.mentalHealth)
                            put("hygiene", pet.hygiene)
                            put("birthTime", pet.birthTime)
                            put("deathTime", pet.deathTime)
                            put("lastFedTime", pet.lastFedTime)
                            put("lastPetTime", pet.lastPetTime)
                            put("lastPlayTime", pet.lastPlayTime)
                            put("lastCleanTime", pet.lastCleanTime)
                            put("lastCheckTime", pet.lastCheckTime)
                            put("diseases", JSONArray().apply {
                                for (disease in pet.diseases) {
                                    put(disease.name)
                                }
                            })
                            // Genetic fields
                            put("petId", pet.petId ?: "")
                            put("generation", pet.generation)
                            put("hybrid", pet.isHybrid)
                            put("parentId1", pet.parentId1 ?: "")
                            put("parentId2", pet.parentId2 ?: "")
                            put("lastBreedTime", pet.lastBreedTime)
                            put("petTypeOrdinal", pet.type.ordinal)
                            put("incubating", pet.isIncubating)
                            put("incubationEndTime", pet.incubationEndTime)
                            put("hadMutation", pet.hadMutation())
                            put("traits", JSONArray().apply {
                                for (trait in pet.traits) {
                                    put(trait.name)
                                }
                            })
                        })
                    }
                })

                // Nursery welfare
                put("nurseryWelfareLevel", state.nurseryWelfareLevel)
                put("lastWelfareDecayTime", state.lastWelfareDecayTime)
                put("totalPetsBred", state.totalPetsBred)
                put("totalRareMutations", state.totalRareMutations)
                put("totalStrikesResolved", state.totalStrikesResolved)
                put("maxComboReached", state.maxComboReached)

                // Events
                put("galacticStability", state.galacticStability)
                put("lastMinorEventTime", state.lastMinorEventTime)
                put("lastMajorEventTime", state.lastMajorEventTime)
                put("activeGameEvents", JSONArray().apply {
                    for (event in state.activeGameEvents) {
                        put(JSONObject().apply {
                            put("id", event.id)
                            put("type", event.type.name)
                            put("durationMs", event.durationMs)
                            put("startTime", event.startTime)
                            put("active", event.active)
                            put("resolved", event.isResolved)
                            put("autoResolved", event.isAutoResolved)
                            put("choiceIndex", event.choiceIndex)
                        })
                    }
                })
                put("eventHistory", JSONArray().apply {
                    for (event in state.eventHistory) {
                        put(JSONObject().apply {
                            put("id", event.id)
                            put("type", event.type.name)
                            put("durationMs", event.durationMs)
                            put("startTime", event.startTime)
                            put("active", event.active)
                            put("resolved", event.isResolved)
                            put("autoResolved", event.isAutoResolved)
                            put("choiceIndex", event.choiceIndex)
                        })
                    }
                })

                // Monetization
                put("monetization", JSONObject().apply {
                    val m = state.monetization
                    put("gems", m.gems)
                    put("starterPackPurchased", m.isStarterPackPurchased)
                    put("starterPackExpired", m.isStarterPackExpired)
                    put("firstSessionTime", m.firstSessionTime)
                    put("totalSpentCents", m.totalSpentCents)
                    put("lastDailyGemClaim", m.lastDailyGemClaim)
                    put("whaleUnlocked", m.isWhaleUnlocked)
                    put("exclusiveMultiplierActive", m.isExclusiveMultiplierActive)
                    put("exclusiveMultiplierExpiry", m.exclusiveMultiplierExpiry)
                    put("productionBoostActive", m.isProductionBoostActive)
                    put("productionBoostExpiry", m.productionBoostExpiry)
                    put("productionBoostMult", m.productionBoostMult)
                    put("autoTapActive", m.isAutoTapActiveRaw)
                    put("autoTapExpiry", m.autoTapExpiry)
                    put("purchaseTimestamps", JSONArray().apply {
                        for (ts in m.purchaseTimestamps) put(ts)
                    })
                })
            }

            prefs.edit()
                .putString(K_GAME_DATA, json.toString())
                .putLong(K_LAST_SAVE, System.currentTimeMillis())
                .apply()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadState() {
        val jsonData = prefs.getString(K_GAME_DATA, null) ?: return

        try {
            val json = JSONObject(jsonData)

            state.coins = json.optDouble("coins", 0.0)
            state.totalCoinsEarned = json.optDouble("totalCoinsEarned", 0.0)
            state.totalTaps = json.optLong("totalTaps", 0)
            state.prestigeLevel = json.optInt("prestigeLevel", 0)
            state.prestigePoints = json.optDouble("prestigePoints", 0.0)
            state.currentWorldIndex = json.optInt("currentWorldIndex", 0)
            state.lastUpdateTime = System.currentTimeMillis()

            // Global stats
            state.globalTotalCoinsEarned = json.optDouble("globalTotalCoinsEarned", state.totalCoinsEarned)
            state.globalTotalTaps = json.optLong("globalTotalTaps", state.totalTaps)
            state.globalTotalGeneratorsBought = json.optInt("globalTotalGeneratorsBought", 0)
            state.totalPrestigesPerformed = json.optInt("totalPrestigesPerformed", 0)
            state.globalTotalCriticalHits = json.optLong("globalTotalCriticalHits", 0)

            json.optJSONArray("generators")?.let { arr ->
                val gens = state.generators
                // Build id -> generator map for resilient loading
                val genMap = gens.associateBy { it.id }
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val id = obj.optString("id", "")
                    val gen = if (id.isNotEmpty()) genMap[id] else gens.getOrNull(i)
                    if (gen != null) {
                        gen.owned = obj.optInt("owned", 0)
                        gen.level = obj.optInt("level", 1)
                        gen.productionMultiplier = obj.optDouble("productionMultiplier", 1.0)
                        gen.isUnlocked = obj.optBoolean("unlocked", false)
                        gen.workers = obj.optInt("workers", 0)
                        gen.localSizeM2 = obj.optInt("localSizeM2", 10)
                        gen.locationTier = obj.optInt("locationTier", 0)
                        gen.branches = obj.optInt("branches", 1)
                    }
                }
            }

            json.optJSONArray("upgrades")?.let { arr ->
                val upgs = state.upgrades
                for (i in 0 until minOf(arr.length(), upgs.size)) {
                    upgs[i].currentLevel = arr.getJSONObject(i).optInt("currentLevel", 0)
                }
            }

            json.optJSONArray("achievements")?.let { arr ->
                val achs = state.achievements
                for (i in 0 until minOf(arr.length(), achs.size)) {
                    val obj = arr.getJSONObject(i)
                    achs[i].isUnlocked = obj.optBoolean("unlocked", false)
                    achs[i].unlockedTimestamp = obj.optLong("unlockedTimestamp", 0)
                }
            }

            json.optJSONArray("worlds")?.let { arr ->
                val worlds = state.worlds
                for (i in 0 until minOf(arr.length(), worlds.size)) {
                    worlds[i].isUnlocked = arr.getJSONObject(i).optBoolean("unlocked", i == 0)
                }
            }

            json.optJSONArray("miniGames")?.let { arr ->
                val games = state.miniGames
                for (i in 0 until minOf(arr.length(), games.size)) {
                    val obj = arr.getJSONObject(i)
                    games[i].timesPlayedToday = obj.optInt("timesPlayedToday", 0)
                    val cooldownEnd = obj.optLong("cooldownEndTime", 0)
                    if (cooldownEnd > System.currentTimeMillis()) {
                        games[i].cooldownEndTime = cooldownEnd
                    }
                }
            }

            // Load daily missions
            val savedResetTime = json.optLong("lastMissionResetTime", 0)
            if (savedResetTime > 0) {
                state.lastMissionResetTime = savedResetTime
            }
            json.optJSONArray("dailyMissions")?.let { arr ->
                if (arr.length() > 0) {
                    val loadedMissions = mutableListOf<DailyMission>()
                    for (i in 0 until arr.length()) {
                        try {
                            val obj = arr.getJSONObject(i)
                            val typeStr = obj.optString("type", "")
                            val missionType = try {
                                DailyMission.MissionType.valueOf(typeStr)
                            } catch (_: Exception) { null }
                            if (missionType != null) {
                                val mission = DailyMission(
                                    obj.optString("id", "daily_$i"),
                                    missionType,
                                    obj.optDouble("targetValue", 1.0),
                                    obj.optDouble("coinReward", 1000.0),
                                    obj.optInt("difficulty", 1)
                                )
                                mission.currentProgress = obj.optDouble("currentProgress", 0.0)
                                if (obj.optBoolean("completed", false)) {
                                    mission.isCompleted = true
                                }
                                if (obj.optBoolean("claimed", false)) {
                                    mission.isClaimed = true
                                }
                                mission.bonusReward = obj.optDouble("bonusReward", 0.0)
                                loadedMissions.add(mission)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    if (loadedMissions.isNotEmpty()) {
                        state.dailyMissions = loadedMissions
                    }
                }
            }

            // Load pets
            json.optJSONArray("pets")?.let { arr ->
                val pets = state.pets
                // First restore existing pets (by index up to initial count)
                val initialCount = pets.size
                for (i in 0 until arr.length()) {
                    try {
                        val obj = arr.getJSONObject(i)
                        val pet: Pet
                        if (i < initialCount) {
                            pet = pets[i]
                        } else {
                            // This is a bred pet - create new one with correct type
                            val typeOrdinal = obj.optInt("petTypeOrdinal", 0)
                            val petType = Pet.PetType.entries[typeOrdinal.coerceIn(0, Pet.PetType.entries.size - 1)]
                            pet = Pet(petType)
                            pets.add(pet)
                        }
                        if (obj.optBoolean("owned", false)) {
                            pet.isOwned = true
                        }
                        pet.isAlive = obj.optBoolean("alive", true)
                        pet.isGhost = obj.optBoolean("ghost", false)
                        pet.level = obj.optInt("level", 1)
                        pet.experience = obj.optDouble("experience", 0.0)
                        pet.experienceToNextLevel = obj.optDouble("experienceToNextLevel", 100.0)
                        val nameStr = obj.optString("customName", "")
                        if (nameStr.isNotEmpty()) pet.customName = nameStr
                        try {
                            pet.mood = Pet.PetMood.valueOf(obj.optString("mood", "NORMAL"))
                        } catch (_: Exception) {}
                        pet.health = obj.optInt("health", 100)
                        pet.happiness = obj.optInt("happiness", 70)
                        pet.hunger = obj.optInt("hunger", 20)
                        pet.energy = obj.optInt("energy", 80)
                        pet.mentalHealth = obj.optInt("mentalHealth", 80)
                        pet.hygiene = obj.optInt("hygiene", 90)
                        pet.birthTime = obj.optLong("birthTime", System.currentTimeMillis())
                        pet.deathTime = obj.optLong("deathTime", 0)
                        pet.lastFedTime = obj.optLong("lastFedTime", System.currentTimeMillis())
                        pet.lastPetTime = obj.optLong("lastPetTime", 0)
                        pet.lastPlayTime = obj.optLong("lastPlayTime", 0)
                        pet.lastCleanTime = obj.optLong("lastCleanTime", System.currentTimeMillis())
                        pet.lastCheckTime = obj.optLong("lastCheckTime", System.currentTimeMillis())

                        // Load diseases
                        val diseasesArr = obj.optJSONArray("diseases")
                        if (diseasesArr != null && diseasesArr.length() > 0) {
                            val diseaseList = mutableListOf<Pet.Disease>()
                            for (d in 0 until diseasesArr.length()) {
                                try {
                                    diseaseList.add(Pet.Disease.valueOf(diseasesArr.getString(d)))
                                } catch (_: Exception) {}
                            }
                            pet.diseases = diseaseList
                        }

                        // Load genetic fields
                        val petIdStr = obj.optString("petId", "")
                        if (petIdStr.isNotEmpty()) pet.petId = petIdStr
                        pet.generation = obj.optInt("generation", 0)
                        pet.isHybrid = obj.optBoolean("hybrid", false)
                        val p1 = obj.optString("parentId1", "")
                        if (p1.isNotEmpty()) pet.parentId1 = p1
                        val p2 = obj.optString("parentId2", "")
                        if (p2.isNotEmpty()) pet.parentId2 = p2
                        pet.lastBreedTime = obj.optLong("lastBreedTime", 0)

                        // Load incubation
                        pet.isIncubating = obj.optBoolean("incubating", false)
                        pet.incubationEndTime = obj.optLong("incubationEndTime", 0)
                        pet.setHadMutation(obj.optBoolean("hadMutation", false))

                        // Load traits
                        val traitsArr = obj.optJSONArray("traits")
                        if (traitsArr != null && traitsArr.length() > 0) {
                            val traitList = mutableListOf<Pet.PetTrait>()
                            for (t in 0 until traitsArr.length()) {
                                try {
                                    traitList.add(Pet.PetTrait.valueOf(traitsArr.getString(t)))
                                } catch (_: Exception) {}
                            }
                            pet.traits = traitList
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            // Load nursery welfare
            state.nurseryWelfareLevel = json.optDouble("nurseryWelfareLevel", 70.0)
            state.lastWelfareDecayTime = json.optLong("lastWelfareDecayTime", System.currentTimeMillis())
            state.totalPetsBred = json.optInt("totalPetsBred", 0)
            state.totalRareMutations = json.optInt("totalRareMutations", 0)
            state.totalStrikesResolved = json.optInt("totalStrikesResolved", 0)
            state.maxComboReached = json.optInt("maxComboReached", 0)

            // Load events
            state.galacticStability = json.optDouble("galacticStability", 50.0)
            state.lastMinorEventTime = json.optLong("lastMinorEventTime", 0)
            state.lastMajorEventTime = json.optLong("lastMajorEventTime", 0)
            json.optJSONArray("activeGameEvents")?.let { arr ->
                val events = mutableListOf<GameEvent>()
                for (i in 0 until arr.length()) {
                    try {
                        val obj = arr.getJSONObject(i)
                        val type = GameEvent.EventType.valueOf(obj.getString("type"))
                        val event = GameEvent.create(type)
                        event.id = obj.getString("id")
                        event.durationMs = obj.getLong("durationMs")
                        event.startTime = obj.getLong("startTime")
                        event.active = obj.getBoolean("active")
                        event.isResolved = obj.getBoolean("resolved")
                        event.isAutoResolved = obj.optBoolean("autoResolved", false)
                        event.choiceIndex = obj.getInt("choiceIndex")
                        if (event.isActive) events.add(event)
                    } catch (_: Exception) {}
                }
                state.activeGameEvents = events
            }

            // Load event history
            json.optJSONArray("eventHistory")?.let { arr ->
                val history = mutableListOf<GameEvent>()
                for (i in 0 until arr.length()) {
                    try {
                        val obj = arr.getJSONObject(i)
                        val type = GameEvent.EventType.valueOf(obj.getString("type"))
                        val event = GameEvent.create(type)
                        event.id = obj.getString("id")
                        event.durationMs = obj.getLong("durationMs")
                        event.startTime = obj.getLong("startTime")
                        event.active = obj.getBoolean("active")
                        event.isResolved = obj.getBoolean("resolved")
                        event.isAutoResolved = obj.optBoolean("autoResolved", false)
                        event.choiceIndex = obj.getInt("choiceIndex")
                        history.add(event)
                    } catch (_: Exception) {}
                }
                state.eventHistory = history
            }

            // Restore active pet
            val activePetIdx = json.optInt("activePetIndex", -1)
            if (activePetIdx in 0 until state.pets.size && state.pets[activePetIdx].isOwned) {
                state.setActivePet(activePetIdx)
            }

            // Load workers
            state.lastSalaryPayTime = json.optLong("lastSalaryPayTime", System.currentTimeMillis())
            json.optJSONArray("workers")?.let { arr ->
                val loadedWorkers = mutableListOf<Worker>()
                for (i in 0 until arr.length()) {
                    try {
                        val obj = arr.getJSONObject(i)
                        val wClass = try { Worker.WorkerClass.valueOf(obj.getString("workerClass")) } catch (_: Exception) { Worker.WorkerClass.LABORER }
                        val wRarity = try { Worker.WorkerRarity.valueOf(obj.getString("rarity")) } catch (_: Exception) { Worker.WorkerRarity.COMMON }
                        val wRole = try { Worker.WorkerRole.valueOf(obj.getString("role")) } catch (_: Exception) { Worker.WorkerRole.JUNIOR }
                        val wMood = try { Worker.WorkerMood.valueOf(obj.optString("mood", "HAPPY")) } catch (_: Exception) { Worker.WorkerMood.HAPPY }
                        val assignedId = obj.optString("assignedGeneratorId", "").ifEmpty { null }
                        val worker = Worker(
                            obj.getString("id"),
                            obj.getString("name"),
                            wClass, wRarity, wRole,
                            obj.optInt("yearsExperience", 0),
                            obj.optInt("productivity", 50),
                            obj.optDouble("baseSalary", 10.0),
                            wMood,
                            obj.optString("maritalStatus", "Single"),
                            obj.optInt("numberOfChildren", 0),
                            assignedId,
                            obj.optInt("contractDurationMonths", 12),
                            obj.optBoolean("strikeRights", true),
                            obj.optInt("unpaidTicks", 0),
                            obj.optDouble("contractSalary", 0.0),
                            obj.optString("contractSchedule", "full_time"),
                            obj.optString("contractCategory", "standard"),
                            obj.optBoolean("contractSigned", false),
                            obj.optString("education", "None")
                        )
                        loadedWorkers.add(worker)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                state.ownedWorkers = loadedWorkers
            }

            // Load contract templates
            json.optJSONArray("contractTemplates")?.let { arr ->
                val templates = mutableListOf<Worker.ContractTemplate>()
                for (i in 0 until arr.length()) {
                    try {
                        val obj = arr.getJSONObject(i)
                        templates.add(Worker.ContractTemplate(
                            obj.getString("id"),
                            obj.getString("name"),
                            obj.optDouble("salary", 100.0),
                            obj.optString("schedule", "full_time"),
                            obj.optString("category", "standard"),
                            obj.optBoolean("strikeRights", true),
                            obj.optInt("durationMonths", 12)
                        ))
                    } catch (_: Exception) {}
                }
                state.contractTemplates = templates
            }

            // Load monetization
            json.optJSONObject("monetization")?.let { mJson ->
                val m = state.monetization
                m.gems = mJson.optInt("gems", 0)
                m.isStarterPackPurchased = mJson.optBoolean("starterPackPurchased", false)
                m.isStarterPackExpired = mJson.optBoolean("starterPackExpired", false)
                m.firstSessionTime = mJson.optLong("firstSessionTime", 0)
                m.totalSpentCents = mJson.optInt("totalSpentCents", 0)
                m.lastDailyGemClaim = mJson.optLong("lastDailyGemClaim", 0)
                m.isWhaleUnlocked = mJson.optBoolean("whaleUnlocked", false)
                m.isExclusiveMultiplierActive = mJson.optBoolean("exclusiveMultiplierActive", false)
                m.exclusiveMultiplierExpiry = mJson.optLong("exclusiveMultiplierExpiry", 0)
                m.isProductionBoostActive = mJson.optBoolean("productionBoostActive", false)
                m.productionBoostExpiry = mJson.optLong("productionBoostExpiry", 0)
                m.productionBoostMult = mJson.optDouble("productionBoostMult", 1.0)
                m.isAutoTapActive = mJson.optBoolean("autoTapActive", false)
                m.autoTapExpiry = mJson.optLong("autoTapExpiry", 0)
                mJson.optJSONArray("purchaseTimestamps")?.let { tsArr ->
                    val timestamps = mutableListOf<Long>()
                    for (i in 0 until tsArr.length()) {
                        timestamps.add(tsArr.getLong(i))
                    }
                    m.purchaseTimestamps = timestamps
                }
                // Ensure firstSessionTime is set if it was 0
                m.initFirstSession()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCleared() {
        super.onCleared()
        saveState()
    }

    companion object {
        private const val PREFS = "tap_empire_prefs"
        private const val K_GAME_DATA = "game_data"
        private const val K_LAST_SAVE = "last_save_time"
    }
}

// === DATA CLASSES ===

data class GameUiState(
    val coins: Double = 0.0,
    val totalCoinsEarned: Double = 0.0,
    val perTap: Double = 1.0,
    val perSecond: Double = 0.0,
    val totalTaps: Long = 0,
    val prestigeLevel: Int = 0,
    val prestigePoints: Double = 0.0,
    val prestigeMultiplier: Double = 1.0,
    val canPrestige: Boolean = false,
    val currentWorldIndex: Int = 0,
    val currentWorld: World? = null,
    val worlds: List<World> = emptyList(),
    val generators: List<Generator> = emptyList(),
    val upgrades: List<Upgrade> = emptyList(),
    val achievements: List<Achievement> = emptyList(),
    val miniGames: List<MiniGame> = emptyList(),
    val dailyMissions: List<DailyMission> = emptyList(),
    val missionSnapshots: List<MissionSnapshot> = emptyList(),
    val pets: List<Pet> = emptyList(),
    val activePet: Pet? = null,
    val comboCount: Int = 0,
    val comboMultiplier: Double = 1.0,
    val comboActive: Boolean = false,
    val comboTimePercent: Int = 0,
    val activeEvent: SpecialEvent? = null,
    val criticalChance: Double = 0.05,
    val criticalMultiplier: Double = 2.0,
    // Global stats (never reset)
    val globalTotalCoinsEarned: Double = 0.0,
    val globalTotalTaps: Long = 0,
    val globalTotalGeneratorsBought: Int = 0,
    val totalPrestigesPerformed: Int = 0,
    val globalTotalCriticalHits: Long = 0,
    val totalGeneratorsBought: Int = 0,
    val totalMiniGamesPlayed: Int = 0,
    val stateVersion: Int = 0,
    val ownedWorkers: List<Worker> = emptyList(),
    val totalMonthlySalary: Double = 0.0,
    val contractTemplates: List<Worker.ContractTemplate> = emptyList(),
    val workersUnlocked: Boolean = false,
    // Nursery
    val nurseryWelfareLevel: Double = 70.0,
    val nurseryWorkerCount: Int = 0,
    val nurseryWorkersNeeded: Int = 0,
    // Events
    val galacticStability: Double = 50.0,
    val activeGameEvents: List<GameEvent> = emptyList(),
    val eventHistory: List<GameEvent> = emptyList(),
    val maxComboReached: Int = 0,
    // Monetization
    val monetizationState: com.example.juego.ui.screens.MonetizationUiState = com.example.juego.ui.screens.MonetizationUiState()
)

data class MissionSnapshot(
    val id: String,
    val type: DailyMission.MissionType,
    val description: String,
    val targetValue: Double,
    val currentProgress: Double,
    val coinReward: Double,
    val bonusReward: Double,
    val completed: Boolean,
    val claimed: Boolean,
    val difficulty: Int,
    val progressPercent: Double
)

data class TapFeedback(
    val amount: Double,
    val isCritical: Boolean,
    val comboCount: Int,
    val comboMultiplier: Double
)
