package com.example.juego.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.juego.GameState
import com.example.juego.R
import com.example.juego.Worker
import com.example.juego.ui.components.AnimatedBackground
import com.example.juego.ui.components.GlassCard
import com.example.juego.ui.theme.*
import com.example.juego.ui.viewmodel.GameUiState
import com.example.juego.ui.viewmodel.GameViewModel
import java.util.UUID

@Composable
fun ContractsScreen(
    viewModel: GameViewModel,
    uiState: GameUiState,
    modifier: Modifier = Modifier
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var showStrikeDialog by remember { mutableStateOf(false) }

    val strikingWorkers = uiState.ownedWorkers.filter { it.mood == Worker.WorkerMood.ON_STRIKE }

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedBackground(worldTheme = uiState.currentWorld?.theme)

        Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
            Text(
                stringResource(R.string.contracts_title),
                fontSize = 28.sp, fontWeight = FontWeight.Black, color = TextPrimary,
                modifier = Modifier.padding(24.dp, 16.dp, 24.dp, 8.dp)
            )

            // Strike alert banner
            if (strikingWorkers.isNotEmpty()) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).clickable { showStrikeDialog = true },
                    glassColor = NeonRed.copy(alpha = 0.15f), borderColor = NeonRed.copy(alpha = 0.4f), cornerRadius = 12.dp
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("✊", fontSize = 24.sp)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.contracts_strike_alert), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NeonRed)
                            Text(stringResource(R.string.contracts_strike_count, strikingWorkers.size), fontSize = 12.sp, color = TextMuted)
                        }
                        Icon(Icons.Default.ChevronRight, null, tint = NeonRed)
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            // Create new contract button
            Button(
                onClick = { showCreateDialog = true },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.contracts_create_new))
            }

            Spacer(Modifier.height(12.dp))

            // Existing contracts
            if (uiState.contractTemplates.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📋", fontSize = 48.sp)
                        Spacer(Modifier.height(12.dp))
                        Text(stringResource(R.string.contracts_empty), fontSize = 14.sp, color = TextMuted, textAlign = TextAlign.Center)
                    }
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.contractTemplates, key = { it.id }) { ct ->
                        ContractTemplateCard(ct, viewModel)
                    }
                }
            }
        }
    }

    // Create dialog
    if (showCreateDialog) {
        CreateContractDialog(
            currentProduction = uiState.perSecond,
            onDismiss = { showCreateDialog = false },
            onCreate = { template ->
                viewModel.addContractTemplate(template)
                showCreateDialog = false
            }
        )
    }

    // Strike resolution dialog
    if (showStrikeDialog && strikingWorkers.isNotEmpty()) {
        StrikeResolutionDialog(
            strikingWorkers = strikingWorkers,
            contracts = uiState.contractTemplates,
            onDismiss = { showStrikeDialog = false },
            onResolveAll = { template ->
                viewModel.resolveMassStrike(template)
                showStrikeDialog = false
            },
            onResolveSingle = { workerId, template ->
                viewModel.resolveWorkerStrike(workerId, template)
            }
        )
    }
}

@Composable
fun ContractTemplateCard(ct: Worker.ContractTemplate, viewModel: GameViewModel) {
    val scheduleLabel = when (ct.schedule) {
        "full_time" -> stringResource(R.string.contract_full_time)
        "part_time" -> stringResource(R.string.contract_part_time)
        "flexible" -> stringResource(R.string.contract_flexible)
        else -> ct.schedule
    }
    val categoryLabel = when (ct.category) {
        "standard" -> stringResource(R.string.contract_standard)
        "premium" -> stringResource(R.string.contract_premium)
        "executive" -> stringResource(R.string.contract_executive)
        else -> ct.category
    }

    GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 12.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(ct.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ContractChip("💰", "${GameState.fmt(ct.salary)}/mo")
                    ContractChip("🕐", scheduleLabel)
                    ContractChip("📂", categoryLabel)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ContractChip("📅", "${ct.durationMonths} ${stringResource(R.string.contract_months)}")
                    ContractChip(if (ct.strikeRights) "✊" else "🚫",
                        if (ct.strikeRights) stringResource(R.string.contract_strike_yes) else stringResource(R.string.contract_no_strike))
                }
            }
            IconButton(onClick = { viewModel.removeContractTemplate(ct.id) }) {
                Icon(Icons.Default.Delete, null, tint = NeonRed.copy(alpha = 0.6f))
            }
        }
    }
}

@Composable
private fun ContractChip(icon: String, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(icon, fontSize = 12.sp)
        Spacer(Modifier.width(3.dp))
        Text(label, fontSize = 10.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateContractDialog(
    currentProduction: Double,
    onDismiss: () -> Unit,
    onCreate: (Worker.ContractTemplate) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var salaryText by remember { mutableStateOf("${(currentProduction * 50).toLong()}") }
    var schedule by remember { mutableStateOf("full_time") }
    var category by remember { mutableStateOf("standard") }
    var strikeRights by remember { mutableStateOf(true) }
    var duration by remember { mutableIntStateOf(12) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.contracts_create_title), fontWeight = FontWeight.Bold, color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Name
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text(stringResource(R.string.contract_name)) },
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary, unfocusedTextColor = TextSecondary,
                        focusedBorderColor = NeonCyan, unfocusedBorderColor = GlassBorder,
                        focusedLabelColor = NeonCyan, unfocusedLabelColor = TextMuted
                    )
                )

                // Salary
                OutlinedTextField(
                    value = salaryText, onValueChange = { new -> salaryText = new.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("💰 ${stringResource(R.string.contract_salary)}") },
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = CoinGold, unfocusedTextColor = TextSecondary,
                        focusedBorderColor = NeonCyan, unfocusedBorderColor = GlassBorder,
                        focusedLabelColor = NeonCyan, unfocusedLabelColor = TextMuted
                    )
                )

                // Schedule
                Text(stringResource(R.string.contract_schedule), fontSize = 12.sp, color = TextMuted)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("full_time" to stringResource(R.string.contract_full_time),
                        "part_time" to stringResource(R.string.contract_part_time),
                        "flexible" to stringResource(R.string.contract_flexible)).forEach { (key, label) ->
                        FilterChip(
                            selected = schedule == key, onClick = { schedule = key },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Onyx, selectedContainerColor = NeonCyan.copy(0.2f),
                                labelColor = TextMuted, selectedLabelColor = NeonCyan)
                        )
                    }
                }

                // Category
                Text(stringResource(R.string.contract_category), fontSize = 12.sp, color = TextMuted)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("standard" to stringResource(R.string.contract_standard),
                        "premium" to stringResource(R.string.contract_premium),
                        "executive" to stringResource(R.string.contract_executive)).forEach { (key, label) ->
                        FilterChip(
                            selected = category == key, onClick = { category = key },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Onyx, selectedContainerColor = NeonPurple.copy(0.2f),
                                labelColor = TextMuted, selectedLabelColor = NeonPurple)
                        )
                    }
                }

                // Duration
                Text("📅 ${stringResource(R.string.contract_duration)}: $duration ${stringResource(R.string.contract_months)}", fontSize = 12.sp, color = TextMuted)
                Slider(
                    value = duration.toFloat(), onValueChange = { duration = it.toInt() },
                    valueRange = 3f..36f, steps = 10,
                    colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan)
                )

                // Strike rights
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.contract_strike_rights), fontSize = 12.sp, color = TextMuted, modifier = Modifier.weight(1f))
                    Switch(
                        checked = strikeRights, onCheckedChange = { strikeRights = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = NeonGreen, checkedTrackColor = NeonGreen.copy(0.3f))
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val salary = salaryText.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && salary > 0) {
                        onCreate(Worker.ContractTemplate(
                            UUID.randomUUID().toString().substring(0, 8),
                            name, salary, schedule, category, strikeRights, duration
                        ))
                    }
                },
                enabled = name.isNotBlank() && (salaryText.toDoubleOrNull() ?: 0.0) > 0,
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
            ) { Text(stringResource(R.string.contracts_create_btn), color = DeepSpace) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_cancel), color = TextMuted) } },
        containerColor = Onyx, titleContentColor = TextPrimary
    )
}

@Composable
fun StrikeResolutionDialog(
    strikingWorkers: List<Worker>,
    contracts: List<Worker.ContractTemplate>,
    onDismiss: () -> Unit,
    onResolveAll: (Worker.ContractTemplate) -> Unit,
    onResolveSingle: (String, Worker.ContractTemplate) -> Unit
) {
    var selectedTemplate by remember { mutableStateOf<Worker.ContractTemplate?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("✊ ${stringResource(R.string.contracts_strike_title)}", fontWeight = FontWeight.Bold, color = NeonRed) },
        text = {
            Column {
                Text(stringResource(R.string.contracts_strike_desc, strikingWorkers.size), fontSize = 13.sp, color = TextMuted)
                Spacer(Modifier.height(8.dp))

                // Show striking workers
                strikingWorkers.take(5).forEach { w ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                        Text(w.workerClass.emoji, fontSize = 16.sp)
                        Spacer(Modifier.width(6.dp))
                        Text(w.name, fontSize = 12.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                        Text("✊", fontSize = 12.sp)
                    }
                }
                if (strikingWorkers.size > 5) Text("+${strikingWorkers.size - 5} ${stringResource(R.string.contracts_strike_more)}", fontSize = 11.sp, color = TextMuted)

                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.contracts_strike_offer), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(Modifier.height(6.dp))

                if (contracts.isEmpty()) {
                    Text(stringResource(R.string.negotiate_no_contracts), fontSize = 12.sp, color = Warning)
                } else {
                    contracts.forEach { ct ->
                        val sel = selectedTemplate?.id == ct.id
                        Surface(
                            onClick = { selectedTemplate = ct },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = if (sel) NeonCyan.copy(0.15f) else Onyx.copy(0.5f)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(10.dp)) {
                                Text("📋", fontSize = 14.sp)
                                Spacer(Modifier.width(6.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(ct.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text("💰${GameState.fmt(ct.salary)}/mo", fontSize = 10.sp, color = CoinGold)
                                }
                                if (sel) Icon(Icons.Default.CheckCircle, null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { selectedTemplate?.let { onResolveAll(it) } },
                enabled = selectedTemplate != null,
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
            ) { Text(stringResource(R.string.contracts_strike_resolve_all), color = DeepSpace) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_cancel), color = TextMuted) } },
        containerColor = Onyx
    )
}
