package com.phonemic.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.phonemic.app.model.AppScreen
import com.phonemic.app.model.ConnectionStatus
import com.phonemic.app.ui.screens.ConnectScreen
import com.phonemic.app.ui.screens.MicDashboardScreen
import com.phonemic.app.ui.screens.SettingsScreen
import com.phonemic.app.ui.screens.WelcomeScreen
import com.phonemic.app.ui.theme.PhoneMicBgLight
import com.phonemic.app.ui.theme.PhoneMicTheme
import com.phonemic.app.viewmodel.PhoneMicViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: PhoneMicViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestAudioAndNotificationPermissions()

        setContent {
            val state by viewModel.uiState.collectAsState()

            PhoneMicTheme(darkMode = state.settings.darkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = PhoneMicBgLight
                ) {
                    Crossfade(targetState = state.currentScreen, label = "PhoneMicNavigation") { screen ->
                        when (screen) {
                            AppScreen.WELCOME -> {
                                WelcomeScreen(
                                    onGetStartedClick = {
                                        viewModel.navigateTo(AppScreen.CONNECT)
                                    }
                                )
                            }

                            AppScreen.CONNECT -> {
                                ConnectScreen(
                                    selectedTab = state.selectedConnectTab,
                                    manualIp = state.manualIpInput,
                                    history = state.connectionHistory,
                                    onTabSelected = viewModel::selectConnectTab,
                                    onManualIpChanged = viewModel::updateManualIp,
                                    onAutoDiscoverClick = viewModel::startAutoDiscovery,
                                    onConnectIpClick = { ip -> viewModel.connectToPc(ip) },
                                    onOpenSettingsClick = { viewModel.navigateTo(AppScreen.SETTINGS) }
                                )
                            }

                            AppScreen.DASHBOARD -> {
                                MicDashboardScreen(
                                    pcName = state.connectedPcName,
                                    pcIp = state.connectedPcIp,
                                    isMuted = state.isMuted,
                                    rmsDb = state.rmsDb,
                                    spectrumBars = state.spectrumBars,
                                    wifiSsid = state.wifiSsid,
                                    sampleRateLabel = state.settings.sampleRateLabel,
                                    onBackClick = { viewModel.navigateTo(AppScreen.CONNECT) },
                                    onToggleMute = viewModel::toggleMute,
                                    onDisconnectClick = viewModel::disconnect,
                                    onSettingsClick = { viewModel.navigateTo(AppScreen.SETTINGS) }
                                )
                            }

                            AppScreen.SETTINGS -> {
                                SettingsScreen(
                                    settings = state.settings,
                                    onBackClick = {
                                        val target = if (state.connectionStatus == ConnectionStatus.CONNECTED) {
                                            AppScreen.DASHBOARD
                                        } else {
                                            AppScreen.CONNECT
                                        }
                                        viewModel.navigateTo(target)
                                    },
                                    onSampleRateClick = viewModel::cycleSampleRate,
                                    onAutoReconnectChange = viewModel::toggleAutoReconnect,
                                    onSecureConnectionChange = viewModel::toggleSecureConnection,
                                    onDarkModeChange = viewModel::toggleDarkMode,
                                    onAppThemeClick = viewModel::cycleAppTheme
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun requestAudioAndNotificationPermissions() {
        val perms = mutableListOf(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            perms.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        val missing = perms.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) {
            permissionLauncher.launch(missing.toTypedArray())
        }
    }
}
