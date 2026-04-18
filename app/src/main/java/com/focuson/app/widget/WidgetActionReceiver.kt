package com.focuson.app.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.focuson.app.MainActivity
import com.focuson.app.domain.model.PresetMode
import com.focuson.app.service.BlockSessionService
import com.focuson.app.util.PermissionChecker

/**
 * 위젯 칩 탭에서 오는 broadcast 처리.
 *  - [ACTION_START_SESSION] : 지정한 preset 을 기본값(defaultDurationMin, strictByDefault) 으로 바로 시작
 *    · 권한이 없으면 MainActivity 로 보냄 (permission 화면 유도)
 *  - [ACTION_STOP_SESSION]  : 현재 세션 종료 (엄격모드면 force=false 라 무시됨)
 *
 * 위젯 탭은 Android 12+ FGS 시작 예외 (widget click exemption) 에 해당하므로
 * BroadcastReceiver 에서 startForegroundService 를 직접 호출할 수 있음.
 */
class WidgetActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_START_SESSION -> handleStart(context, intent)
            ACTION_STOP_SESSION -> handleStop(context)
        }
    }

    private fun handleStart(context: Context, intent: Intent) {
        val presetId = intent.getStringExtra(EXTRA_PRESET_ID) ?: return
        val mode = PresetMode.fromId(presetId) ?: return

        // 이미 세션 실행 중이면 무시
        if (com.focuson.app.domain.engine.BlockEngine.active() != null) {
            Toast.makeText(context, "이미 세션이 진행 중이에요", Toast.LENGTH_SHORT).show()
            return
        }

        val hasAccessibility = PermissionChecker.accessibilityGranted(context)
        val hasOverlay = PermissionChecker.overlayGranted(context)
        if (!hasAccessibility || !hasOverlay) {
            Toast.makeText(context, "먼저 권한 설정이 필요해요", Toast.LENGTH_SHORT).show()
            val openApp = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            context.startActivity(openApp)
            return
        }

        val serviceIntent = BlockSessionService.startIntent(
            context,
            mode.id,
            mode.defaultDurationMin,
            mode.strictByDefault,
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(context, serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
        Toast.makeText(
            context,
            "${context.getString(mode.displayNameRes)} 시작 · ${mode.defaultDurationMin}분",
            Toast.LENGTH_SHORT,
        ).show()
    }

    private fun handleStop(context: Context) {
        val active = com.focuson.app.domain.engine.BlockEngine.active()
        if (active == null) {
            Toast.makeText(context, "진행 중인 세션이 없어요", Toast.LENGTH_SHORT).show()
            return
        }
        if (active.strict) {
            Toast.makeText(context, "엄격모드는 중간 종료 불가", Toast.LENGTH_SHORT).show()
            return
        }
        val serviceIntent = BlockSessionService.stopIntent(context, force = false)
        context.startService(serviceIntent)
        Toast.makeText(context, "세션을 종료했어요", Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val ACTION_START_SESSION = "com.focuson.app.widget.ACTION_START_SESSION"
        const val ACTION_STOP_SESSION = "com.focuson.app.widget.ACTION_STOP_SESSION"
        const val EXTRA_PRESET_ID = "preset_id"
    }
}
