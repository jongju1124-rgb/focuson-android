package com.focuson.app.ui.onboarding

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.focuson.app.R
import com.focuson.app.util.PermissionChecker

private data class PermItem(
    val key: String,
    val title: String,
    val description: String,
    val granted: Boolean,
    val required: Boolean,
    val needsRestrictedSettings: Boolean = false,
    val action: () -> Unit,
)

@Composable
fun PermissionScreen(onDone: () -> Unit) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    var refresh by remember { mutableStateOf(0) }
    var showRestrictedGuide by remember { mutableStateOf(false) }

    val notifLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { refresh++ },
    )

    fun rescan() { refresh++ }

    val items = remember(refresh) { buildPermItems(context, notifLauncher, ::rescan) }

    // 화면 복귀 시마다 권한 상태 재검사
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) refresh++
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val required = items.filter { it.required }
    val optional = items.filter { !it.required }
    val requiredGranted = required.count { it.granted }
    val requiredTotal = required.size
    val ready = requiredGranted == requiredTotal
    val progress = if (requiredTotal == 0) 1f else requiredGranted.toFloat() / requiredTotal
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(500),
        label = "perm_progress",
    )

    Scaffold { inner: PaddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(inner),
        ) {
            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(20.dp),
            ) {
                Text(
                    text = stringResource(R.string.onboarding_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.onboarding_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "필수 $requiredGranted/$requiredTotal 허용됨",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (ready) Color(0xFF0EA371)
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = if (ready) Color(0xFF0EA371) else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )

                Spacer(Modifier.height(20.dp))
                SectionLabel("필수")
                Spacer(Modifier.height(8.dp))
                required.forEach { item ->
                    PermCard(
                        item = item,
                        onAction = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            if (item.needsRestrictedSettings && !item.granted) {
                                showRestrictedGuide = true
                            } else {
                                item.action()
                            }
                        },
                    )
                    Spacer(Modifier.height(10.dp))
                }

                Spacer(Modifier.height(16.dp))
                SectionLabel("선택")
                Text(
                    "건너뛰어도 기본 기능은 동작해요.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                optional.forEach { item ->
                    PermCard(
                        item = item,
                        onAction = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            item.action()
                        },
                    )
                    Spacer(Modifier.height(10.dp))
                }
            }

            // 고정된 하단 바
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Button(
                        onClick = onDone,
                        enabled = ready,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                    ) {
                        Text(
                            if (ready) "계속하기" else "필수 권한 ${requiredTotal - requiredGranted}개 남음",
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }

    if (showRestrictedGuide) {
        RestrictedSettingsGuide(
            onOpenAppDetails = {
                context.startActivity(PermissionChecker.appDetailsIntent(context))
                showRestrictedGuide = false
            },
            onOpenAccessibility = {
                context.startActivity(PermissionChecker.accessibilityIntent())
                showRestrictedGuide = false
            },
            onDismiss = { showRestrictedGuide = false },
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun PermCard(item: PermItem, onAction: () -> Unit) {
    val granted = item.granted
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (granted)
                Color(0xFF0EA371).copy(alpha = 0.10f)
            else
                MaterialTheme.colorScheme.surface,
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        item.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (item.needsRestrictedSettings && !granted) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { onAction() }
                            .padding(vertical = 2.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.width(16.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "사이드로드 앱이라 Android 보안 정책상 2단계가 필요해요. 탭해서 가이드 보기",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
            Spacer(Modifier.width(12.dp))
            Box(contentAlignment = Alignment.Center) {
                if (granted) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "허용됨",
                        tint = Color(0xFF0EA371),
                        modifier = Modifier.padding(8.dp),
                    )
                } else {
                    OutlinedButton(
                        onClick = onAction,
                        shape = RoundedCornerShape(10.dp),
                    ) { Text(stringResource(R.string.perm_grant)) }
                }
            }
        }
    }
}

@Composable
private fun RestrictedSettingsGuide(
    onOpenAppDetails: () -> Unit,
    onOpenAccessibility: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("접근성 허용 3단계") },
        text = {
            Column {
                Text(
                    "사이드로드 앱은 Android 보안 정책상 접근성 권한이 기본 차단됩니다. 순서대로 따라 하세요.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(14.dp))

                StepRow(
                    number = "1",
                    title = "접근성 설정 열기 → 토글 ON 시도",
                    detail = "접근성 서비스 목록에서 이 앱을 찾아 토글을 켜면 \"제한된 설정\" 안내가 뜹니다. 확인 후 뒤로.",
                )
                Spacer(Modifier.height(10.dp))
                StepRow(
                    number = "2",
                    title = "앱 정보 → ⋮ 메뉴 → 제한된 설정 허용",
                    detail = "1단계를 거쳐야 ⋮(점 3개)가 생깁니다. 삼성은 화면 맨 위 오른쪽에 나타나요.",
                )
                Spacer(Modifier.height(10.dp))
                StepRow(
                    number = "3",
                    title = "다시 접근성 설정 → 토글 ON",
                    detail = "이번엔 정상적으로 켜집니다.",
                )

                Spacer(Modifier.height(14.dp))
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    Text(
                        "⋮ 메뉴가 안 보이면 1단계를 먼저 시도하세요. 토글을 시도해 봐야 메뉴가 활성화됩니다.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onOpenAccessibility) { Text("1. 접근성 설정 열기") }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDismiss) { Text("닫기") }
                TextButton(onClick = onOpenAppDetails) { Text("2. 앱 정보 열기") }
            }
        },
    )
}

@Composable
private fun StepRow(number: String, title: String, detail: String) {
    Row(verticalAlignment = Alignment.Top) {
        Surface(
            shape = RoundedCornerShape(percent = 50),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.width(28.dp).height(28.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    number,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun buildPermItems(
    context: Context,
    notifLauncher: androidx.activity.result.ActivityResultLauncher<String>,
    rescan: () -> Unit,
): List<PermItem> = listOf(
    PermItem(
        key = "notifications",
        title = "알림",
        description = "세션 진행 중 상단바에 타이머를 보여줘요.",
        granted = PermissionChecker.notificationsGranted(context),
        required = true,
        action = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else rescan()
        },
    ),
    PermItem(
        key = "overlay",
        title = "다른 앱 위에 표시",
        description = "차단된 앱을 열었을 때 전체화면 안내를 띄워요.",
        granted = PermissionChecker.overlayGranted(context),
        required = true,
        action = { context.startActivity(PermissionChecker.overlayIntent(context)) },
    ),
    PermItem(
        key = "usage",
        title = "사용정보 접근",
        description = "어떤 앱이 실행됐는지 감지합니다.",
        granted = PermissionChecker.usageAccessGranted(context),
        required = true,
        action = { context.startActivity(PermissionChecker.usageAccessIntent()) },
    ),
    PermItem(
        key = "accessibility",
        title = "접근성 서비스",
        description = "앱/웹사이트 차단의 핵심 권한. 입력한 글자는 저장하지 않아요.",
        granted = PermissionChecker.accessibilityGranted(context),
        required = true,
        needsRestrictedSettings = true,
        action = { context.startActivity(PermissionChecker.accessibilityIntent()) },
    ),
    PermItem(
        key = "exact_alarm",
        title = "정확한 알람",
        description = "예약한 세션을 제시간에 시작하고 싶을 때.",
        granted = PermissionChecker.exactAlarmGranted(context),
        required = false,
        action = { context.startActivity(PermissionChecker.exactAlarmIntent(context)) },
    ),
    PermItem(
        key = "battery",
        title = "배터리 최적화 제외",
        description = "백그라운드에서 서비스가 꺼지지 않게 해줘요. (삼성 기기 권장)",
        granted = PermissionChecker.batteryOptIgnored(context),
        required = false,
        action = { context.startActivity(PermissionChecker.batteryOptIntent(context)) },
    ),
)
