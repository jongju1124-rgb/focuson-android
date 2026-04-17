package com.focuson.app.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.focuson.app.domain.engine.BlockEngine
import com.focuson.app.overlay.BlockOverlayManager

class AppBlockerAccessibilityService : AccessibilityService() {

    private lateinit var overlayManager: BlockOverlayManager

    override fun onServiceConnected() {
        super.onServiceConnected()
        overlayManager = BlockOverlayManager(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val e = event ?: return
        val pkg = e.packageName?.toString() ?: return
        if (pkg == packageName || pkg == "com.focuson.app.debug") return

        when (e.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> handleWindowStateChanged(pkg, e)
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED,
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> handleContentChanged(pkg, e)
        }
    }

    private fun handleWindowStateChanged(pkg: String, e: AccessibilityEvent) {
        // 엄격모드 중 우회 시도(앱 삭제 / 접근성 끄기) 감지 → 뒤로가기
        if (BlockEngine.active()?.strict == true && isStrictBypassScreen(pkg, e)) {
            performGlobalAction(GLOBAL_ACTION_BACK)
            return
        }
        if (BlockEngine.isAppBlocked(pkg)) {
            overlayManager.showAppBlocked(pkg)
            performGlobalAction(GLOBAL_ACTION_HOME)
            return
        }
        val url = if (pkg in BROWSERS) extractBrowserUrl(rootInActiveWindow, pkg) else null
        if (url != null && BlockEngine.isUrlBlocked(url)) {
            overlayManager.showSiteBlocked(url)
            performGlobalAction(GLOBAL_ACTION_BACK)
            return
        }
        overlayManager.hideIfNotBlocked(pkg, url)
    }

    private fun handleContentChanged(pkg: String, e: AccessibilityEvent) {
        if (pkg !in BROWSERS) return
        val url = extractBrowserUrl(rootInActiveWindow, pkg) ?: return
        if (BlockEngine.isUrlBlocked(url)) {
            overlayManager.showSiteBlocked(url)
            performGlobalAction(GLOBAL_ACTION_BACK)
        } else {
            overlayManager.hideIfNotBlocked(pkg, url)
        }
    }

    private fun isStrictBypassScreen(pkg: String, e: AccessibilityEvent): Boolean {
        if (pkg !in SETTINGS_PACKAGES) return false
        val text = (e.text.joinToString(" ") + " " + (e.className ?: "")).lowercase()
        return UNINSTALL_KEYWORDS.any { it in text } ||
            ACCESSIBILITY_KEYWORDS.any { it in text } ||
            FORCE_STOP_KEYWORDS.any { it in text }
    }

    private fun extractBrowserUrl(root: AccessibilityNodeInfo?, pkg: String): String? {
        root ?: return null
        val ids = BROWSER_URL_BAR_IDS[pkg] ?: return null
        for (id in ids) {
            val nodes = root.findAccessibilityNodeInfosByViewId(id)
            val text = nodes?.firstOrNull()?.text?.toString()
            if (!text.isNullOrBlank()) return text
        }
        return null
    }

    override fun onInterrupt() { }

    override fun onDestroy() {
        super.onDestroy()
        if (::overlayManager.isInitialized) overlayManager.dispose()
    }

    companion object {
        val BROWSERS = setOf(
            "com.android.chrome",
            "com.sec.android.app.sbrowser",   // 삼성 인터넷
            "com.microsoft.emmx",             // Edge
            "org.mozilla.firefox",
            "com.brave.browser",
            "com.naver.whale",                // 네이버 웨일
            "com.kakao.talk",                 // 인앱 브라우저는 제한적 지원
        )

        val BROWSER_URL_BAR_IDS = mapOf(
            "com.android.chrome" to listOf("com.android.chrome:id/url_bar"),
            "com.sec.android.app.sbrowser" to listOf(
                "com.sec.android.app.sbrowser:id/location_bar_edit_text",
                "com.sec.android.app.sbrowser:id/url_bar",
            ),
            "com.microsoft.emmx" to listOf("com.microsoft.emmx:id/url_bar"),
            "org.mozilla.firefox" to listOf(
                "org.mozilla.firefox:id/mozac_browser_toolbar_url_view",
                "org.mozilla.firefox:id/url_bar_title",
            ),
            "com.brave.browser" to listOf("com.brave.browser:id/url_bar"),
            "com.naver.whale" to listOf("com.naver.whale:id/url_bar"),
        )

        val SETTINGS_PACKAGES = setOf(
            "com.android.settings",
            "com.samsung.android.settings",
            "com.samsung.android.app.settings",
            "com.google.android.packageinstaller",
            "com.android.packageinstaller",
        )

        val UNINSTALL_KEYWORDS = listOf("uninstall", "제거", "삭제", "언인스톨")
        val ACCESSIBILITY_KEYWORDS = listOf("accessibility", "접근성", "접근성 서비스")
        val FORCE_STOP_KEYWORDS = listOf("force stop", "강제 중지", "강제 종료")
    }
}
