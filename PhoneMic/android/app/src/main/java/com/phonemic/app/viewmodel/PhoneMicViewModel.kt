package com.phonemic.app.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.phonemic.app.audio.AudioCaptureEngine
import com.phonemic.app.model.AppScreen
import com.phonemic.app.model.ConnectionStatus
import com.phonemic.app.model.DiscoveredPc
import com.phonemic.app.model.PhoneMicUiState
import com.phonemic.app.network.AudioStreamClient
import com.phonemic.app.network.PcDiscoveryClient
import com.phonemic.app.service.MicForegroundService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PhoneMicViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(PhoneMicUiState())
    val uiState: StateFlow<PhoneMicUiState> = _uiState.asStateFlow()

    private val discoveryClient = PcDiscoveryClient()

    private val streamClient = AudioStreamClient(
        scope = viewModelScope,
        onRemoteMuteChanged = { remoteMuted ->
            audioCaptureEngine.isMuted = remoteMuted
            _uiState.update { it.copy(isMuted = remoteMuted) }
        },
        onDisconnected = { reason ->
            handleRemoteDisconnect(reason)
        }
    )

    private val audioCaptureEngine = AudioCaptureEngine(
        scope = viewModelScope,
        onAudioFrame = { pcmBytes, length, rmsDb, spectrum33 ->
            val state = _uiState.value
            if (state.connectionStatus == ConnectionStatus.CONNECTED) {
                streamClient.sendAudioFrame(
                    pcmBytes = pcmBytes,
                    length = length,
                    sampleRate = state.settings.sampleRate,
                    rmsDb = rmsDb,
                    isMuted = state.isMuted
                )
            }
            _uiState.update {
                it.copy(
                    rmsDb = rmsDb,
                    spectrumBars = spectrum33
                )
            }
        }
    )

    init {
        refreshDeviceTelemetry()
    }

    fun navigateTo(screen: AppScreen) {
        _uiState.update { it.copy(currentScreen = screen) }
    }

    fun selectConnectTab(tabIndex: Int) {
        _uiState.update { it.copy(selectedConnectTab = tabIndex) }
    }

    fun updateManualIp(ip: String) {
        _uiState.update { it.copy(manualIpInput = ip) }
    }

    fun startAutoDiscovery() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    connectionStatus = ConnectionStatus.DISCOVERING,
                    statusMessage = "Scanning local Wi-Fi for Windows PCs..."
                )
            }
            val found = discoveryClient.discoverPcs()
            if (found.isNotEmpty()) {
                val first = found.first()
                _uiState.update {
                    it.copy(
                        discoveredPcs = found,
                        manualIpInput = first.ipAddress,
                        statusMessage = "Found ${first.pcName} (${first.ipAddress})"
                    )
                }
                connectToPc(first.ipAddress, first.pcName)
            } else {
                // If user already typed an IP or we have default 192.168.1.42, attempt direct connection
                val targetIp = _uiState.value.manualIpInput.ifBlank { "192.168.1.42" }
                connectToPc(targetIp, "Windows PC")
            }
        }
    }

    fun connectToPc(ipAddress: String, pcNameHint: String = "Windows PC") {
        val cleanIp = ipAddress.trim().ifBlank { "192.168.1.42" }
        viewModelScope.launch {
            refreshDeviceTelemetry()
            _uiState.update {
                it.copy(
                    connectionStatus = ConnectionStatus.CONNECTING,
                    connectedPcIp = cleanIp,
                    connectedPcName = pcNameHint,
                    statusMessage = "Connecting to $cleanIp:${it.connectedPort}..."
                )
            }

            val current = _uiState.value
            val result = streamClient.connect(
                pcIp = cleanIp,
                port = current.connectedPort,
                sampleRate = current.settings.sampleRate,
                secureConnection = current.settings.secureConnection,
                wifiSsid = current.wifiSsid,
                batteryPercent = current.batteryPercent
            )

            val resolvedName = result.getOrElse { pcNameHint }
            startForegroundService(cleanIp)
            audioCaptureEngine.isMuted = false
            audioCaptureEngine.startCapture(current.settings.sampleRate)

            val historyItem = DiscoveredPc(
                pcName = resolvedName,
                ipAddress = cleanIp,
                port = current.connectedPort,
                encryptionRequired = current.settings.secureConnection,
                lastConnectedText = "Connected · $cleanIp"
            )

            _uiState.update { state ->
                val updatedHistory = (listOf(historyItem) + state.connectionHistory.filterNot { it.ipAddress == cleanIp }).take(5)
                state.copy(
                    currentScreen = AppScreen.DASHBOARD,
                    connectionStatus = ConnectionStatus.CONNECTED,
                    connectedPcName = resolvedName,
                    connectedPcIp = cleanIp,
                    isMuted = false,
                    connectionHistory = updatedHistory,
                    statusMessage = null
                )
            }
        }
    }

    fun toggleMute() {
        val nextMute = !_uiState.value.isMuted
        audioCaptureEngine.isMuted = nextMute
        streamClient.sendMuteState(nextMute)
        _uiState.update { it.copy(isMuted = nextMute) }
    }

    fun disconnect() {
        audioCaptureEngine.stopCapture()
        streamClient.disconnect()
        stopForegroundService()
        _uiState.update {
            it.copy(
                connectionStatus = ConnectionStatus.DISCONNECTED,
                isMuted = false,
                currentScreen = AppScreen.CONNECT,
                statusMessage = "Disconnected"
            )
        }
    }

    fun cycleSampleRate() {
        val rates = listOf(48000, 44100, 16000)
        val current = _uiState.value.settings.sampleRate
        val next = rates[(rates.indexOf(current) + 1) % rates.size]
        _uiState.update { it.copy(settings = it.settings.copy(sampleRate = next)) }
        if (_uiState.value.connectionStatus == ConnectionStatus.CONNECTED) {
            audioCaptureEngine.startCapture(next)
        }
    }

    fun toggleAutoReconnect(enabled: Boolean) {
        _uiState.update { it.copy(settings = it.settings.copy(autoReconnect = enabled)) }
    }

    fun toggleSecureConnection(enabled: Boolean) {
        _uiState.update { it.copy(settings = it.settings.copy(secureConnection = enabled)) }
    }

    fun toggleDarkMode(enabled: Boolean) {
        _uiState.update {
            it.copy(
                settings = it.settings.copy(
                    darkMode = enabled,
                    appTheme = if (enabled) "Dark" else "System"
                )
            )
        }
    }

    fun cycleAppTheme() {
        val themes = listOf("System", "Light", "Dark")
        val current = _uiState.value.settings.appTheme
        val next = themes[(themes.indexOf(current) + 1) % themes.size]
        _uiState.update {
            it.copy(
                settings = it.settings.copy(
                    appTheme = next,
                    darkMode = (next == "Dark")
                )
            )
        }
    }

    private fun handleRemoteDisconnect(reason: String) {
        val state = _uiState.value
        if (state.settings.autoReconnect && state.connectionStatus == ConnectionStatus.CONNECTED) {
            viewModelScope.launch {
                _uiState.update { it.copy(connectionStatus = ConnectionStatus.RECONNECTING, statusMessage = "Reconnecting...") }
                delay(1500L)
                connectToPc(state.connectedPcIp, state.connectedPcName)
            }
        } else {
            disconnect()
        }
    }

    private fun refreshDeviceTelemetry() {
        val context = getApplication<Application>()
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        val battery = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)?.takeIf { it in 1..100 } ?: 94

        val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        @Suppress("DEPRECATION")
        val rawSsid = runCatching { wm?.connectionInfo?.ssid?.trim('"') }.getOrNull()
        val ssid = if (rawSsid.isNullOrBlank() || rawSsid == "<unknown ssid>") "Home_5G" else rawSsid

        _uiState.update { it.copy(batteryPercent = battery, wifiSsid = ssid) }
    }

    private fun startForegroundService(pcIp: String) {
        val context = getApplication<Application>()
        val intent = Intent(context, MicForegroundService::class.java).apply {
            putExtra(MicForegroundService.EXTRA_PC_IP, pcIp)
        }
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    private fun stopForegroundService() {
        val context = getApplication<Application>()
        runCatching {
            context.stopService(Intent(context, MicForegroundService::class.java))
        }
    }

    override fun onCleared() {
        audioCaptureEngine.stopCapture()
        streamClient.disconnect()
        stopForegroundService()
        super.onCleared()
    }
}
