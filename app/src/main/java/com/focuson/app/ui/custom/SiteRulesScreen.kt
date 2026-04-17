package com.focuson.app.ui.custom

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.focuson.app.FocusOnApp
import com.focuson.app.R
import com.focuson.app.data.repo.BlockRuleRepository
import com.focuson.app.domain.model.PresetMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SiteRulesScreen(mode: PresetMode, onDone: () -> Unit) {
    val app = remember { FocusOnApp.instance }
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current

    var local by remember { mutableStateOf(listOf<String>()) }
    var initialList by remember { mutableStateOf(listOf<String>()) }
    var loaded by remember { mutableStateOf(false) }
    var input by remember { mutableStateOf("") }
    var pendingDelete by remember { mutableStateOf<String?>(null) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    LaunchedEffect(mode.id) {
        val sites = withContext(Dispatchers.IO) {
            app.blockRuleRepository.seedIfEmpty(mode)
            app.blockRuleRepository.findMode(mode.id)
                .filter { it.kind == BlockRuleRepository.KIND_SITE && it.enabled }
                .map { it.value }
        }
        local = sites
        initialList = sites
        loaded = true
    }

    val hasUnsavedChanges = loaded && local != initialList
    BackHandler(enabled = hasUnsavedChanges) { showDiscardDialog = true }

    fun addSite() {
        val v = input.trim().lowercase()
        if (v.isNotEmpty() && !local.contains(v)) {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            local = local + v
            input = ""
        }
    }

    Scaffold { inner: PaddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(inner).padding(16.dp)) {
            Text(
                stringResource(R.string.site_rules_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "도메인을 추가하면 세션 중 해당 사이트를 차단해요. 와일드카드는 *.example.com 처럼 씁니다.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    placeholder = { Text(stringResource(R.string.site_rules_hint)) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                OutlinedButton(
                    onClick = ::addSite,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.height(56.dp),
                ) { Text(stringResource(R.string.site_add)) }
            }
            Spacer(Modifier.height(12.dp))
            AssistChip(
                onClick = {},
                label = {
                    Text("${local.size}개 사이트", fontWeight = FontWeight.SemiBold)
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = mode.color.copy(alpha = 0.18f),
                    labelColor = MaterialTheme.colorScheme.onSurface,
                ),
                border = null,
            )
            Spacer(Modifier.height(8.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (!loaded) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (local.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "추가된 사이트가 없어요.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(local, key = { it }) { pattern ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                ),
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth().padding(start = 14.dp, end = 4.dp),
                                ) {
                                    Text(
                                        pattern,
                                        modifier = Modifier.weight(1f),
                                        style = MaterialTheme.typography.bodyLarge,
                                    )
                                    IconButton(onClick = {
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                        pendingDelete = pattern
                                    }) {
                                        Icon(
                                            Icons.Filled.Delete,
                                            contentDescription = "삭제",
                                            tint = MaterialTheme.colorScheme.error,
                                        )
                                    }
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
                        app.blockRuleRepository.replaceSites(mode.id, local)
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

    pendingDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("이 사이트를 삭제할까요?") },
            text = { Text(target) },
            confirmButton = {
                TextButton(onClick = {
                    local = local - target
                    pendingDelete = null
                }) { Text("삭제") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("취소") }
            },
        )
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("저장하지 않고 나갈까요?") },
            text = { Text("변경한 사이트 목록이 사라집니다.") },
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
