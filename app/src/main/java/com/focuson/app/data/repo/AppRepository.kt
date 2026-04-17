package com.focuson.app.data.repo

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class InstalledApp(
    val packageName: String,
    val label: String,
    val isSystem: Boolean,
)

class AppRepository(private val context: Context) {

    // 인스톨 목록은 자주 바뀌지 않음. 프로세스 수명동안 메모리 캐시.
    // 다만 includeSystem=true/false 결과를 같이 캐시하지는 않고 false만 (picker에서만 씀).
    private val cache = AtomicReference<CacheEntry?>(null)
    private val iconCache = mutableMapOf<String, Drawable?>()

    private data class CacheEntry(val includeSystem: Boolean, val list: List<InstalledApp>, val takenAtMs: Long)

    suspend fun listInstalledApps(includeSystem: Boolean = false): List<InstalledApp> {
        cache.get()?.let { entry ->
            if (entry.includeSystem == includeSystem &&
                System.currentTimeMillis() - entry.takenAtMs < CACHE_TTL_MS) {
                return entry.list
            }
        }
        return withContext(Dispatchers.IO) {
            val pm = context.packageManager
            val apps = pm.getInstalledApplications(0)
            val result = apps.asSequence()
                .filter { info ->
                    if (includeSystem) true
                    else (info.flags and ApplicationInfo.FLAG_SYSTEM) == 0 ||
                            (info.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
                }
                .filter { it.packageName != context.packageName }
                .map { info ->
                    InstalledApp(
                        packageName = info.packageName,
                        label = pm.getApplicationLabel(info).toString(),
                        isSystem = (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                    )
                }
                .sortedBy { it.label.lowercase() }
                .toList()
            cache.set(CacheEntry(includeSystem, result, System.currentTimeMillis()))
            result
        }
    }

    fun invalidate() { cache.set(null); iconCache.clear() }

    fun iconFor(packageName: String): Drawable? {
        synchronized(iconCache) {
            iconCache[packageName]?.let { return it }
        }
        val icon = runCatching { context.packageManager.getApplicationIcon(packageName) }.getOrNull()
        synchronized(iconCache) { iconCache[packageName] = icon }
        return icon
    }

    fun labelFor(packageName: String): String =
        runCatching {
            val pm = context.packageManager
            pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString()
        }.getOrDefault(packageName)

    companion object {
        private const val CACHE_TTL_MS = 60_000L * 5L   // 5분
    }
}
