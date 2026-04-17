package com.focuson.app.domain.model

/**
 * 앱 분류 카테고리. AppPicker 에서 섹션 헤더로 사용.
 * [order] 작을수록 위에 표시.
 */
enum class AppCategory(
    val displayName: String,
    val emoji: String,
    val order: Int,
) {
    SNS("SNS", "📱", 10),
    VIDEO("OTT·동영상", "🎬", 20),
    MUSIC("음악", "🎵", 30),
    WEBTOON("웹툰·웹소설", "📖", 40),
    COMMUNITY("커뮤니티", "💬", 50),
    SHOPPING("쇼핑·배달", "🛒", 60),
    GAME("게임", "🎮", 70),
    MESSAGING("메신저", "✉️", 80),
    BROWSER("브라우저", "🌐", 90),
    NEWS("뉴스·매거진", "📰", 100),
    OTHER("기타", "📦", 999),
    ;
}
