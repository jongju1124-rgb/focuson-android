package com.focuson.app.overlay

import android.content.Context
import android.graphics.Color as AColor
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.os.Build
import android.provider.Settings
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.focuson.app.R
import com.focuson.app.data.repo.AppRepository
import com.focuson.app.domain.engine.BlockEngine
import com.focuson.app.service.AppBlockerAccessibilityService

/**
 * Accessibility service 안에서 full-screen overlay 를 올렸다 내렸다 함.
 * Compose 를 overlay 에서 쓰려면 ViewTreeLifecycleOwner 수동 세팅 필요하지만
 * 여기서는 단순 View 로 충분 — 의존성을 최소화.
 */
class BlockOverlayManager(private val context: Context) {

    private val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val repo = AppRepository(context)
    private var view: View? = null
    private var currentReason: Reason? = null

    sealed class Reason {
        data class AppBlocked(val packageName: String) : Reason()
        data class SiteBlocked(val url: String) : Reason()
    }

    fun showAppBlocked(pkg: String) = show(Reason.AppBlocked(pkg))
    fun showSiteBlocked(url: String) = show(Reason.SiteBlocked(url))

    private fun show(reason: Reason) {
        if (!Settings.canDrawOverlays(context)) return
        if (view != null && currentReason == reason) return
        hide()
        currentReason = reason
        val root = buildView(reason)
        val params = WindowManager.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED,
            PixelFormat.TRANSLUCENT,
        )
        runCatching { wm.addView(root, params); view = root }
    }

    fun hide() {
        view?.let { runCatching { wm.removeViewImmediate(it) } }
        view = null
        currentReason = null
    }

    fun hideIfNotBlocked(currentPackage: String, currentUrl: String? = null) {
        val reason = currentReason ?: return
        val active = BlockEngine.active()
        if (active == null) { hide(); return }
        when (reason) {
            is Reason.AppBlocked -> if (currentPackage != reason.packageName && !BlockEngine.isAppBlocked(currentPackage)) hide()
            is Reason.SiteBlocked -> {
                val stillInBrowser = currentPackage in AppBlockerAccessibilityServiceBrowsers
                when {
                    !stillInBrowser -> hide()
                    currentUrl == null -> Unit   // URL unreadable: stay visible (conservative)
                    !BlockEngine.isUrlBlocked(currentUrl) -> hide()
                }
            }
        }
    }

    fun dispose() { hide() }

    private fun buildView(reason: Reason): View {
        val container = FrameLayout(context).apply {
            setBackgroundColor(AColor.parseColor("#E6101828"))
            isClickable = true; isFocusable = true
        }
        val column = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(32), dp(32), dp(32), dp(32))
        }

        val title = TextView(context).apply {
            text = context.getString(R.string.overlay_blocked_title)
            setTextColor(AColor.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 32f)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val message = TextView(context).apply {
            text = when (reason) {
                is Reason.AppBlocked -> context.getString(R.string.overlay_blocked_app, repo.labelFor(reason.packageName))
                is Reason.SiteBlocked -> context.getString(R.string.overlay_blocked_site)
            }
            setTextColor(AColor.parseColor("#CBD5E1"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            gravity = Gravity.CENTER
            setPadding(0, dp(12), 0, dp(24))
        }

        val remaining = TextView(context).apply {
            setTextColor(AColor.parseColor("#F5C542"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            updateRemainingText(this)
        }

        val hint = TextView(context).apply {
            text = context.getString(R.string.overlay_back_hint)
            setTextColor(AColor.parseColor("#94A3B8"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            gravity = Gravity.CENTER
            setPadding(0, dp(16), 0, 0)
        }

        column.addView(title)
        column.addView(message)
        column.addView(remaining)
        column.addView(hint)

        container.addView(
            column,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
                Gravity.CENTER,
            ),
        )
        return container
    }

    private fun updateRemainingText(tv: TextView) {
        val ms = BlockEngine.active()?.remainingMillis() ?: 0L
        val totalSec = ms / 1000
        val m = totalSec / 60
        val s = totalSec % 60
        tv.text = context.getString(R.string.overlay_remaining, m.toInt(), s.toInt())
    }

    private fun dp(v: Int): Int =
        (v * context.resources.displayMetrics.density).toInt()

    // 순환 의존을 피하기 위한 내부 사본
    private val AppBlockerAccessibilityServiceBrowsers: Set<String>
        get() = AppBlockerAccessibilityService.BROWSERS
}
