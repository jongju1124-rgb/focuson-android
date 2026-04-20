package com.focuson.app.ui.session

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focuson.app.domain.engine.BlockEngine
import com.focuson.app.domain.model.PresetMode
import kotlinx.coroutines.delay

@Composable
fun SessionScreen(onEndSession: (force: Boolean) -> Unit) {
    // 초 단위로만 상태 갱신 → 불필요한 recomposition 방지
    var nowSec by remember { mutableLongStateOf(System.currentTimeMillis() / 1000L) }
    val haptics = LocalHapticFeedback.current

    LaunchedEffect(Unit) {
        while (true) {
            val sec = System.currentTimeMillis() / 1000L
            if (sec != nowSec) nowSec = sec
            // 다음 초 경계까지 대기
            delay(1000L - (System.currentTimeMillis() % 1000L))
        }
    }
    val now = nowSec * 1000L

    val active = BlockEngine.active()
    if (active == null) {
        LaunchedEffect(Unit) { onEndSession(true) }
        Scaffold { inner: PaddingValues ->
            Column(
                modifier = Modifier.fillMaxSize().padding(inner),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator()
                Spacer(Modifier.height(16.dp))
                Text(
                    "수면 위로 올라왔어요. 홈으로 이동합니다…",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        return
    }

    val totalMs = (active.endEpochMs - active.startEpochMs).coerceAtLeast(1L)
    val remainingMs = active.remainingMillis(now)
    val progress = (1f - remainingMs.toFloat() / totalMs).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 600),
        label = "session_progress",
    )

    val totalSec = remainingMs / 1000
    val minutes = (totalSec / 60).toInt()
    val seconds = (totalSec % 60).toInt()
    val mode = PresetMode.fromId(active.modeId)
    val accent = mode?.color ?: MaterialTheme.colorScheme.primary

    Scaffold { inner: PaddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(inner).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = mode?.let { androidx.compose.ui.res.stringResource(it.displayNameRes) } ?: "잠수 중",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = mode?.let { androidx.compose.ui.res.stringResource(it.taglineRes) } ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(40.dp))
            Box(
                modifier = Modifier.size(260.dp),
                contentAlignment = Alignment.Center,
            ) {
                // 배경 링
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.size(260.dp),
                    color = accent.copy(alpha = 0.12f),
                    strokeWidth = 12.dp,
                    trackColor = Color.Transparent,
                )
                // 진행 링
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.size(260.dp),
                    color = accent,
                    strokeWidth = 12.dp,
                    trackColor = Color.Transparent,
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "%d:%02d".format(minutes, seconds),
                        fontSize = 72.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = accent,
                    )
                    Text(
                        "남음",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(48.dp))

            if (active.strict) {
                Text(
                    "🔒 엄격모드 — 시간이 다 될 때까지 수면 위로 올라올 수 없어요",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            } else {
                Button(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onEndSession(false)
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    ),
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                ) {
                    Text("수면 위로", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
