package com.focuson.app.ui.custom

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.focuson.app.FocusOnApp
import com.focuson.app.R
import com.focuson.app.data.repo.BlockRuleRepository
import com.focuson.app.data.repo.InstalledApp
import com.focuson.app.domain.AppCategorizer
import com.focuson.app.domain.model.AppCategory
import com.focuson.app.domain.model.PresetMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private sealed interface PickerRow {
    data class Header(val title: String, val count: Int, val isSelected: Boolean) : PickerRow
    data class Item(val app: InstalledApp, val isChecked: Boolean) : PickerRow
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppPickerScreen(
    mode: PresetMode,
    onDone: () -> Unit,
) {
    val app = remember { FocusOnApp.instance }
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current

    val appsFlow = remember { MutableStateFlow<List<InstalledApp>>(emptyList()) }
    val selected = remember { mutableStateOf(setOf<String>()) }
    var initialSelection by remember { mutableStateOf(setOf<String>()) }
    var loaded by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    LaunchedEffect(mode.id) {
        val (installed, savedApps) = withContext(Dispatchers.IO) {
            app.blockRuleRepository.seedIfEmpty(mode)
            val rules = app.blockRuleRepository.findMode(mode.id)
            val savedSet = rules
                .filter { it.kind == BlockRuleRepository.KIND_APP && it.enabled }
                .map { it.value }
                .toSet()
            val installedApps = app.appRepository.listInstalledApps(includeSystem = false)
            installedApps to savedSet
        }
        appsFlow.value = installed
        selected.value = savedApps
        initialSelection = savedApps
        loaded = true
    }

    val apps by appsFlow.collectAsState()
    var query by remember { mutableStateOf("") }

    // 필터링
    val filtered = remember(apps, query) {
        if (query.isBlank()) apps
        else apps.filter {
            it.label.contains(query, ignoreCase = true) ||
                it.packageName.contains(query, ignoreCase = true)
        }
    }

    // 섹션화: [선택됨 헤더 + 아이템들] + [카테고리별 (order 순) 헤더 + 아이템들]
    val rows: List<PickerRow> = remember(filtered, selected.value) {
        buildList {
            val checkedApps = filtered.filter { selected.value.contains(it.packageName) }
                .sortedBy { it.label.lowercase() }
            val uncheckedApps = filtered.filter { !selected.value.contains(it.packageName) }

            if (checkedApps.isNotEmpty()) {
                add(PickerRow.Header("차단 중", checkedApps.size, isSelected = true))
                checkedApps.forEach { add(PickerRow.Item(it, isChecked = true)) }
            }

            val byCategory = uncheckedApps.groupBy { AppCategorizer.categorize(it.packageName) }
            AppCategory.entries.sortedBy { it.order }.forEach { cat ->
                val list = byCategory[cat]?.sortedBy { it.label.lowercase() } ?: return@forEach
                if (list.isEmpty()) return@forEach
                add(PickerRow.Header("${cat.emoji} ${cat.displayName}", list.size, isSelected = false))
                list.forEach { add(PickerRow.Item(it, isChecked = false)) }
            }
        }
    }

    val hasUnsavedChanges = loaded && selected.value != initialSelection
    BackHandler(enabled = hasUnsavedChanges) { showDiscardDialog = true }

    val whitelist = mode.whitelistMode
    val titleRes = if (whitelist) R.string.app_picker_title_allow else R.string.app_picker_title
    val hintRes = if (whitelist) R.string.app_picker_hint_allow else R.string.app_picker_hint_block
    val countRes = if (whitelist) R.string.app_picker_allowed_count else R.string.app_picker_selected_count
    val chipColor = if (whitelist) MaterialTheme.colorScheme.tertiaryContainer else mode.color

    fun toggle(pkg: String) {
        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        val next = selected.value.toMutableSet().apply {
            if (contains(pkg)) remove(pkg) else add(pkg)
        }
        selected.value = next
    }

    val listState = rememberLazyListState()

    Scaffold { inner: PaddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(inner).padding(16.dp)) {
            Text(
                stringResource(titleRes),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(hintRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text(stringResource(R.string.app_picker_search_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
            )
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            stringResource(countRes, selected.value.size),
                            fontWeight = FontWeight.SemiBold,
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = chipColor.copy(alpha = 0.18f),
                        labelColor = MaterialTheme.colorScheme.onSurface,
                    ),
                    border = null,
                )
                Spacer(Modifier.weight(1f))
                if (selected.value.isNotEmpty()) {
                    Text(
                        "모두 해제",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                showClearDialog = true
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Box(modifier = Modifier.weight(1f)) {
                if (!loaded) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "앱 목록을 읽는 중…",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else if (rows.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            if (query.isBlank()) "설치된 앱이 없어요." else "검색 결과가 없어요.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    LazyColumn(state = listState) {
                        for (row in rows) {
                            when (row) {
                                is PickerRow.Header -> stickyHeader(key = "h-${row.title}") {
                                    SectionHeader(
                                        title = row.title,
                                        count = row.count,
                                        highlight = row.isSelected,
                                        accentColor = chipColor,
                                    )
                                }
                                is PickerRow.Item -> item(key = "p-${row.app.packageName}") {
                                    AppRow(
                                        app = row.app,
                                        isChecked = row.isChecked,
                                        onToggle = { toggle(row.app.packageName) },
                                    )
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    if (!loaded) return@Button
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    scope.launch {
                        app.blockRuleRepository.replaceApps(mode.id, selected.value)
                        onDone()
                    }
                },
                enabled = loaded,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(54.dp),
            ) {
                Text(
                    if (hasUnsavedChanges) stringResource(R.string.action_save) else "변경사항 없음",
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("모두 해제할까요?") },
            text = { Text("선택한 ${selected.value.size}개 앱의 체크가 모두 풀립니다. 아직 저장 전이에요.") },
            confirmButton = {
                TextButton(onClick = {
                    selected.value = emptySet()
                    showClearDialog = false
                }) { Text("모두 해제") }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("취소") }
            },
        )
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("저장하지 않고 나갈까요?") },
            text = { Text("변경한 체크 상태가 사라집니다.") },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardDialog = false
                    onDone()
                }) { Text("나가기") }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) { Text("계속 편집") }
            },
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    count: Int,
    highlight: Boolean,
    accentColor: androidx.compose.ui.graphics.Color,
) {
    Surface(
        color = if (highlight) accentColor.copy(alpha = 0.16f)
        else MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (highlight) accentColor.copy(alpha = 0.12f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                )
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (highlight) {
                Text(
                    "✅",
                    style = MaterialTheme.typography.labelLarge,
                )
                Spacer(Modifier.width(6.dp))
            }
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (highlight) accentColor
                else MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "$count",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AppRow(
    app: InstalledApp,
    isChecked: Boolean,
    onToggle: () -> Unit,
) {
    val repo = remember { FocusOnApp.instance.appRepository }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(horizontal = 4.dp, vertical = 10.dp),
    ) {
        val icon = remember(app.packageName) { repo.iconFor(app.packageName) }
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(40.dp),
        ) {
            if (icon != null) {
                Image(
                    bitmap = icon.toBitmap(96, 96).asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.padding(4.dp),
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        app.label.firstOrNull()?.toString() ?: "?",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                app.label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
            Text(
                app.packageName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Checkbox(
            checked = isChecked,
            onCheckedChange = { onToggle() },
        )
    }
}
