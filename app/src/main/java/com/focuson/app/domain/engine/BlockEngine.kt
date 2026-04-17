package com.focuson.app.domain.engine

import com.focuson.app.domain.model.ActiveSession
import java.net.URI
import java.util.concurrent.atomic.AtomicReference

/**
 * 접근성 서비스에서 O(1) 으로 참조하는 판정 엔진.
 * 세션 상태를 싱글톤 원자 참조로 유지 — 서비스 인스턴스가 달라도 공유.
 */
object BlockEngine {
    private val current = AtomicReference<ActiveSession?>(null)

    fun set(session: ActiveSession?) { current.set(session) }

    fun active(): ActiveSession? = current.get()?.takeIf { !it.isExpired() }

    fun isAppBlocked(packageName: String): Boolean {
        val s = active() ?: return false
        if (s.allowedPackages.contains(packageName)) return false
        return if (s.whitelist) {
            // 명상 모드: 허용 리스트에 없으면 전부 차단
            !s.allowedPackages.contains(packageName)
        } else {
            s.blockedPackages.contains(packageName)
        }
    }

    fun isUrlBlocked(url: String): Boolean {
        val s = active() ?: return false
        val host = extractHost(url) ?: return false
        return s.blockedSitePatterns.any { pattern -> matchHost(host, pattern) }
    }

    private fun extractHost(raw: String): String? {
        val withScheme = if (raw.contains("://")) raw else "https://$raw"
        return runCatching {
            val uri = URI(withScheme.trim())
            uri.host?.removePrefix("www.")?.lowercase()
        }.getOrNull()
    }

    private fun matchHost(host: String, pattern: String): Boolean {
        val p = pattern.trim().lowercase().removePrefix("www.")
        if (p.isEmpty()) return false
        if (p == "*") return true
        if (p.startsWith("*.")) {
            val suffix = p.substring(2)
            return host == suffix || host.endsWith(".$suffix")
        }
        return host == p
    }
}
