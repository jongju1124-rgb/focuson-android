package com.focuson.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.focuson.app.MainActivity
import com.focuson.app.R
import com.focuson.app.domain.engine.BlockEngine
import com.focuson.app.domain.model.PresetMode

/**
 * 홈 화면 위젯. 현재 세션 "상태" 를 반영한다.
 *
 * 비활성:
 *   상단: "포커스온 · 프리셋 탭해서 시작"
 *   칩: [OFF (회색)] [📚] [💼] [🧘]    ← 프리셋 탭 → 바로 세션 시작
 *
 * 활성 세션 (예: 수험생):
 *   상단: "📚 수험생 중 · 12:34"
 *   칩: [OFF (빨강)] [📚 (밝게)] [💼 (흐림)] [🧘 (흐림)]
 *         · OFF 탭 → 세션 종료
 *         · 다른 프리셋 탭 → 앱 열기 (세션 전환은 안 함)
 *
 * 업데이트 주기: BlockSessionService ticker 가 5초마다 ACTION_REFRESH broadcast.
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

        // 루트 탭 → 앱 열기 (정보 확인용, 칩은 각자 별도 핸들러)
        rv.setOnClickPendingIntent(R.id.widget_root, openAppIntent(context))

        if (active != null) {
            // ── 활성 세션 ──
            val activeMode = PresetMode.fromId(active.modeId)
            val emoji = when (activeMode) {
                PresetMode.STUDENT -> "📚"
                PresetMode.WORKER -> "💼"
                PresetMode.MEDITATION -> "🧘"
                null -> "⏱"
            }
            val modeName = activeMode?.let { context.getString(it.displayNameRes) } ?: "집중"
            rv.setTextViewText(R.id.widget_status_title, "$emoji $modeName 중")
            rv.setTextViewText(R.id.widget_status_sub, "OFF 탭하면 종료")

            val totalSec = (active.remainingMillis() / 1000L).coerceAtLeast(0L)
            val m = totalSec / 60
            val s = totalSec % 60
            rv.setTextViewText(R.id.widget_status_time, "%d:%02d".format(m, s))

            // OFF 활성 (빨강) → 세션 종료
            rv.setInt(R.id.widget_chip_off, "setBackgroundResource", R.drawable.widget_chip_off)
            rv.setTextColor(R.id.widget_chip_off_label, 0xFFFFFFFF.toInt())
            rv.setOnClickPendingIntent(R.id.widget_chip_off, stopSessionIntent(context))

            // 프리셋 칩: 현재 모드만 밝게, 나머지는 dim
            paintPreset(rv, R.id.widget_preset_student, PresetMode.STUDENT, activeMode)
            paintPreset(rv, R.id.widget_preset_worker, PresetMode.WORKER, activeMode)
            paintPreset(rv, R.id.widget_preset_meditation, PresetMode.MEDITATION, activeMode)
            // 세션 중 프리셋 탭은 앱 열기만 (세션 교체는 안 함)
            rv.setOnClickPendingIntent(R.id.widget_preset_student, openAppIntent(context))
            rv.setOnClickPendingIntent(R.id.widget_preset_worker, openAppIntent(context))
            rv.setOnClickPendingIntent(R.id.widget_preset_meditation, openAppIntent(context))
        } else {
            // ── 비활성 ──
            rv.setTextViewText(R.id.widget_status_title, context.getString(R.string.app_name))
            rv.setTextViewText(R.id.widget_status_sub, "프리셋 탭하면 바로 세션 시작")
            rv.setTextViewText(R.id.widget_status_time, "")

            // OFF 비활성 (회색) → 탭 시 앱 열기 (할 일이 없으므로)
            rv.setInt(R.id.widget_chip_off, "setBackgroundResource", R.drawable.widget_chip_off_idle)
            rv.setTextColor(R.id.widget_chip_off_label, 0xFF94A3B8.toInt())
            rv.setOnClickPendingIntent(R.id.widget_chip_off, openAppIntent(context))

            // 프리셋: 모두 밝게, 탭 시 broadcast 로 바로 시작
            rv.setInt(R.id.widget_preset_student, "setBackgroundResource", R.drawable.widget_chip_student)
            rv.setInt(R.id.widget_preset_worker, "setBackgroundResource", R.drawable.widget_chip_worker)
            rv.setInt(R.id.widget_preset_meditation, "setBackgroundResource", R.drawable.widget_chip_meditation)
            rv.setOnClickPendingIntent(
                R.id.widget_preset_student,
                startPresetIntent(context, PresetMode.STUDENT),
            )
            rv.setOnClickPendingIntent(
                R.id.widget_preset_worker,
                startPresetIntent(context, PresetMode.WORKER),
            )
            rv.setOnClickPendingIntent(
                R.id.widget_preset_meditation,
                startPresetIntent(context, PresetMode.MEDITATION),
            )
        }

        return rv
    }

    private fun paintPreset(
        rv: RemoteViews,
        viewId: Int,
        mode: PresetMode,
        activeMode: PresetMode?,
    ) {
        val isActive = mode == activeMode
        val bg = if (isActive) {
            when (mode) {
                PresetMode.STUDENT -> R.drawable.widget_chip_student
                PresetMode.WORKER -> R.drawable.widget_chip_worker
                PresetMode.MEDITATION -> R.drawable.widget_chip_meditation
            }
        } else {
            R.drawable.widget_chip_dim
        }
        rv.setInt(viewId, "setBackgroundResource", bg)
    }

    private fun openAppIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(
            context, 100, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    private fun startPresetIntent(context: Context, mode: PresetMode): PendingIntent {
        val intent = Intent(context, WidgetActionReceiver::class.java).apply {
            action = WidgetActionReceiver.ACTION_START_SESSION
            putExtra(WidgetActionReceiver.EXTRA_PRESET_ID, mode.id)
        }
        val reqCode = when (mode) {
            PresetMode.STUDENT -> 201
            PresetMode.WORKER -> 202
            PresetMode.MEDITATION -> 203
        }
        return PendingIntent.getBroadcast(
            context, reqCode, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    private fun stopSessionIntent(context: Context): PendingIntent {
        val intent = Intent(context, WidgetActionReceiver::class.java).apply {
            action = WidgetActionReceiver.ACTION_STOP_SESSION
        }
        return PendingIntent.getBroadcast(
            context, 200, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    companion object {
        const val ACTION_REFRESH = "com.focuson.app.widget.ACTION_REFRESH"

        /** BlockSessionService 등 외부에서 위젯 갱신 요청 */
        fun requestUpdate(context: Context) {
            val intent = Intent(context, FocusOnWidgetProvider::class.java).apply {
                action = ACTION_REFRESH
            }
            context.sendBroadcast(intent)
        }
    }
}
