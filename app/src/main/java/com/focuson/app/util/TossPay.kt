package com.focuson.app.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.focuson.app.BuildConfig

/**
 * 토스 송금 링크 + 계좌이체 백업 정보.
 *
 *  - Toss ID 가 설정돼 있으면 `https://toss.me/{id}/{amount}` 로 딥링크.
 *    토스 앱 설치돼 있으면 바로 열림, 아니면 웹 브라우저가 "앱 설치" 유도.
 *  - Toss 실패 대비로 계좌번호·예금주 복사 기능 같이 제공.
 */
object TossPay {

    data class PaymentInfo(
        val tossId: String,
        val bank: String,
        val accountNumber: String,
        val holder: String,
        val devEmail: String,
    ) {
        val accountDisplay: String get() = "$bank $accountNumber ($holder)"
    }

    fun info(): PaymentInfo = PaymentInfo(
        tossId = BuildConfig.TOSS_ID,
        bank = BuildConfig.PAYMENT_BANK,
        accountNumber = BuildConfig.PAYMENT_ACCOUNT,
        holder = BuildConfig.PAYMENT_HOLDER,
        devEmail = BuildConfig.DEV_EMAIL,
    )

    /** toss.me 딥링크로 결제 시도. 실패 시 false. */
    fun openToss(context: Context, amountKrw: Int): Boolean {
        val tossId = BuildConfig.TOSS_ID
        if (tossId.isBlank() || tossId == "yourTossId") return false
        val url = "https://toss.me/$tossId/$amountKrw"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return runCatching { context.startActivity(intent); true }.getOrDefault(false)
    }

    /** 계좌번호 클립보드 복사 + 토스트. */
    fun copyAccount(context: Context) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val text = "${BuildConfig.PAYMENT_BANK} ${BuildConfig.PAYMENT_ACCOUNT}"
        clipboard.setPrimaryClip(ClipData.newPlainText("계좌번호", text))
        Toast.makeText(context, "계좌번호 복사됨: $text", Toast.LENGTH_LONG).show()
    }

    /** 개발자 이메일로 "라이선스 문의" 메일 작성 화면 열기. */
    fun openEmail(context: Context, subject: String, body: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:${BuildConfig.DEV_EMAIL}")
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }
    }
}
