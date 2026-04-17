package com.focuson.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.focuson.app.MainActivity
import com.focuson.app.R
import com.focuson.app.data.db.FocusOnDatabase
import com.focuson.app.data.db.entity.SessionEntity
import com.focuson.app.data.prefs.SettingsStore
import com.focuson.app.data.repo.BlockRuleRepository
import com.focuson.app.domain.engine.BlockEngine
import com.focuson.app.domain.model.ActiveSession
import com.focuson.app.domain.model.PresetMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class BlockSessionService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var ticker: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val modeId = intent.getStringExtra(EXTRA_MODE_ID) ?: return START_NOT_STICKY
                val durationMin = intent.getIntExtra(EXTRA_DURATION_MIN, 0)
                val strict = intent.getBooleanExtra(EXTRA_STRICT, false)
                startSession(modeId, durationMin, strict)
            }
            ACTION_STOP -> stopSession(force = false)
            ACTION_STOP_FORCE -> stopSession(force = true)
            ACTION_REHYDRATE -> rehydrate()
            else -> return START_NOT_STICKY
        }
        return START_STICKY
    }

    private fun startSession(modeId: String, durationMin: Int, strict: Boolean) {
        val mode = PresetMode.fromId(modeId) ?: return
        val now = System.currentTimeMillis()
        val end = now + durationMin.coerceAtLeast(1) * 60_000L

        val notification = buildNotification(mode, end)
        startAsForeground(notification)

        scope.launch {
            val db = FocusOnDatabase.get(this@BlockSessionService)
            val repo = BlockRuleRepository(db.blockRuleDao())
            repo.seedIfEmpty(mode)
            val rules = repo.findMode(modeId)
            val customApps = rules.filter { it.kind == BlockRuleRepository.KIND_APP && it.enabled }.map { it.value }.toSet()
            val blockedSites = rules.filter { it.kind == BlockRuleRepository.KIND_SITE && it.enabled }.map { it.value }
            val blockedApps = if (mode.whitelistMode) emptySet() else customApps
            val allowed = if (mode.whitelistMode) customApps else emptySet()

            val sessionId = db.sessionDao().insert(
                SessionEntity(
                    modeId = modeId,
                    startEpochMs = now,
                    endEpochMs = end,
                    strict = strict,
                )
            )
            BlockEngine.set(
                ActiveSession(
                    id = sessionId,
                    modeId = modeId,
                    startEpochMs = now,
                    endEpochMs = end,
                    strict = strict,
                    whitelist = mode.whitelistMode,
                    blockedPackages = blockedApps,
                    blockedSitePatterns = blockedSites,
                    allowedPackages = allowed,
                )
            )
            SettingsStore(this@BlockSessionService).setActiveSession(sessionId, modeId, end, strict)

            ticker?.cancel()
            ticker = launch {
                while (isActive) {
                    val remaining = BlockEngine.active()?.remainingMillis() ?: 0L
                    if (remaining <= 0L) { stopSession(force = true); break }
                    updateNotification(mode, end)
                    delay(5_000L)
                }
            }
        }
    }

    private fun rehydrate() {
        scope.launch {
            val store = SettingsStore(this@BlockSessionService)
            val id = store.activeSessionId.firstOrNull() ?: run { stopSelf(); return@launch }
            val modeId = store.activeModeId.firstOrNull() ?: run { stopSelf(); return@launch }
            val endMs = store.activeEndEpochMs.firstOrNull() ?: run { stopSelf(); return@launch }
            val strict = store.activeStrict.firstOrNull() ?: false
            if (endMs <= System.currentTimeMillis()) { store.clearActiveSession(); stopSelf(); return@launch }

            val mode = PresetMode.fromId(modeId) ?: run { stopSelf(); return@launch }
            val notif = buildNotification(mode, endMs)
            startAsForeground(notif)

            val db = FocusOnDatabase.get(this@BlockSessionService)
            val repo = BlockRuleRepository(db.blockRuleDao())
            val rules = repo.findMode(modeId)
            val customApps = rules.filter { it.kind == BlockRuleRepository.KIND_APP && it.enabled }.map { it.value }.toSet()
            val blockedSites = rules.filter { it.kind == BlockRuleRepository.KIND_SITE && it.enabled }.map { it.value }
            val blockedApps = if (mode.whitelistMode) emptySet() else customApps
            val allowed = if (mode.whitelistMode) customApps else emptySet()

            BlockEngine.set(
                ActiveSession(
                    id = id, modeId = modeId,
                    startEpochMs = System.currentTimeMillis(),
                    endEpochMs = endMs,
                    strict = strict,
                    whitelist = mode.whitelistMode,
                    blockedPackages = blockedApps,
                    blockedSitePatterns = blockedSites,
                    allowedPackages = allowed,
                )
            )
            ticker?.cancel()
            ticker = launch {
                while (isActive) {
                    val remaining = BlockEngine.active()?.remainingMillis() ?: 0L
                    if (remaining <= 0L) { stopSession(force = true); break }
                    updateNotification(mode, endMs)
                    delay(5_000L)
                }
            }
        }
    }

    private fun stopSession(force: Boolean) {
        val active = BlockEngine.active()
        if (!force && active?.strict == true) return   // 엄격모드는 외부 중단 불가
        scope.launch {
            val store = SettingsStore(this@BlockSessionService)
            active?.let {
                val dao = FocusOnDatabase.get(this@BlockSessionService).sessionDao()
                dao.findById(it.id)?.let { s ->
                    dao.update(s.copy(actualEndEpochMs = System.currentTimeMillis(), completed = it.isExpired()))
                }
            }
            BlockEngine.set(null)
            store.clearActiveSession()
            ticker?.cancel()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    private fun startAsForeground(notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIF_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIF_ID, notification)
        }
    }

    private fun buildNotification(mode: PresetMode, endMs: Long): Notification {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.session_channel_name),
                NotificationManager.IMPORTANCE_LOW,
            ).apply { description = getString(R.string.session_channel_desc) }
            nm.createNotificationChannel(ch)
        }
        val openIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val remaining = ((endMs - System.currentTimeMillis()) / 60_000L).toInt().coerceAtLeast(0)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .setContentTitle(getString(R.string.session_running))
            .setContentText(getString(R.string.home_active_session, getString(mode.displayNameRes), remaining))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(openIntent)
            .build()
    }

    private fun updateNotification(mode: PresetMode, endMs: Long) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIF_ID, buildNotification(mode, endMs))
    }

    companion object {
        const val ACTION_START = "com.focuson.app.action.START"
        const val ACTION_STOP = "com.focuson.app.action.STOP"
        const val ACTION_STOP_FORCE = "com.focuson.app.action.STOP_FORCE"
        const val ACTION_REHYDRATE = "com.focuson.app.action.REHYDRATE"
        const val EXTRA_MODE_ID = "mode_id"
        const val EXTRA_DURATION_MIN = "duration_min"
        const val EXTRA_STRICT = "strict"

        private const val CHANNEL_ID = "focuson_session_v1"
        private const val NOTIF_ID = 1001

        fun startIntent(context: Context, modeId: String, durationMin: Int, strict: Boolean): Intent =
            Intent(context, BlockSessionService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_MODE_ID, modeId)
                putExtra(EXTRA_DURATION_MIN, durationMin)
                putExtra(EXTRA_STRICT, strict)
            }

        fun stopIntent(context: Context, force: Boolean): Intent =
            Intent(context, BlockSessionService::class.java).apply {
                action = if (force) ACTION_STOP_FORCE else ACTION_STOP
            }

        fun rehydrateIntent(context: Context): Intent =
            Intent(context, BlockSessionService::class.java).apply { action = ACTION_REHYDRATE }
    }
}

