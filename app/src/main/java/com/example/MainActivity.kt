package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.ElectricBolt
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainViewModel
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LogsScreen
import com.example.ui.screens.ProtectionScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    AbuThalamApp()
                }
            }
        }
    }
}

sealed class NavTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    data object Dashboard : NavTab("الرئيسية", Icons.Filled.ElectricBolt, Icons.Outlined.ElectricBolt, "tab_dashboard")
    data object Settings : NavTab("التنبيهات", Icons.Filled.Settings, Icons.Outlined.Settings, "tab_settings")
    data object Logs : NavTab("السجل", Icons.Filled.History, Icons.Outlined.History, "tab_logs")
    data object Protection : NavTab("درع الحماية", Icons.Filled.Security, Icons.Outlined.Security, "tab_protection")
}

@Composable
fun AbuThalamApp(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = remember {
        listOf(NavTab.Dashboard, NavTab.Settings, NavTab.Logs, NavTab.Protection)
    }

    // Permission launcher for Android 13+ Notification permission
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                viewModel.startGuardianService()
            }
        }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionCheck = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Handle system back navigation: return to Dashboard tab if on another tab
    BackHandler(enabled = selectedTabIndex != 0) {
        selectedTabIndex = 0
    }

    val batteryInfo by viewModel.currentBattery.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val logs by viewModel.logs.collectAsStateWithLifecycle()
    val isServiceRunning by viewModel.isServiceRunning.collectAsStateWithLifecycle()
    val voiceState by viewModel.voiceEngineState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkBackground,
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_nav_bar")
            ) {
                tabs.forEachIndexed { index, tab ->
                    val isSelected = selectedTabIndex == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTabIndex = index },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyberAmber,
                            selectedTextColor = CyberAmber,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = DarkSurfaceVariant
                        ),
                        modifier = Modifier.testTag(tab.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DarkBackground)
        ) {
            when (selectedTabIndex) {
                0 -> DashboardScreen(
                    batteryInfo = batteryInfo,
                    settings = settings,
                    isServiceRunning = isServiceRunning,
                    voiceState = voiceState,
                    onToggleService = { viewModel.toggleService(it) },
                    onTestHighAlert = { viewModel.testHighAlert() },
                    onTestLowAlert = { viewModel.testLowAlert() },
                    onStopVoice = { viewModel.stopSpeaking() }
                )
                1 -> SettingsScreen(
                    settings = settings,
                    onSaveHighSettings = { enabled, threshold, message ->
                        viewModel.updateHighSettings(enabled, threshold, message)
                    },
                    onSaveLowSettings = { enabled, threshold, message ->
                        viewModel.updateLowSettings(enabled, threshold, message)
                    },
                    onSaveAudioAndGeneral = { vibration, rate, pitch, interval, boot, plugged, unplugged ->
                        viewModel.updateAudioAndGeneralSettings(
                            vibration, rate, pitch, interval, boot, plugged, unplugged
                        )
                    },
                    onSetVoiceProfile = { viewModel.setVoiceProfile(it) },
                    onTestHigh = { viewModel.testHighAlert() },
                    onTestLow = { viewModel.testLowAlert() }
                )
                2 -> LogsScreen(
                    logs = logs,
                    onClearLogs = { viewModel.clearLogs() }
                )
                3 -> ProtectionScreen()
            }
        }
    }
}
