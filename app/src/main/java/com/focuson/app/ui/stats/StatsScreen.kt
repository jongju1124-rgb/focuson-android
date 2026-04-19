package com.focuson.app.ui.stats

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.focuson.app.FocusOnApp
import com.focuson.app.data.db.FocusOnDatabase
import com.focuson.app.domain.model.ProTier
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar

private data class StatsData(
    val totalMs: Long,
    val completedCount: Int,
    val totalCount: Int,
    val dailyMs: List<Pair<Long, Long>>, // (dayStartMs, ms)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onBack: () -> Unit,
    onUpgradeClick: () -> Unit,
) {
    val context = LocalContext.current
    val app = FocusOnApp.instance
    val dao = remember { FocusOnDatabase.get(context).sessionDao() }
    val proTierId by app.settingsStore.proTierId.collectAsState(initial = "free")
    val tier = ProTier.fromId(proTierId)
    val hasFullStats = tier.atLeast(ProTier.PRO)

    // 무료: 지난 7일만 / Pro: 전체 기간 (최대 30일 그래프)
    val days = if (hasFullStats) 30 else 7
    var stats by remember { mutableStateOf<StatsData?>(null) }

    LaunchedEffect(days) {
        val now = System.currentTimeMillis()
        val since = now - days * 24L * 60L * 60L * 1000L
        val total = dao.totalFocusedMsSince(since)
        val completed = dao.completedCountSince(since)
        val all = dao.totalCountSince(since)
        val sessions = dao.findSince(since)
        // 일별 집계
        val byDay = mutableMapOf<Long, Long>()
        for (day in 0 until days) {
            val ms = since + day * 24L * 60L * 60L * 1000L
            byDay[startOfDay(ms)] = 0L
        }
        for (s in sessions) {
            val day = startOfDay(s.startEpochMs)
            val duration = (s.actualEndEpochMs ?: s.endEpochMs) - s.startEpochMs
            byDay[day] = (byDay[day] ?: 0L) + duration.coerceAtLeast(0L)
        }
        stats = StatsData(
            totalMs = total,
            completedCount = completed,
            totalCount = all,
            dailyMs = byDay.toSortedMap().map { it.key to it.value },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("집중 통계") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
            )
        },
    ) { inner: PaddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            val data = stats
            if (data == null) {
                Text("불러오는 중…", color = MaterialTheme.colorScheme.onSurfaceVariant)
                return@Column
            }

            // 요약 카드 3개
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SummaryCard(
                    label = "총 집중 시간",
                    value = formatHMS(data.totalMs),
                    modifier = Modifier.weight(1f),
                )
                SummaryCard(
                    label = "완주 세션",
                    value = "${data.completedCount}회",
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SummaryCard(
                    label = "총 세션",
                    value = "${data.totalCount}회",
                    modifier = Modifier.weight(1f),
                )
                SummaryCard(
                    label = "완주율",
                    value = if (data.totalCount == 0) "—"
                    else "${data.completedCount * 100 / data.totalCount}%",
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(24.dp))
            Text(
                "지난 ${days}일 일별 집중 시간",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(10.dp))
            BarChart(data.dailyMs)

            if (!hasFullStats) {
                Spacer(Modifier.height(24.dp))
                UpgradeHint(onUpgradeClick)
            }
        }
    }
}

@Composable
private fun SummaryCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun BarChart(data: List<Pair<Long, Long>>) {
    val max = (data.maxOfOrNull { it.second } ?: 0L).coerceAtLeast(60_000L)
    val accent = MaterialTheme.colorScheme.primary
    Row(
        modifier = Modifier.fillMaxWidth().height(120.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        for ((_, ms) in data) {
            val ratio = ms.toFloat() / max.toFloat()
            val heightDp = (ratio * 110f).coerceAtLeast(if (ms > 0) 4f else 2f).dp
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(heightDp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (ms > 0) accent else accent.copy(alpha = 0.15f)),
            )
        }
    }
}

@Composable
private fun UpgradeHint(onUpgradeClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
        ),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "💛 전체 기간 통계를 보려면",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "무료 버전은 지난 7일까지만 보여드려요. Pro 로 업그레이드하면 모든 기록과 30일+ 그래프를 볼 수 있어요.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(14.dp))
            Button(
                onClick = onUpgradeClick,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(44.dp),
            ) { Text("Pro 자세히 보기", fontWeight = FontWeight.SemiBold) }
        }
    }
}

private fun formatHMS(ms: Long): String {
    val totalMin = ms / 60_000L
    val h = totalMin / 60
    val m = totalMin % 60
    return when {
        h == 0L && m == 0L -> "0분"
        h == 0L -> "${m}분"
        m == 0L -> "${h}시간"
        else -> "${h}시간 ${m}분"
    }
}

private fun startOfDay(ms: Long): Long {
    val cal = Calendar.getInstance().apply {
        timeInMillis = ms
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return cal.timeInMillis
}
