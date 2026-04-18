package com.focuson.app

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.focuson.app.domain.model.PresetMode
import com.focuson.app.service.BlockSessionService
import com.focuson.app.ui.custom.AppPickerScreen
import com.focuson.app.ui.custom.ModeDetailScreen
import com.focuson.app.ui.custom.SiteRulesScreen
import com.focuson.app.ui.home.HomeScreen
import com.focuson.app.ui.onboarding.PermissionScreen
import com.focuson.app.ui.session.SessionScreen
import com.focuson.app.ui.theme.FocusOnTheme
import com.focuson.app.util.PermissionChecker
import com.focuson.app.widget.FocusOnWidgetProvider
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    // 위젯에서 전달된 preset id — Compose 에서 관측 가능하도록 mutableState
    private var widgetPresetId by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        widgetPresetId = intent?.getStringExtra(FocusOnWidgetProvider.EXTRA_WIDGET_PRESET_ID)
        setContent {
            FocusOnTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    FocusOnRoot(
                        widgetPresetId = widgetPresetId,
                        onConsumeWidgetPreset = { widgetPresetId = null },
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.getStringExtra(FocusOnWidgetProvider.EXTRA_WIDGET_PRESET_ID)?.let {
            widgetPresetId = it
        }
    }
}

@Composable
private fun FocusOnRoot(
    widgetPresetId: String?,
    onConsumeWidgetPreset: () -> Unit,
) {
    val nav = rememberNavController()
    val context = LocalContext.current
    val app = FocusOnApp.instance
    val scope = rememberCoroutineScope()
    val onboardingDone by app.settingsStore.onboardingDone.collectAsStateWithLifecycle(initialValue = false)
    val activeSessionId by app.settingsStore.activeSessionId.collectAsStateWithLifecycle(initialValue = null)

    val start = when {
        !onboardingDone -> "permission"
        activeSessionId != null -> "session"
        else -> "home"
    }

    NavHost(navController = nav, startDestination = start) {
        composable("permission") {
            PermissionScreen(onDone = {
                scope.launch { app.settingsStore.setOnboardingDone(true) }
                nav.navigate("home") { popUpTo("permission") { inclusive = true } }
            })
        }
        composable("home") {
            val initialPreset = remember(widgetPresetId) {
                widgetPresetId?.let(PresetMode::fromId)
            }
            // 소비 후에는 다시 같은 탭 = 같은 값이 와도 반응하도록 초기 상태만 사용
            HomeScreen(
                initialExpandedMode = initialPreset,
                onPresetConsumed = onConsumeWidgetPreset,
                onStartPreset = { mode, minutes, strict ->
                    val ok = PermissionChecker.accessibilityGranted(context) && PermissionChecker.overlayGranted(context)
                    if (!ok) {
                        nav.navigate("permission")
                    } else {
                        val intent = BlockSessionService.startIntent(context, mode.id, minutes, strict)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            ContextCompat.startForegroundService(context, intent)
                        } else {
                            context.startService(intent)
                        }
                        nav.navigate("session")
                    }
                },
                onOpenCustomize = { mode -> nav.navigate("mode/${mode.id}") },
                onOpenSettings = { nav.navigate("permission") },
            )
        }
        composable("session") {
            SessionScreen(
                onEndSession = { force ->
                    val intent = BlockSessionService.stopIntent(context, force)
                    context.startService(intent)
                    nav.navigate("home") { popUpTo("session") { inclusive = true } }
                },
            )
        }
        composable("mode/{modeId}") { backStack ->
            val id = backStack.arguments?.getString("modeId") ?: return@composable
            val mode = PresetMode.fromId(id) ?: return@composable
            ModeDetailScreen(
                mode = mode,
                onPickApps = { nav.navigate("mode/$id/apps") },
                onPickSites = { nav.navigate("mode/$id/sites") },
                onBack = { nav.navigate("home") { popUpTo("home") { inclusive = false } } },
            )
        }
        composable("mode/{modeId}/apps") { backStack ->
            val id = backStack.arguments?.getString("modeId") ?: return@composable
            val mode = PresetMode.fromId(id) ?: return@composable
            AppPickerScreen(mode = mode, onDone = { nav.popBackStack() })
        }
        composable("mode/{modeId}/sites") { backStack ->
            val id = backStack.arguments?.getString("modeId") ?: return@composable
            val mode = PresetMode.fromId(id) ?: return@composable
            SiteRulesScreen(mode = mode, onDone = { nav.popBackStack() })
        }
    }
}
