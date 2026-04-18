package com.focuson.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.focuson.app.MainActivity
import com.focuson.app.R
import com.focuson.app.domain.engine.BlockEngine
import com.focuson.app.domain.model.PresetMode

/**
 * 홈 화면 위젯.
 *  - 비활성 상태: 3개 프리셋 칩을 보여주고, 탭 시 해당 프리셋이 펼쳐진 채로 MainActivity 를 연다.
 *  - 활성 세션 상태: 모드 + 남은 시간(MM:SS) 표시. 탭 시 세션 화면으로 이동.
 *
 * 업데이트 트리거는 BlockSessionService 에서 ticker 마다 ACTION_REFRESH broadcast 로 호출.
 */
class FocusOnWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        manager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        val rv = buildRemoteViews(context)
        for (id in appWidgetIds) manager.updateAppWidget(id, rv)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                ComponentName(context, FocusOnWidgetProvider::class.java),
            )
            if (ids.isNotEmpty()) {
                val rv = buildRemoteViews(context)
                for (id in ids) manager.updateAppWidget(id, rv)
            }
        }
    }

    private fun buildRemoteViews(context: Context): RemoteViews {
        val rv = RemoteViews(context.packageName, R.layout.widget_focuson)
        val active = BlockEngine.active()

        if (active != null) {
            // ── 활성 세션 ──
            rv.setViewVisibility(R.id.widget_active, View.VISIBLE)
            rv.setViewVisibility(R.id.widget_idle, View.GONE)

            val mode = PresetMode.fromId(active.modeId)
            val emoji = when (mode) {
                PresetMode.STUDENT -> "📚"
                PresetMode.WORKER -> "💼"
                PresetMode.MEDITATION -> "🧘"
                null -> "⏱"
            }
            val modeName = mode?.let { context.getString(it.displayNameRes) } ?: "집중"
            rv.setTextViewText(R.id.widget_active_mode, "$emoji $modeName")

            val totalSec = (active.remainingMillis() / 1000L).coerceAtLeast(0L)
            val m = totalSec / 60
            val s = totalSec % 60
            rv.setTextViewText(R.id.widget_active_time, "%d:%02d".format(m, s))

            rv.setOnClickPendingIntent(R.id.widget_root, openAppIntent(context, null))
        } else {
            // ── 비활성 ──
            rv.setViewVisibility(R.id.widget_active, View.GONE)
            rv.setViewVisibility(R.id.widget_idle, View.VISIBLE)

            rv.setOnClickPendingIntent(R.id.widget_root, openAppIntent(context, null))
            rv.setOnClickPendingIntent(
                R.id.widget_preset_student,
                openAppIntent(context, PresetMode.STUDENT.id),
            )
            rv.setOnClickPendingIntent(
                R.id.widget_preset_worker,
                openAppIntent(context, PresetMode.WORKER.id),
            )
            rv.setOnClickPendingIntent(
                R.id.widget_preset_meditation,
                openAppIntent(context, PresetMode.MEDITATION.id),
            )
        }

        return rv
    }

    private fun openAppIntent(context: Context, presetId: String?): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            if (presetId != null) putExtra(EXTRA_WIDGET_PRESET_ID, presetId)
        }
        // requestCode 는 프리셋별로 달라야 PendingIntent 가 덮어쓰지 않음
        val reqCode = when (presetId) {
            PresetMode.STUDENT.id -> 101
            PresetMode.WORKER.id -> 102
            PresetMode.MEDITATION.id -> 103
            else -> 100
        }
        return PendingIntent.getActivity(
            context,
            reqCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    companion object {
        const val ACTION_REFRESH = "com.focuson.app.widget.ACTION_REFRESH"
        const val EXTRA_WIDGET_PRESET_ID = "widget_preset_id"

        /** BlockSessionService 등 외부에서 위젯 갱신 요청 */
        fun requestUpdate(context: Context) {
            val intent = Intent(context, FocusOnWidgetProvider::class.java).apply {
                action = ACTION_REFRESH
            }
            context.sendBroadcast(intent)
        }
    }
}
