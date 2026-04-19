package com.focuson.app.domain.model

/**
 * Pro 구독 티어.
 *
 * FREE  : 기본 기능 모두 제공, 통계는 "지난 7일"만 (미리보기)
 * TIER1 : 1,000원 · 전체 통계 열람
 * TIER2 : 2,900원 · TIER1 + 무제한 커스텀 프리셋
 * TIER3 : 4,900원 · TIER2 + 스케줄 자동화 + 스마트 트리거
 *
 * 상위 티어는 하위 티어의 모든 기능을 포함 (ordinal 비교).
 */
enum class ProTier(
    val id: String,
    val priceKrw: Int,
    val displayName: String,
    val tagline: String,
) {
    FREE(id = "free", priceKrw = 0, displayName = "무료", tagline = "기본 기능 모두 제공"),
    TIER1(id = "tier1", priceKrw = 1000, displayName = "라이트", tagline = "전체 통계 열람"),
    TIER2(id = "tier2", priceKrw = 2900, displayName = "베이직", tagline = "전체 통계 + 무제한 커스텀 프리셋"),
    TIER3(id = "tier3", priceKrw = 4900, displayName = "프로", tagline = "전체 기능 (스케줄·트리거 포함)"),
    ;

    fun atLeast(required: ProTier): Boolean = this.ordinal >= required.ordinal

    companion object {
        fun fromId(id: String?): ProTier = entries.firstOrNull { it.id == id } ?: FREE
    }
}
