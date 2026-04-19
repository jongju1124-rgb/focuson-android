package com.focuson.app.ui.pro

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.focuson.app.FocusOnApp
import com.focuson.app.domain.model.ProTier
import com.focuson.app.util.LicenseVerifier
import com.focuson.app.util.TossPay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val app = FocusOnApp.instance
    val scope = rememberCoroutineScope()
    val currentTierId by app.settingsStore.proTierId.collectAsState(initial = "free")
    val currentTier = ProTier.fromId(currentTierId)

    var selectedTier by remember { mutableStateOf<ProTier?>(null) }
    var showPaymentSheet by remember { mutableStateOf(false) }
    var showLicenseSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pro 업그레이드") },
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
                .padding(20.dp),
        ) {
            Text(
                "한 번 결제로 평생 사용",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "기본 기능은 평생 무료. Pro는 데이터·개인화·자동화 기능을 열어줍니다.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (currentTier != ProTier.FREE) {
                Spacer(Modifier.height(14.dp))
                CurrentTierBanner(currentTier)
            }

            Spacer(Modifier.height(18.dp))
            listOf(ProTier.TIER1, ProTier.TIER2, ProTier.TIER3).forEach { tier ->
                TierCard(
                    tier = tier,
                    isCurrent = currentTier == tier,
                    isRecommended = tier == ProTier.TIER3,
                    onClick = {
                        selectedTier = tier
                        showPaymentSheet = true
                    },
                )
                Spacer(Modifier.height(12.dp))
            }

            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { showLicenseSheet = true },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp),
            ) { Text("이미 라이선스 키가 있어요") }

            Spacer(Modifier.height(14.dp))
            Text(
                "· 결제는 토스 송금으로 진행됩니다.\n" +
                    "· 송금 후 메시지란에 본인 이메일을 적어주세요.\n" +
                    "· 하루 안에 라이선스 키를 회신 드립니다.\n" +
                    "· 모든 데이터는 기기에만 저장됩니다 (서버·광고 없음).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    if (showPaymentSheet && selectedTier != null) {
        PaymentDialog(
            tier = selectedTier!!,
            onOpenToss = { amount ->
                val ok = TossPay.openToss(context, amount)
                if (!ok) TossPay.copyAccount(context)
            },
            onCopyAccount = { TossPay.copyAccount(context) },
            onEmailInquiry = {
                TossPay.openEmail(
                    context,
                    subject = "Pro 라이선스 요청 (${selectedTier!!.displayName})",
                    body = "안녕하세요,\n" +
                        "${selectedTier!!.displayName} (${selectedTier!!.priceKrw}원) 구매했습니다.\n" +
                        "이 계정 이메일: (본인 이메일 입력)\n" +
                        "\n수령하신 라이선스 키를 회신 주시면 앱에서 등록하겠습니다.",
                )
            },
            onDismiss = { showPaymentSheet = false },
        )
    }

    if (showLicenseSheet) {
        LicenseInputDialog(
            onConfirm = { email, tier, key ->
                if (LicenseVerifier.verify(email, tier, key)) {
                    scope.launch {
                        app.settingsStore.setProLicense(tier.id, email, key)
                    }
                    showLicenseSheet = false
                    true
                } else {
                    false
                }
            },
            onDismiss = { showLicenseSheet = false },
        )
    }
}

@Composable
private fun CurrentTierBanner(tier: ProTier) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0EA371).copy(alpha = 0.15f),
        ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF0EA371),
            )
            Spacer(Modifier.fillMaxWidth(0.02f))
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(
                    "현재 ${tier.displayName} 이용 중",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    tier.tagline,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun TierCard(
    tier: ProTier,
    isCurrent: Boolean,
    isRecommended: Boolean,
    onClick: () -> Unit,
) {
    val border = if (isRecommended && !isCurrent) {
        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    }
    Card(
        modifier = Modifier.fillMaxWidth().clickable(enabled = !isCurrent) { onClick() },
        shape = RoundedCornerShape(16.dp),
        border = border,
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    tier.displayName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                if (isRecommended) {
                    Spacer(Modifier.fillMaxWidth(0.02f))
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(8.dp),
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                    ) {
                        Text(
                            "추천",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                Text(
                    "${"%,d".format(tier.priceKrw)}원",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "평생 이용",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            tierBullets(tier).forEach {
                Row(modifier = Modifier.padding(vertical = 3.dp)) {
                    Text("• ", color = MaterialTheme.colorScheme.primary)
                    Text(it, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

private fun tierBullets(tier: ProTier): List<String> = when (tier) {
    ProTier.FREE -> emptyList()
    ProTier.TIER1 -> listOf(
        "전체 기간 통계 열람 (기본 7일 → 무제한)",
        "일별/주별/월별 집중 시간 그래프",
        "완주율 · 스트릭 트래킹",
    )
    ProTier.TIER2 -> listOf(
        "TIER1의 모든 기능",
        "✨ 무제한 커스텀 프리셋 (이름·색·앱 목록 자유 설정)",
        "예: \"새벽 코딩\", \"영어 공부\", \"운동 전 1시간\"",
    )
    ProTier.TIER3 -> listOf(
        "TIER2의 모든 기능",
        "⏰ 스케줄 자동화 — 요일·시간 반복 세션",
        "🎯 스마트 트리거 — 특정 앱 N분 사용시 자동 차단",
        "모든 향후 기능 무료 업데이트",
    )
}

@Composable
private fun PaymentDialog(
    tier: ProTier,
    onOpenToss: (Int) -> Unit,
    onCopyAccount: () -> Unit,
    onEmailInquiry: () -> Unit,
    onDismiss: () -> Unit,
) {
    val info = remember { TossPay.info() }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${tier.displayName} · ${"%,d".format(tier.priceKrw)}원 결제") },
        text = {
            Column {
                Text(
                    "아래 방법 중 편한 걸로 송금해주세요. 송금 메시지란에 본인 이메일을 꼭 기재해주세요.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    "토스 ID: @${info.tossId}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "계좌: ${info.accountDisplay}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    "1️⃣ 토스로 송금 (가장 빠름)\n" +
                        "2️⃣ 계좌이체로 송금 후 이메일 문의",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onOpenToss(tier.priceKrw); onDismiss() },
                shape = RoundedCornerShape(10.dp),
            ) { Text("토스로 ${"%,d".format(tier.priceKrw)}원 보내기") }
        },
        dismissButton = {
            Row {
                TextButton(onClick = { onCopyAccount() }) { Text("계좌 복사") }
                TextButton(onClick = { onEmailInquiry() }) { Text("이메일 문의") }
            }
        },
    )
}

@Composable
private fun LicenseInputDialog(
    onConfirm: (email: String, tier: ProTier, key: String) -> Boolean,
    onDismiss: () -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var key by remember { mutableStateOf("") }
    var selectedTier by remember { mutableStateOf(ProTier.TIER3) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("라이선스 키 등록") },
        text = {
            Column {
                Text(
                    "이메일로 받으신 티어 + 라이선스 키를 입력해주세요.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                Text("티어", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(ProTier.TIER1, ProTier.TIER2, ProTier.TIER3).forEach { t ->
                        val selected = selectedTier == t
                        OutlinedButton(
                            onClick = { selectedTier = t },
                            shape = RoundedCornerShape(8.dp),
                            colors = if (selected) ButtonDefaults.outlinedButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ) else ButtonDefaults.outlinedButtonColors(),
                        ) { Text(t.displayName, style = MaterialTheme.typography.labelSmall) }
                    }
                }
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; errorMsg = null },
                    label = { Text("이메일") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = key,
                    onValueChange = { key = it.uppercase(); errorMsg = null },
                    label = { Text("16자리 라이선스 키") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                errorMsg?.let {
                    Spacer(Modifier.height(6.dp))
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (email.isBlank() || key.isBlank()) {
                    errorMsg = "이메일과 키를 모두 입력해주세요."
                    return@Button
                }
                val ok = onConfirm(email, selectedTier, key)
                if (!ok) errorMsg = "키가 일치하지 않아요. 이메일·티어·키를 다시 확인해주세요."
            }) { Text("등록") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
    )
}

