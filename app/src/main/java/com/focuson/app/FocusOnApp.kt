package com.focuson.app

import android.app.Application
import android.os.Build
import androidx.core.content.ContextCompat
import com.focuson.app.data.db.FocusOnDatabase
import com.focuson.app.data.prefs.SettingsStore
import com.focuson.app.data.repo.AppRepository
import com.focuson.app.data.repo.BlockRuleRepository
import com.focuson.app.domain.model.PresetMode
import com.focuson.app.service.BlockSessionService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class FocusOnApp : Application() {

    lateinit var appRepository: AppRepository
    lateinit var blockRuleRepository: BlockRuleRepository
    lateinit var settingsStore: SettingsStore

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        instance = this
        val db = FocusOnDatabase.get(this)
        appRepository = AppRepository(this)
        blockRuleRepository = BlockRuleRepository(db.blockRuleDao())
        settingsStore = SettingsStore(this)

        scope.launch {
            PresetMode.entries.forEach { blockRuleRepository.seedIfEmpty(it) }
        }

        // 서비스가 OS에 의해 종료돼도 DataStore에 세션 정보가 남아 있으면
        // 앱 프로세스 재시작 시점에 rehydrate 시도 → 만료면 정리, 아니면 복원
        scope.launch {
            val id = settingsStore.activeSessionId.firstOrNull() ?: return@launch
            val end = settingsStore.activeEndEpochMs.firstOrNull() ?: run {
                settingsStore.clearActiveSession(); return@launch
            }
            if (end <= System.currentTimeMillis()) {
                settingsStore.clearActiveSession()
                return@launch
            }
            val intent = BlockSessionService.rehydrateIntent(this@FocusOnApp)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(this@FocusOnApp, intent)
            } else {
                startService(intent)
            }
        }
    }

    companion object {
        @Volatile lateinit var instance: FocusOnApp
            private set
    }
}
