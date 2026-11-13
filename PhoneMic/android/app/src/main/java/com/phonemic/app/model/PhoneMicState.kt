package com.phonemic.app.model

enum class AppScreen {
    WELCOME,
    CONNECT,
    DASHBOARD,
    SETTINGS
}

enum class ConnectionStatus {
    DISCONNECTED,
    DISCOVERING,
    CONNECTING,
    CONNECTED,
    RECONNECTING,
    ERROR
}

data class DiscoveredPc(
    val pcName: String,
    val ipAddress: String,
    val port: Int = 55123,
    val encryptionRequired: Boolean = true,
    val lastConnectedText: String? = null
)

data class AudioSettings(
    val sampleRate: Int = 48000,
    val audioFormat: String = "PCM (16-bit)",
    val channels: String = "Mono",
    val autoReconnect: Boolean = true,
    val secureConnection: Boolean = true,
    val darkMode: Boolean = false,
    val appTheme: String = "System"
) {
    val sampleRateLabel: String
        get() = "${sampleRate / 1000} kHz"
}

data class PhoneMicUiState(
    val currentScreen: AppScreen = AppScreen.WELCOME,
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val connectedPcName: String = "Windows PC",
    val connectedPcIp: String = "192.168.1.42",
    val connectedPort: Int = 55123,
    val manualIpInput: String = "",
    val selectedConnectTab: Int = 0, // 0 = Connect, 1 = History
    val discoveredPcs: List<DiscoveredPc> = emptyList(),
    val connectionHistory: List<DiscoveredPc> = listOf(
        DiscoveredPc("Windows PC", "192.168.1.42", 55123, true, "Recent · 192.168.1.42")
    ),
    val isMuted: Boolean = false,
    val wifiSsid: String = "Home_5G",
    val batteryPercent: Int = 94,
    val rmsDb: Float = -12.0f,
    val spectrumBars: List<Float> = List(33) { index ->
        // Default initial visual contour before live mic frames arrive
        val centerDist = kotlin.math.abs(index - 16) / 16f
        (0.25f + 0.55f * (1f - centerDist)).coerceIn(0.12f, 0.92f)
    },
    val settings: AudioSettings = AudioSettings(),
    val statusMessage: String? = null
)
