package com.focuson.app.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "focuson_settings")

class SettingsStore(private val context: Context) {

    val activeSessionId: Flow<Long?> = context.dataStore.data.map { it[KEY_ACTIVE_SESSION_ID] }
    val activeModeId: Flow<String?> = context.dataStore.data.map { it[KEY_ACTIVE_MODE_ID] }
    val activeEndEpochMs: Flow<Long?> = context.dataStore.data.map { it[KEY_ACTIVE_END_MS] }
    val activeStrict: Flow<Boolean> = context.dataStore.data.map { it[KEY_ACTIVE_STRICT] ?: false }
    val onboardingDone: Flow<Boolean> = context.dataStore.data.map { it[KEY_ONBOARDING_DONE] ?: false }

    val proTierId: Flow<String> = context.dataStore.data.map { it[KEY_PRO_TIER_ID] ?: "free" }
    val proEmail: Flow<String?> = context.dataStore.data.map { it[KEY_PRO_EMAIL] }
    val proLicenseKey: Flow<String?> = context.dataStore.data.map { it[KEY_PRO_LICENSE_KEY] }

    suspend fun setActiveSession(id: Long?, modeId: String?, endMs: Long?, strict: Boolean) {
        context.dataStore.edit { prefs ->
            if (id != null) prefs[KEY_ACTIVE_SESSION_ID] = id else prefs.remove(KEY_ACTIVE_SESSION_ID)
            if (modeId != null) prefs[KEY_ACTIVE_MODE_ID] = modeId else prefs.remove(KEY_ACTIVE_MODE_ID)
            if (endMs != null) prefs[KEY_ACTIVE_END_MS] = endMs else prefs.remove(KEY_ACTIVE_END_MS)
            prefs[KEY_ACTIVE_STRICT] = strict
        }
    }

    suspend fun clearActiveSession() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_ACTIVE_SESSION_ID)
            prefs.remove(KEY_ACTIVE_MODE_ID)
            prefs.remove(KEY_ACTIVE_END_MS)
            prefs[KEY_ACTIVE_STRICT] = false
        }
    }

    suspend fun setOnboardingDone(done: Boolean) {
        context.dataStore.edit { it[KEY_ONBOARDING_DONE] = done }
    }

    suspend fun setProLicense(tierId: String, email: String, licenseKey: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_PRO_TIER_ID] = tierId
            prefs[KEY_PRO_EMAIL] = email
            prefs[KEY_PRO_LICENSE_KEY] = licenseKey
        }
    }

    suspend fun clearProLicense() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_PRO_TIER_ID)
            prefs.remove(KEY_PRO_EMAIL)
            prefs.remove(KEY_PRO_LICENSE_KEY)
        }
    }

    companion object {
        private val KEY_ACTIVE_SESSION_ID = longPreferencesKey("active_session_id")
        private val KEY_ACTIVE_MODE_ID = stringPreferencesKey("active_mode_id")
        private val KEY_ACTIVE_END_MS = longPreferencesKey("active_end_ms")
        private val KEY_ACTIVE_STRICT = booleanPreferencesKey("active_strict")
        private val KEY_ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        private val KEY_PRO_TIER_ID = stringPreferencesKey("pro_tier_id")
        private val KEY_PRO_EMAIL = stringPreferencesKey("pro_email")
        private val KEY_PRO_LICENSE_KEY = stringPreferencesKey("pro_license_key")
    }
}
