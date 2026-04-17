package com.focuson.app.domain.model

data class ActiveSession(
    val id: Long,
    val modeId: String,
    val startEpochMs: Long,
    val endEpochMs: Long,
    val strict: Boolean,
    val whitelist: Boolean,
    val blockedPackages: Set<String>,
    val blockedSitePatterns: List<String>,
    val allowedPackages: Set<String>,
) {
    fun remainingMillis(nowMs: Long = System.currentTimeMillis()): Long =
        (endEpochMs - nowMs).coerceAtLeast(0L)

    fun isExpired(nowMs: Long = System.currentTimeMillis()): Boolean =
        nowMs >= endEpochMs
}
