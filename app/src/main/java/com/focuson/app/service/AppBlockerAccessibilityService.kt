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
        overlayManager = BlockOverlayManager(this) {
            // 오버레이 아무 곳이나 탭 → 홈으로
            performGlobalAction(GLOBAL_ACTION_HOME)
        }
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
        // 엄격모드 중 "포커스온 자체"에 대한 우회 시도만 차단 — 다른 앱 제거·강제중지는 통과
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

    /**
     * 엄격모드에서 "포커스온 자체를 무력화하려는 화면"인지 판별.
     *
     * 판별 조건:
     *   1) 현재 앱이 설정/패키지 설치 관리자 패키지
     *   2) 제거/접근성 해제/강제 중지 관련 키워드 존재 (= 액션성 화면)
     *   3) 화면 텍스트 어디엔가 "포커스온" 또는 "focuson" 이 언급됨 (= 대상이 우리 앱)
     *
     * (2)&(3) 둘 다 만족해야 차단. 다른 앱 제거·강제중지·다른 접근성 서비스 해제는 통과.
     */
    private fun isStrictBypassScreen(pkg: String, e: AccessibilityEvent): Boolean {
        if (pkg !in SETTINGS_PACKAGES) return false
        val eventText = (e.text.joinToString(" ") + " " + (e.className ?: "")).lowercase()

        val hasActionKeyword = UNINSTALL_KEYWORDS.any { it in eventText } ||
            ACCESSIBILITY_KEYWORDS.any { it in eventText } ||
            FORCE_STOP_KEYWORDS.any { it in eventText }
        if (!hasActionKeyword) return false

        // 우리 앱이 대상인지 — event text 우선, 없으면 활성 창 최상위 영역까지 확인
        if (FOCUSON_KEYWORDS.any { it in eventText }) return true
        val rootText = collectTopLevelText(rootInActiveWindow).lowercase()
        return FOCUSON_KEYWORDS.any { it in rootText }
    }

    private fun collectTopLevelText(node: AccessibilityNodeInfo?): String {
        node ?: return ""
        val sb = StringBuilder()
        collectTextDepthLimited(node, sb, depth = 0, maxDepth = 3)
        return sb.toString()
    }

    private fun collectTextDepthLimited(
        node: AccessibilityNodeInfo,
        sb: StringBuilder,
        depth: Int,
        maxDepth: Int,
    ) {
        if (depth > maxDepth) return
        node.text?.toString()?.takeIf { it.isNotBlank() }?.let { sb.append(it).append(' ') }
        node.contentDescription?.toString()?.takeIf { it.isNotBlank() }?.let { sb.append(it).append(' ') }
        val count = node.childCount
        for (i in 0 until count) {
            node.getChild(i)?.let { collectTextDepthLimited(it, sb, depth + 1, maxDepth) }
        }
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

        /** "포커스온 자체" 를 대상으로 하는지 판단할 때 쓰는 키워드 (전부 소문자) */
        val FOCUSON_KEYWORDS = listOf("포커스온", "focuson", "com.focuson")
    }
}
