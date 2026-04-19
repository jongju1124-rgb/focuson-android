package com.focuson.app.domain.model

/**
 * Pro 구독 티어. FREE / PRO 2단계.
 *
 * FREE : 기본 기능 모두 제공 (차단·위젯·엄격모드), 통계는 지난 7일만 미리보기
 * PRO  : 2,900원 · 평생 이용 · 전체 통계 + 무제한 커스텀 프리셋
 */
enum class ProTier(
    val id: String,
    val priceKrw: Int,
    val displayName: String,
    val tagline: String,
) {
    FREE(id = "free", priceKrw = 0, displayName = "무료", tagline = "기본 기능 모두 제공"),
    PRO(id = "pro", priceKrw = 2900, displayName = "Pro", tagline = "전체 통계 + 무제한 커스텀 프리셋"),
    ;

    fun atLeast(required: ProTier): Boolean = this.ordinal >= required.ordinal

    companion object {
        fun fromId(id: String?): ProTier = entries.firstOrNull { it.id == id } ?: FREE
    }
}
