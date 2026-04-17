package com.focuson.app.ui.custom

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.focuson.app.FocusOnApp
import com.focuson.app.R
import com.focuson.app.data.repo.BlockRuleRepository
import com.focuson.app.domain.model.PresetMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ModeDetailScreen(
    mode: PresetMode,
    onPickApps: () -> Unit,
    onPickSites: () -> Unit,
    onBack: () -> Unit,
) {
    val app = remember { FocusOnApp.instance }
    var appCount by remember(mode.id) { mutableIntStateOf(0) }
    var siteCount by remember(mode.id) { mutableIntStateOf(0) }

    LaunchedEffect(mode.id) {
        withContext(Dispatchers.IO) {
            app.blockRuleRepository.seedIfEmpty(mode)
            val rules = app.blockRuleRepository.findMode(mode.id)
            appCount = rules.count { it.kind == BlockRuleRepository.KIND_APP && it.enabled }
            siteCount = rules.count { it.kind == BlockRuleRepository.KIND_SITE && it.enabled }
        }
    }

    val whitelist = mode.whitelistMode

    Scaffold { inner: PaddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(inner).padding(20.dp),
        ) {
            // 모드 헤더
            Card(
                colors = CardDefaults.cardColors(containerColor = mode.color.copy(alpha = 0.12f)),
                border = BorderStroke(1.dp, mode.color.copy(alpha = 0.35f)),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = mode.color,
                        modifier = Modifier.size(52.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                when (mode) {
                                    PresetMode.STUDENT -> "📚"
                                    PresetMode.WORKER -> "💼"
                                    PresetMode.MEDITATION -> "🧘"
                                },
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White,
                            )
                        }
                    }
                    Spacer(Modifier.size(14.dp))
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
            }

            Spacer(Modifier.height(20.dp))
            Text(
                "차단 규칙",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))

            RuleRow(
                title = stringResource(
                    if (whitelist) R.string.app_picker_title_allow
                    else R.string.app_picker_title
                ),
                description = if (whitelist)
                    "${appCount}개 앱만 세션 중 사용 가능"
                else
                    "${appCount}개 앱 차단 중",
                accentColor = mode.color,
                onClick = onPickApps,
            )
            Spacer(Modifier.height(10.dp))
            RuleRow(
                title = stringResource(R.string.site_rules_title),
                description = "${siteCount}개 도메인 차단 중",
                accentColor = mode.color,
                onClick = onPickSites,
            )

            Spacer(Modifier.weight(1f))
            OutlinedButton(
                onClick = onBack,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                Text("홈으로")
            }
        }
    }
}

@Composable
private fun RuleRow(
    title: String,
    description: String,
    accentColor: Color,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.size(2.dp))
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = accentColor,
                    fontWeight = FontWeight.Medium,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
