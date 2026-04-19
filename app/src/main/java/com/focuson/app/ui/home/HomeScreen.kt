package com.focuson.app.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.focuson.app.R
import com.focuson.app.domain.model.PresetMode

@Composable
fun HomeScreen(
    onStartPreset: (PresetMode, Int, Boolean) -> Unit,
    onOpenCustomize: (PresetMode) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenStats: () -> Unit = {},
    onOpenPro: () -> Unit = {},
) {
    var focusedMode by remember { mutableStateOf<PresetMode?>(null) }
    val haptics = LocalHapticFeedback.current
    var confirmStart by remember { mutableStateOf<Triple<PresetMode, Int, Boolean>?>(null) }

    Scaffold { inner: PaddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(inner).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = stringResource(R.string.home_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.home_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            PresetMode.entries.forEach { mode ->
                PresetCard(
                    mode = mode,
                    expanded = focusedMode == mode,
                    onToggle = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        focusedMode = if (focusedMode == mode) null else mode
                    },
                    onStart = { minutes, strict ->
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        confirmStart = Triple(mode, minutes, strict)
                    },
                    onCustomize = { onOpenCustomize(mode) },
                )
            }

            Spacer(Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onOpenStats,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).height(48.dp),
                ) { Text("📊 통계") }
                OutlinedButton(
                    onClick = onOpenPro,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).height(48.dp),
                ) { Text("💛 Pro") }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = onOpenSettings,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(44.dp),
            ) {
                Text(stringResource(R.string.action_settings))
            }
        }
    }

    confirmStart?.let { (mode, minutes, strict) ->
        AlertDialog(
            onDismissRequest = { confirmStart = null },
            title = { Text("${stringResource(mode.displayNameRes)} 시작") },
            text = {
                Column {
                    Text("${formatMinutes(minutes)} 동안 차단 세션을 시작합니다.")
                    if (strict) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "🔒 엄격모드 — 세션이 끝날 때까지 중간에 종료할 수 없어요.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onStartPreset(mode, minutes, strict)
                    confirmStart = null
                }) { Text("시작") }
            },
            dismissButton = {
                TextButton(onClick = { confirmStart = null }) { Text("취소") }
            },
        )
    }
}

@Composable
private fun PresetCard(
    mode: PresetMode,
    expanded: Boolean,
    onToggle: () -> Unit,
    onStart: (minutes: Int, strict: Boolean) -> Unit,
    onCustomize: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onToggle() },
        colors = CardDefaults.cardColors(containerColor = mode.color.copy(alpha = 0.12f)),
        border = BorderStroke(1.dp, mode.color.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = mode.color,
                    modifier = Modifier.height(48.dp).width(48.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            when (mode) {
                                PresetMode.STUDENT -> "📚"
                                PresetMode.WORKER -> "💼"
                                PresetMode.MEDITATION -> "🧘"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                        )
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(mode.displayNameRes),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        stringResource(mode.taglineRes),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(
                    animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
                ),
                exit = fadeOut() + shrinkVertically(
                    animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
                ),
            ) {
                Column {
                    Spacer(Modifier.height(18.dp))
                    var minutes by remember { mutableStateOf(mode.defaultDurationMin) }
                    var strict by remember { mutableStateOf(mode.strictByDefault) }
                    DurationRow(minutes, mode.color) { minutes = it }
                    Spacer(Modifier.height(12.dp))
                    StrictSwitchRow(strict) { strict = it }
                    Spacer(Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { onStart(minutes, strict) },
                            colors = ButtonDefaults.buttonColors(containerColor = mode.color),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(48.dp),
                        ) {
                            Text(stringResource(R.string.action_start), fontWeight = FontWeight.SemiBold)
                        }
                        OutlinedButton(
                            onClick = onCustomize,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(48.dp),
                        ) { Text("설정") }
                    }
                }
            }
        }
    }
}

@Composable
private fun DurationRow(current: Int, accent: Color, onChange: (Int) -> Unit) {
    val options = listOf(15, 30, 60, 120, 240, 480)
    val haptics = LocalHapticFeedback.current
    val isCustom = current !in options
    var showCustomDialog by remember { mutableStateOf(false) }

    Column {
        Text(stringResource(R.string.session_duration_label), style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        ) {
            options.forEach { opt ->
                val selected = opt == current && !isCustom
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (selected) accent else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        1.dp,
                        if (selected) accent else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    ),
                    modifier = Modifier.clickable {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onChange(opt)
                    },
                ) {
                    Text(
                        formatMinutes(opt),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    )
                }
            }
            // 직접 입력 칩
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isCustom) accent else MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    1.dp,
                    if (isCustom) accent else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                ),
                modifier = Modifier.clickable {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    showCustomDialog = true
                },
            ) {
                Text(
                    if (isCustom) "✏\uFE0F ${formatMinutes(current)}" else "✏\uFE0F 직접 입력",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    color = if (isCustom) Color.White else MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isCustom) FontWeight.SemiBold else FontWeight.Normal,
                )
            }
        }
    }

    if (showCustomDialog) {
        CustomMinutesDialog(
            initial = if (isCustom) current else 45,
            onConfirm = {
                onChange(it)
                showCustomDialog = false
            },
            onDismiss = { showCustomDialog = false },
        )
    }
}

private fun formatMinutes(m: Int): String = when {
    m < 60 -> "${m}분"
    m % 60 == 0 -> "${m / 60}시간"
    else -> "${m / 60}시간 ${m % 60}분"
}

@Composable
private fun CustomMinutesDialog(
    initial: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember { mutableStateOf(initial.toString()) }
    val parsed = text.toIntOrNull()
    val valid = parsed != null && parsed in 1..720
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("세션 시간 직접 입력") },
        text = {
            Column {
                Text(
                    "1분 ~ 720분(12시간) 사이로 입력해주세요.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { new -> text = new.filter { it.isDigit() }.take(3) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    trailingIcon = { Text("분", modifier = Modifier.padding(end = 14.dp)) },
                    isError = text.isNotEmpty() && !valid,
                    supportingText = {
                        if (!valid && text.isNotEmpty()) {
                            Text("1 ~ 720 사이 숫자만 가능합니다.")
                        } else if (parsed != null && parsed >= 60) {
                            Text(
                                "= ${formatMinutes(parsed)}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { if (valid) onConfirm(parsed!!) }, enabled = valid) {
                Text("확인")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        },
    )
}

@Composable
private fun StrictSwitchRow(value: Boolean, onChange: (Boolean) -> Unit) {
    val haptics = LocalHapticFeedback.current
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                stringResource(R.string.session_strict_label),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                stringResource(R.string.session_strict_warning),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.width(12.dp))
        Switch(
            checked = value,
            onCheckedChange = {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onChange(it)
            },
        )
    }
}
