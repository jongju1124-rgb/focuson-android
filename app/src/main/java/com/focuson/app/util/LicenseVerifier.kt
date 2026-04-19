package com.focuson.app.util

import com.focuson.app.BuildConfig
import com.focuson.app.domain.model.ProTier
import java.util.Locale
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * 오프라인 라이선스 키 발급·검증.
 *
 * 발급 흐름 (수동):
 *   1) 사용자가 토스로 송금 (메시지란에 본인 이메일 기재)
 *   2) 개발자가 송금 확인 후 [generate] 로 키 생성
 *   3) 이메일로 "이메일 + 티어 + 16자리 키" 전달
 *   4) 사용자가 앱 Pro 화면에서 세 값 입력 → [verify] 로 검증 → SettingsStore 저장
 *
 * 검증은 완전 오프라인. 서버 없음.
 * 비밀키는 BuildConfig.LICENSE_SECRET 에 주입되며 릴리즈 APK 엔 난독화로 숨김.
 * (디컴파일로 100% 보호는 불가 — 정직한 대다수 사용자 대상으로는 충분)
 */
object LicenseVerifier {

    private const val KEY_LENGTH = 16

    /** 개발자 도구에서 사용 (CLI 유틸). 앱 런타임에서는 거의 쓸 일 없음. */
    fun generate(email: String, tier: ProTier): String {
        val payload = canonical(email, tier)
        return hmacSha256(payload, BuildConfig.LICENSE_SECRET)
            .uppercase(Locale.ROOT)
            .take(KEY_LENGTH)
    }

    /** 사용자가 입력한 키가 해당 email+tier 조합에 대해 발급된 게 맞는지 검증. */
    fun verify(email: String, tier: ProTier, licenseKey: String): Boolean {
        if (tier == ProTier.FREE) return true
        val cleanInput = licenseKey.trim().uppercase(Locale.ROOT)
        if (cleanInput.length != KEY_LENGTH) return false
        val expected = generate(email.trim().lowercase(Locale.ROOT), tier)
        return constantTimeEquals(cleanInput, expected)
    }

    private fun canonical(email: String, tier: ProTier): String =
        "focuson|${email.trim().lowercase(Locale.ROOT)}|${tier.id}"

    private fun hmacSha256(payload: String, secret: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(Charsets.UTF_8), "HmacSHA256"))
        val raw = mac.doFinal(payload.toByteArray(Charsets.UTF_8))
        return raw.joinToString("") { "%02x".format(it) }
    }

    private fun constantTimeEquals(a: String, b: String): Boolean {
        if (a.length != b.length) return false
        var diff = 0
        for (i in a.indices) diff = diff or (a[i].code xor b[i].code)
        return diff == 0
    }
}
