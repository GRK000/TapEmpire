package com.example.juego.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.juego.GameState
import com.example.juego.Worker
import com.example.juego.WorkerBox
import com.example.juego.R
import com.example.juego.ui.components.AnimatedBackground
import com.example.juego.ui.components.GlassCard
import com.example.juego.ui.theme.*
import com.example.juego.ui.viewmodel.GameUiState
import com.example.juego.ui.viewmodel.GameViewModel
import java.util.UUID

@Composable
fun WorkersScreen(
    viewModel: GameViewModel,
    uiState: GameUiState,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf(
        stringResource(R.string.workers_tab_roster),
        stringResource(R.string.workers_tab_hire),
        stringResource(R.string.workers_tab_summary),
        stringResource(R.string.workers_tab_collection)
    )

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedBackground(worldTheme = uiState.currentWorld?.theme)

        Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
            Text(
                text = stringResource(R.string.workers_title),
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary,
                modifier = Modifier.padding(24.dp, 16.dp, 24.dp, 8.dp)
            )

            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Onyx.copy(alpha = 0.7f),
                contentColor = NeonCyan,
                edgePadding = 8.dp,
                indicator = { tabPositions ->
                    if (selectedTab < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = NeonCyan
                        )
                    }
                }
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(title, fontSize = 13.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) NeonCyan else TextMuted)
                        }
                    )
                }
            }

            when (selectedTab) {
                0 -> WorkerRosterTab(viewModel, uiState)
                1 -> WorkerHireTab(viewModel, uiState)
                2 -> WorkerSummaryTab(uiState)
                3 -> WorkerCollectionTab(uiState)
            }
        }
    }
}

// ========================== ROSTER TAB ==========================

enum class WorkerSortMode { NAME, RARITY, PRODUCTIVITY, SALARY, MOOD }
enum class WorkerFilterMode { ALL, ASSIGNED, UNASSIGNED, ON_STRIKE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerRosterTab(viewModel: GameViewModel, uiState: GameUiState) {
    var selectedWorker by remember { mutableStateOf<Worker?>(null) }
    var showDetailWorker by remember { mutableStateOf<Worker?>(null) }
    var showAssignDialog by remember { mutableStateOf(false) }
    var showFireConfirm by remember { mutableStateOf(false) }
    var sortMode by remember { mutableStateOf(WorkerSortMode.RARITY) }
    var filterMode by remember { mutableStateOf(WorkerFilterMode.ALL) }
    var sortAscending by remember { mutableStateOf(false) }

    val filtered = uiState.ownedWorkers.filter { w ->
        when (filterMode) {
            WorkerFilterMode.ALL -> true
            WorkerFilterMode.ASSIGNED -> w.isAssigned
            WorkerFilterMode.UNASSIGNED -> !w.isAssigned
            WorkerFilterMode.ON_STRIKE -> w.mood == Worker.WorkerMood.ON_STRIKE
        }
    }
    val sorted = when (sortMode) {
        WorkerSortMode.NAME -> if (sortAscending) filtered.sortedBy { it.name } else filtered.sortedByDescending { it.name }
        WorkerSortMode.RARITY -> if (sortAscending) filtered.sortedBy { it.rarity.ordinal } else filtered.sortedByDescending { it.rarity.ordinal }
        WorkerSortMode.PRODUCTIVITY -> if (sortAscending) filtered.sortedBy { it.productivity } else filtered.sortedByDescending { it.productivity }
        WorkerSortMode.SALARY -> if (sortAscending) filtered.sortedBy { it.monthlySalary } else filtered.sortedByDescending { it.monthlySalary }
        WorkerSortMode.MOOD -> if (sortAscending) filtered.sortedBy { it.mood.ordinal } else filtered.sortedByDescending { it.mood.ordinal }
    }

    if (uiState.ownedWorkers.isEmpty()) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(32.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("👷", fontSize = 48.sp)
                Spacer(Modifier.height(16.dp))
                Text(stringResource(R.string.workers_empty), fontSize = 16.sp, color = TextMuted, textAlign = TextAlign.Center)
            }
        }
        return
    }

    Column {
        // Filter & Sort bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            WorkerFilterMode.values().forEach { mode ->
                val label = when (mode) {
                    WorkerFilterMode.ALL -> stringResource(R.string.filter_all)
                    WorkerFilterMode.ASSIGNED -> "📍"
                    WorkerFilterMode.UNASSIGNED -> "🔹"
                    WorkerFilterMode.ON_STRIKE -> "✊"
                }
                FilterChip(
                    selected = filterMode == mode,
                    onClick = { filterMode = mode },
                    label = { Text(label, fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Onyx, selectedContainerColor = NeonCyan.copy(alpha = 0.2f),
                        labelColor = TextMuted, selectedLabelColor = NeonCyan
                    ),
                    modifier = Modifier.height(28.dp)
                )
            }
            Spacer(Modifier.weight(1f))
            IconButton(onClick = {
                sortMode = WorkerSortMode.values()[(sortMode.ordinal + 1) % WorkerSortMode.values().size]
            }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.SwapVert, "Sort", tint = NeonCyan, modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = { sortAscending = !sortAscending }, modifier = Modifier.size(28.dp)) {
                Icon(if (sortAscending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    "Order", tint = TextMuted, modifier = Modifier.size(16.dp))
            }
        }

        Text("⇅ ${sortMode.name.lowercase().replaceFirstChar { it.uppercase() }} · ${sorted.size}",
            fontSize = 10.sp, color = TextMuted, modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp))

        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(sorted, key = { it.id }) { worker ->
                WorkerCard(worker, uiState.generators,
                    onTap = { showDetailWorker = worker },
                    onFire = { selectedWorker = worker; showFireConfirm = true })
            }
        }
    }

    // Worker detail dialog
    if (showDetailWorker != null) {
        WorkerDetailDialog(
            worker = showDetailWorker!!,
            generators = uiState.generators,
            onDismiss = { showDetailWorker = null },
            onAssign = { selectedWorker = showDetailWorker; showDetailWorker = null; showAssignDialog = true },
            onUnassign = { viewModel.unassignWorker(showDetailWorker!!.id); showDetailWorker = null },
            onFire = { selectedWorker = showDetailWorker; showDetailWorker = null; showFireConfirm = true }
        )
    }

    // Assign dialog
    if (showAssignDialog && selectedWorker != null) {
        val w = selectedWorker!!
        AlertDialog(
            onDismissRequest = { showAssignDialog = false },
            title = { Text("${w.workerClass.emoji} ${w.name}", fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = {
                Column {
                    Text(stringResource(R.string.workers_assign_to), color = TextMuted, fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))
                    uiState.generators.filter { it.isUnlocked && it.owned > 0 && (it.requiredWorldIndex == -1 || it.requiredWorldIndex <= uiState.currentWorldIndex) }
                        .forEach { gen ->
                            val gi = uiState.generators.indexOf(gen)
                            val cnt = uiState.ownedWorkers.count { it.assignedGeneratorId == gen.id }
                            val full = cnt >= gen.maxWorkerCapacity
                            Surface(onClick = { if (!full) { viewModel.assignWorker(w.id, gi); showAssignDialog = false } },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), shape = RoundedCornerShape(8.dp),
                                color = if (full) Disabled.copy(0.1f) else NeonPurple.copy(0.1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {
                                    Text(gen.emoji, fontSize = 20.sp); Spacer(Modifier.width(8.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(gen.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (full) TextMuted else TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text("$cnt/${gen.maxWorkerCapacity}", fontSize = 11.sp, color = if (full) NeonRed else NeonGreen)
                                    }
                                    if (full) Text("FULL", fontSize = 10.sp, color = NeonRed, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                }
            },
            confirmButton = { TextButton(onClick = { showAssignDialog = false }) { Text(stringResource(R.string.btn_cancel), color = TextMuted) } },
            containerColor = Onyx, titleContentColor = TextPrimary
        )
    }

    if (showFireConfirm && selectedWorker != null) {
        AlertDialog(onDismissRequest = { showFireConfirm = false },
            title = { Text(stringResource(R.string.workers_fire_title), color = NeonRed) },
            text = { Text(stringResource(R.string.workers_fire_confirm, selectedWorker!!.name), color = TextSecondary) },
            confirmButton = {
                Button(onClick = { viewModel.fireWorker(selectedWorker!!.id); showFireConfirm = false; selectedWorker = null },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonRed)) { Text(stringResource(R.string.workers_fire_btn)) }
            },
            dismissButton = { TextButton(onClick = { showFireConfirm = false }) { Text(stringResource(R.string.btn_cancel), color = TextMuted) } },
            containerColor = Onyx)
    }
}

// ========================== WORKER DETAIL DIALOG ==========================

@Composable
fun WorkerDetailDialog(
    worker: Worker,
    generators: List<com.example.juego.Generator>,
    onDismiss: () -> Unit,
    onAssign: () -> Unit,
    onUnassign: () -> Unit,
    onFire: () -> Unit
) {
    val rc = rarityColor(worker.rarity)
    val assignedGen = if (worker.isAssigned) generators.find { it.id == worker.assignedGeneratorId } else null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(contentAlignment = Alignment.Center,
                    modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(rc.copy(0.2f))) {
                    Text(worker.workerClass.emoji, fontSize = 24.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(worker.name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                    Text("${worker.rarity.icon} ${worker.rarity.name} · ${worker.role.name}", fontSize = 12.sp, color = rc)
                }
            }
        },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                // Stats section
                item {
                    Text(stringResource(R.string.detail_stats), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
                    Spacer(Modifier.height(4.dp))
                    DetailRow("⚡ ${stringResource(R.string.detail_productivity)}", "${worker.productivity}%")
                    DetailRow("📅 ${stringResource(R.string.detail_experience)}", "${worker.yearsExperience} ${stringResource(R.string.detail_years)}")
                    DetailRow("💰 ${stringResource(R.string.contract_salary)}", "${GameState.fmt(worker.monthlySalary)}/mo")
                    DetailRow("💰 ${stringResource(R.string.detail_base_salary)}", "${GameState.fmt(worker.baseSalary)}")
                    DetailRow(worker.mood.emoji + " " + stringResource(R.string.detail_mood), stringResource(when (worker.mood) {
                        Worker.WorkerMood.HAPPY -> R.string.worker_mood_happy
                        Worker.WorkerMood.NEUTRAL -> R.string.worker_mood_neutral
                        Worker.WorkerMood.ANGRY -> R.string.worker_mood_angry
                        Worker.WorkerMood.ON_STRIKE -> R.string.worker_mood_on_strike
                    }))
                }
                // Personal section
                item {
                    Spacer(Modifier.height(4.dp))
                    Text(stringResource(R.string.detail_personal), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NeonPurple)
                    Spacer(Modifier.height(4.dp))
                    DetailRow("💍 ${stringResource(R.string.detail_marital)}", worker.maritalStatus)
                    DetailRow("👶 ${stringResource(R.string.detail_children)}", "${worker.numberOfChildren}")
                    DetailRow("🏷️ ${stringResource(R.string.detail_class)}", "${worker.workerClass.emoji} ${worker.workerClass.name}")
                    DetailRow("🎓 ${stringResource(R.string.detail_education)}", worker.education ?: "None")
                }
                // Contract section
                item {
                    Spacer(Modifier.height(4.dp))
                    Text(stringResource(R.string.detail_contract), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NeonGreen)
                    Spacer(Modifier.height(4.dp))
                    if (worker.isContractSigned) {
                        DetailRow("📋 ${stringResource(R.string.detail_status)}", "✅ ${stringResource(R.string.detail_signed)}")
                        DetailRow("🕐 ${stringResource(R.string.contract_schedule)}", worker.contractSchedule?.replaceFirstChar { it.uppercase() } ?: "?")
                        DetailRow("📂 ${stringResource(R.string.contract_category)}", worker.contractCategory?.replaceFirstChar { it.uppercase() } ?: "?")
                        DetailRow("📅 ${stringResource(R.string.contract_duration)}", "${worker.contractDurationMonths} ${stringResource(R.string.contract_months)}")
                        DetailRow("✊ ${stringResource(R.string.contract_strike_rights)}", if (worker.hasStrikeRights()) "✅" else "❌")
                    } else {
                        DetailRow("📋 ${stringResource(R.string.detail_status)}", "❌ ${stringResource(R.string.detail_no_contract)}")
                    }
                }
                // Assignment
                item {
                    Spacer(Modifier.height(4.dp))
                    Text(stringResource(R.string.detail_assignment), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CoinGold)
                    Spacer(Modifier.height(4.dp))
                    if (assignedGen != null) {
                        DetailRow("📍 ${stringResource(R.string.detail_workplace)}", "${assignedGen.emoji} ${assignedGen.name}")
                    } else {
                        DetailRow("📍 ${stringResource(R.string.detail_workplace)}", stringResource(R.string.workers_unassigned))
                    }
                }
                // Action buttons
                item {
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        if (worker.isAssigned) {
                            Button(onClick = onUnassign, modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Warning)) {
                                Text(stringResource(R.string.workers_unassign), color = DeepSpace, fontSize = 12.sp)
                            }
                        } else {
                            Button(onClick = onAssign, modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)) {
                                Text(stringResource(R.string.workers_assign_to).take(10), color = Color.White, fontSize = 12.sp)
                            }
                        }
                        Button(onClick = onFire, modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonRed)) {
                            Text(stringResource(R.string.workers_fire_btn), fontSize = 12.sp)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_cancel), color = TextMuted) } },
        containerColor = Onyx
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 1.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 12.sp, color = TextMuted)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary, fontFamily = FontFamily.Monospace)
    }
}

// ========================== WORKER CARD ==========================

@Composable
fun WorkerCard(worker: Worker, generators: List<com.example.juego.Generator>, onTap: () -> Unit, onFire: () -> Unit) {
    val rc = rarityColor(worker.rarity)
    GlassCard(modifier = Modifier.fillMaxWidth().clickable { onTap() }, glassColor = rc.copy(0.06f), borderColor = rc.copy(0.3f), cornerRadius = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(rc.copy(0.15f))) { Text(worker.workerClass.emoji, fontSize = 24.sp) }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(worker.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, false))
                    Spacer(Modifier.width(6.dp)); Text(worker.rarity.icon, fontSize = 12.sp); Spacer(Modifier.width(4.dp)); Text(worker.mood.emoji, fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(worker.role.name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = rc,
                        modifier = Modifier.clip(RoundedCornerShape(3.dp)).background(rc.copy(0.12f)).padding(horizontal = 4.dp, vertical = 1.dp))
                    Spacer(Modifier.width(6.dp)); Text("${worker.productivity}%", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = NeonGreen)
                    Spacer(Modifier.width(6.dp)); Text("💰${GameState.fmt(worker.monthlySalary)}/mo", fontSize = 10.sp, color = CoinGold)
                }
                val ag = if (worker.isAssigned) generators.find { it.id == worker.assignedGeneratorId } else null
                Text(if (ag != null) "📍 ${ag.emoji} ${ag.name}" else stringResource(R.string.workers_unassigned), fontSize = 10.sp, color = if (ag != null) NeonCyan else TextMuted)
                if (worker.isContractSigned) {
                    Text("📋 ${worker.contractSchedule?.replaceFirstChar { it.uppercase() }} · ${worker.contractCategory?.replaceFirstChar { it.uppercase() }}",
                        fontSize = 9.sp, color = NeonGreen.copy(0.7f))
                }
            }
            IconButton(onClick = onFire, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Close, stringResource(R.string.workers_fire_btn), tint = NeonRed.copy(0.6f), modifier = Modifier.size(16.dp))
            }
        }
    }
}

private fun rarityColor(rarity: Worker.WorkerRarity): Color = when (rarity) {
    Worker.WorkerRarity.COMMON -> Color(0xFF9E9E9E)
    Worker.WorkerRarity.RARE -> Color(0xFF42A5F5)
    Worker.WorkerRarity.EPIC -> Color(0xFFAB47BC)
    Worker.WorkerRarity.LEGENDARY -> Color(0xFFFFA726)
}

// ========================== HIRE TAB (with inline contract creation + negotiation) ==========================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerHireTab(viewModel: GameViewModel, uiState: GameUiState) {
    var pendingWorker by remember { mutableStateOf<Worker?>(null) }
    var negotiationPhase by remember { mutableIntStateOf(0) }
    var negotiationResult by remember { mutableIntStateOf(-1) }
    var retryUsed by remember { mutableStateOf(false) }
    var selectedTemplate by remember { mutableStateOf<Worker.ContractTemplate?>(null) }
    var showInlineCreate by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item { Spacer(Modifier.height(8.dp)) }
        item { Text(stringResource(R.string.workers_hire_subtitle), fontSize = 13.sp, color = TextMuted, textAlign = TextAlign.Center) }
        item { Spacer(Modifier.height(16.dp)) }

        // Worker box cards
        item {
            WorkerBox.BoxType.values().forEach { boxType ->
                val cost = WorkerBox.getCost(boxType, uiState.perSecond)
                WorkerBoxCard(boxType, cost, uiState.coins >= cost, enabled = negotiationPhase == 0) {
                    val worker = viewModel.buyWorkerBox(boxType)
                    if (worker != null) { pendingWorker = worker; negotiationPhase = 1; retryUsed = false; negotiationResult = -1; selectedTemplate = null; showInlineCreate = false }
                }
                Spacer(Modifier.height(10.dp))
            }
        }

        // Negotiation flow
        when (negotiationPhase) {
            1 -> {
                item {
                    pendingWorker?.let { WorkerRevealCard(it) }
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { negotiationPhase = 2 },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)) {
                        Text(stringResource(R.string.negotiate_choose_contract))
                    }
                }
            }
            2, 4 -> {
                val isRetry = negotiationPhase == 4
                item {
                    Text(if (isRetry) "🔄 ${stringResource(R.string.negotiate_retry_title)}" else stringResource(R.string.negotiate_select_template),
                        fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isRetry) CoinGold else TextPrimary)
                    Spacer(Modifier.height(8.dp))
                }
                // Existing contract templates
                if (uiState.contractTemplates.isNotEmpty()) {
                    items(uiState.contractTemplates) { ct ->
                        val sel = selectedTemplate?.id == ct.id
                        GlassCard(modifier = Modifier.fillMaxWidth().clickable { selectedTemplate = ct; showInlineCreate = false },
                            glassColor = if (sel) NeonCyan.copy(0.15f) else GlassWhite,
                            borderColor = if (sel) NeonCyan.copy(0.4f) else GlassBorder, cornerRadius = 10.dp) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("📋", fontSize = 18.sp); Spacer(Modifier.width(8.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(ct.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text("💰${GameState.fmt(ct.salary)}/mo · ${ct.schedule} · ${ct.category}", fontSize = 10.sp, color = TextMuted)
                                    if (!ct.strikeRights) Text("⚠️ ${stringResource(R.string.contract_no_strike)}", fontSize = 9.sp, color = Warning)
                                }
                                if (sel) Icon(Icons.Default.CheckCircle, null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                }

                // Inline "create new contract" button + form
                item {
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { showInlineCreate = !showInlineCreate; selectedTemplate = null },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonGreen)
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.negotiate_create_inline), fontSize = 13.sp)
                    }
                }

                if (showInlineCreate) {
                    item {
                        InlineContractCreator(
                            recommendedSalary = pendingWorker?.monthlySalary ?: (uiState.perSecond * 50),
                            onCreated = { template ->
                                viewModel.addContractTemplate(template)
                                selectedTemplate = template
                                showInlineCreate = false
                            }
                        )
                    }
                }

                item {
                    Spacer(Modifier.height(12.dp))
                    if (selectedTemplate != null && pendingWorker != null) {
                        val prob = (pendingWorker!!.evaluateContract(selectedTemplate!!) * 100).toInt()
                        val probColor = when { prob >= 70 -> NeonGreen; prob >= 40 -> Warning; else -> NeonRed }
                        Text("📊 ${stringResource(R.string.negotiate_chance)}: $prob%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = probColor)
                        Spacer(Modifier.height(8.dp))
                    }
                    Button(onClick = {
                        if (selectedTemplate != null && pendingWorker != null) {
                            negotiationResult = viewModel.negotiateContract(pendingWorker!!, selectedTemplate!!)
                            negotiationPhase = 3
                        }
                    }, enabled = selectedTemplate != null,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)) {
                        Text(stringResource(R.string.negotiate_offer), color = DeepSpace)
                    }
                }
            }
            3 -> {
                item {
                    if (negotiationResult == 0) {
                        Text("✅ ${stringResource(R.string.negotiate_accepted)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = NeonGreen)
                        Spacer(Modifier.height(8.dp))
                        pendingWorker?.let { WorkerRevealCard(it) }
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { pendingWorker?.let { viewModel.hireWorker(it) }; pendingWorker = null; negotiationPhase = 0 },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)) { Text(stringResource(R.string.negotiate_welcome), color = DeepSpace) }
                    } else {
                        Text("❌ ${stringResource(R.string.negotiate_rejected)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = NeonRed)
                        Spacer(Modifier.height(4.dp))
                        Text(stringResource(R.string.negotiate_rejected_desc), fontSize = 12.sp, color = TextMuted, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(12.dp))
                        if (!retryUsed) {
                            Button(onClick = { retryUsed = true; negotiationPhase = 4; selectedTemplate = null; showInlineCreate = false },
                                colors = ButtonDefaults.buttonColors(containerColor = Warning)) { Text(stringResource(R.string.negotiate_retry), color = DeepSpace) }
                            Spacer(Modifier.height(6.dp))
                        }
                        Button(onClick = { pendingWorker = null; negotiationPhase = 0 },
                            colors = ButtonDefaults.buttonColors(containerColor = Disabled)) { Text(stringResource(R.string.negotiate_give_up)) }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(32.dp)) }
    }
}

// ========================== INLINE CONTRACT CREATOR ==========================

@Composable
fun InlineContractCreator(recommendedSalary: Double, onCreated: (Worker.ContractTemplate) -> Unit) {
    val recSalary = (recommendedSalary * 1.15).toLong() // 15% above base = recommended
    var name by remember { mutableStateOf("") }
    var salaryText by remember { mutableStateOf("$recSalary") }
    var schedule by remember { mutableStateOf("full_time") }
    var category by remember { mutableStateOf("standard") }
    var strikeRights by remember { mutableStateOf(true) }
    var duration by remember { mutableIntStateOf(12) }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        glassColor = NeonGreen.copy(0.06f), borderColor = NeonGreen.copy(0.25f), cornerRadius = 14.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("📝 ${stringResource(R.string.contracts_create_title)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NeonGreen)

            // Name
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text(stringResource(R.string.contract_name), fontSize = 12.sp) },
                singleLine = true, modifier = Modifier.fillMaxWidth(),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary, unfocusedTextColor = TextSecondary,
                    focusedBorderColor = NeonCyan, unfocusedBorderColor = GlassBorder,
                    focusedLabelColor = NeonCyan, unfocusedLabelColor = TextMuted
                )
            )

            // Salary with recommended indicator
            OutlinedTextField(
                value = salaryText, onValueChange = { new -> salaryText = new.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("💰 ${stringResource(R.string.contract_salary)}", fontSize = 12.sp) },
                singleLine = true, modifier = Modifier.fillMaxWidth(),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                supportingText = {
                    Text("⭐ ${stringResource(R.string.negotiate_recommended)}: ${GameState.fmt(recSalary.toDouble())}/mo",
                        fontSize = 12.sp, color = CoinGold, fontWeight = FontWeight.Bold)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = CoinGold, unfocusedTextColor = TextSecondary,
                    focusedBorderColor = NeonCyan, unfocusedBorderColor = GlassBorder,
                    focusedLabelColor = NeonCyan, unfocusedLabelColor = TextMuted
                )
            )

            // Schedule
            Text("🕐 ${stringResource(R.string.contract_schedule)} (⭐ ${stringResource(R.string.negotiate_recommended)}: ${stringResource(R.string.contract_full_time)})",
                fontSize = 12.sp, color = TextMuted, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("full_time" to stringResource(R.string.contract_full_time),
                    "part_time" to stringResource(R.string.contract_part_time),
                    "flexible" to stringResource(R.string.contract_flexible)).forEach { (key, label) ->
                    FilterChip(selected = schedule == key, onClick = { schedule = key },
                        label = { Text(label, fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Onyx, selectedContainerColor = NeonCyan.copy(0.2f),
                            labelColor = TextMuted, selectedLabelColor = NeonCyan),
                        modifier = Modifier.height(28.dp))
                }
            }

            // Category
            Text("📂 ${stringResource(R.string.contract_category)} (⭐ ${stringResource(R.string.negotiate_recommended)}: ${stringResource(R.string.contract_standard)})",
                fontSize = 12.sp, color = TextMuted, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("standard" to stringResource(R.string.contract_standard),
                    "premium" to stringResource(R.string.contract_premium),
                    "executive" to stringResource(R.string.contract_executive)).forEach { (key, label) ->
                    FilterChip(selected = category == key, onClick = { category = key },
                        label = { Text(label, fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Onyx, selectedContainerColor = NeonPurple.copy(0.2f),
                            labelColor = TextMuted, selectedLabelColor = NeonPurple),
                        modifier = Modifier.height(28.dp))
                }
            }

            // Duration
            Text("📅 $duration ${stringResource(R.string.contract_months)} (⭐ ${stringResource(R.string.negotiate_recommended)}: 12)",
                fontSize = 12.sp, color = TextMuted, fontWeight = FontWeight.Bold)
            Slider(value = duration.toFloat(), onValueChange = { duration = it.toInt() },
                valueRange = 3f..36f, steps = 10,
                colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan),
                modifier = Modifier.height(24.dp))

            // Strike rights
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("✊ ${stringResource(R.string.contract_strike_rights)} (⭐ ${stringResource(R.string.negotiate_recommended)}: ✅)",
                    fontSize = 12.sp, color = TextMuted, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Switch(checked = strikeRights, onCheckedChange = { strikeRights = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = NeonGreen, checkedTrackColor = NeonGreen.copy(0.3f)),
                    modifier = Modifier.height(24.dp))
            }

            Button(
                onClick = {
                    val salary = salaryText.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && salary > 0) {
                        onCreated(Worker.ContractTemplate(
                            UUID.randomUUID().toString().substring(0, 8),
                            name, salary, schedule, category, strikeRights, duration
                        ))
                    }
                },
                enabled = name.isNotBlank() && (salaryText.toDoubleOrNull() ?: 0.0) > 0,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
            ) { Text("✅ ${stringResource(R.string.negotiate_create_and_select)}", color = DeepSpace, fontSize = 13.sp) }
        }
    }
}

// ========================== BOX CARD & REVEAL ==========================

@Composable
fun WorkerBoxCard(boxType: WorkerBox.BoxType, cost: Double, canAfford: Boolean, enabled: Boolean = true, onBuy: () -> Unit) {
    val bc = when (boxType) { WorkerBox.BoxType.BASIC -> Color(0xFF78909C); WorkerBox.BoxType.PREMIUM -> Color(0xFF7C4DFF); WorkerBox.BoxType.LEGENDARY -> Color(0xFFFFA726) }
    val lr = when (boxType) { WorkerBox.BoxType.BASIC -> R.string.workers_box_basic; WorkerBox.BoxType.PREMIUM -> R.string.workers_box_premium; WorkerBox.BoxType.LEGENDARY -> R.string.workers_box_legendary }
    val dr = when (boxType) { WorkerBox.BoxType.BASIC -> R.string.workers_box_basic_desc; WorkerBox.BoxType.PREMIUM -> R.string.workers_box_premium_desc; WorkerBox.BoxType.LEGENDARY -> R.string.workers_box_legendary_desc }
    GlassCard(modifier = Modifier.fillMaxWidth(), glassColor = bc.copy(0.08f), borderColor = bc.copy(0.35f), cornerRadius = 16.dp) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(boxType.emoji, fontSize = 36.sp); Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(lr), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(stringResource(dr), fontSize = 11.sp, color = TextMuted)
            }
            Button(onClick = onBuy, enabled = canAfford && enabled,
                colors = ButtonDefaults.buttonColors(containerColor = if (canAfford && enabled) bc else Disabled, contentColor = Color.White),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)) {
                Text("💰 ${GameState.fmt(cost)}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun WorkerRevealCard(worker: Worker) {
    val rc = rarityColor(worker.rarity)
    val pa = rememberInfiniteTransition(label = "r"); val sc by pa.animateFloat(1f, 1.04f, infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "s")
    GlassCard(modifier = Modifier.fillMaxWidth().scale(sc), glassColor = rc.copy(0.12f), borderColor = rc.copy(0.5f), cornerRadius = 16.dp) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Text(stringResource(R.string.workers_new_hire), fontSize = 14.sp, color = CoinGold, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp)); Text(worker.workerClass.emoji, fontSize = 40.sp)
            Spacer(Modifier.height(4.dp)); Text(worker.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("${worker.rarity.icon} ${worker.rarity.name} • ${worker.role.name}", fontSize = 13.sp, color = rc)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                StatChip("⚡", "${worker.productivity}%"); StatChip("📅", "${worker.yearsExperience}y")
                StatChip("💰", "${GameState.fmt(worker.monthlySalary)}/mo"); StatChip("💍", worker.maritalStatus)
            }
            if (worker.education != null && worker.education != "None") {
                Spacer(Modifier.height(4.dp))
                Text("🎓 ${worker.education}", fontSize = 11.sp, color = NeonCyan.copy(0.8f))
            }
        }
    }
}

@Composable private fun StatChip(icon: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(icon, fontSize = 16.sp); Text(value, fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold) }
}

// ========================== SUMMARY TAB ==========================

@Composable
fun WorkerSummaryTab(uiState: GameUiState) {
    val ws = uiState.ownedWorkers
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 12.dp, glassColor = NeonCyan.copy(0.08f), borderColor = NeonCyan.copy(0.2f)) {
                Column {
                    Text(stringResource(R.string.workers_summary_overview), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(Modifier.height(8.dp))
                    SummaryRow("👷 ${stringResource(R.string.workers_summary_total)}", "${ws.size}")
                    SummaryRow("📍 ${stringResource(R.string.workers_summary_assigned)}", "${ws.count { it.isAssigned }} / ${ws.size}")
                    SummaryRow("💰 ${stringResource(R.string.workers_summary_salary)}", "${GameState.fmt(uiState.totalMonthlySalary)}/mo")
                }
            }
        }
        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 12.dp) {
                Column {
                    Text(stringResource(R.string.workers_summary_mood), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary); Spacer(Modifier.height(8.dp))
                    SummaryRow("😄 ${stringResource(R.string.worker_mood_happy)}", "${ws.count { it.mood == Worker.WorkerMood.HAPPY }}")
                    SummaryRow("😐 ${stringResource(R.string.worker_mood_neutral)}", "${ws.count { it.mood == Worker.WorkerMood.NEUTRAL }}")
                    SummaryRow("😠 ${stringResource(R.string.worker_mood_angry)}", "${ws.count { it.mood == Worker.WorkerMood.ANGRY }}")
                    SummaryRow("✊ ${stringResource(R.string.worker_mood_on_strike)}", "${ws.count { it.mood == Worker.WorkerMood.ON_STRIKE }}")
                }
            }
        }
        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 12.dp) {
                Column {
                    Text(stringResource(R.string.workers_summary_rarity), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary); Spacer(Modifier.height(8.dp))
                    Worker.WorkerRarity.values().forEach { r -> SummaryRow("${r.icon} ${r.name}", "${ws.count { it.rarity == r }}") }
                }
            }
        }
    }
}

@Composable private fun SummaryRow(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(label, fontSize = 13.sp, color = TextSecondary)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = CoinGold)
    }
}

// ========================== COLLECTION TAB ==========================

@Composable
fun WorkerCollectionTab(uiState: GameUiState) {
    val allFirstNames = remember { WorkerBox.getAllFirstNames() }
    val ownedNames = remember(uiState.ownedWorkers) {
        uiState.ownedWorkers.map { it.name.split(" ").firstOrNull() ?: "" }.toSet()
    }
    val ownedClasses = remember(uiState.ownedWorkers) {
        uiState.ownedWorkers.map { it.workerClass }.toSet()
    }
    val ownedRarities = remember(uiState.ownedWorkers) {
        uiState.ownedWorkers.map { it.rarity }.toSet()
    }

    val collectedCount = ownedNames.intersect(allFirstNames.toSet()).size
    val totalCount = allFirstNames.size

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Header
        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 14.dp,
                glassColor = CoinGold.copy(0.08f), borderColor = CoinGold.copy(0.25f)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("🏆 ${stringResource(R.string.collection_title)}", fontSize = 20.sp, fontWeight = FontWeight.Black, color = CoinGold)
                    Spacer(Modifier.height(8.dp))
                    Text("$collectedCount / $totalCount", fontSize = 28.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, color = TextPrimary)
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { collectedCount.toFloat() / totalCount.coerceAtLeast(1) },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = CoinGold, trackColor = Onyx
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("${(collectedCount * 100 / totalCount.coerceAtLeast(1))}% ${stringResource(R.string.collection_discovered)}",
                        fontSize = 12.sp, color = TextMuted)
                }
            }
        }

        // Classes collected
        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 12.dp) {
                Column {
                    Text("🏷️ ${stringResource(R.string.collection_classes)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Worker.WorkerClass.values().forEach { cls ->
                            val owned = cls in ownedClasses
                            Column(horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clip(RoundedCornerShape(8.dp))
                                    .background(if (owned) NeonCyan.copy(0.1f) else Onyx.copy(0.5f))
                                    .padding(8.dp)) {
                                Text(if (owned) cls.emoji else "❓", fontSize = 20.sp)
                                Text(if (owned) cls.name else "???", fontSize = 8.sp, color = if (owned) TextPrimary else TextMuted)
                            }
                        }
                    }
                }
            }
        }

        // Rarities collected
        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 12.dp) {
                Column {
                    Text("✨ ${stringResource(R.string.collection_rarities)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NeonPurple)
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Worker.WorkerRarity.values().forEach { r ->
                            val owned = r in ownedRarities
                            val rc = rarityColor(r)
                            Column(horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clip(RoundedCornerShape(8.dp))
                                    .background(if (owned) rc.copy(0.15f) else Onyx.copy(0.5f))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)) {
                                Text(if (owned) r.icon else "❓", fontSize = 20.sp)
                                Text(if (owned) r.name else "???", fontSize = 9.sp, color = if (owned) rc else TextMuted, fontWeight = FontWeight.Bold)
                                Text("${uiState.ownedWorkers.count { it.rarity == r }}", fontSize = 11.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            }
        }

        // Name catalog grid
        item {
            Text("📖 ${stringResource(R.string.collection_catalog)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CoinGold,
                modifier = Modifier.padding(vertical = 4.dp))
        }

        // Show names in grid-like rows
        val chunked = allFirstNames.toList().chunked(4)
        items(chunked) { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { firstName ->
                    val discovered = firstName in ownedNames
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (discovered) NeonGreen.copy(0.12f) else Onyx.copy(0.4f))
                            .padding(vertical = 6.dp, horizontal = 4.dp)
                    ) {
                        Text(
                            text = if (discovered) firstName else "???",
                            fontSize = 10.sp,
                            fontWeight = if (discovered) FontWeight.Bold else FontWeight.Normal,
                            color = if (discovered) TextPrimary else TextMuted.copy(0.4f),
                            maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                // Fill empty slots in last row
                repeat(4 - row.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(2.dp))
        }

        item { Spacer(Modifier.height(32.dp)) }
    }
}
