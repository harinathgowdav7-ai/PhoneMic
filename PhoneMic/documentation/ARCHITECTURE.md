# PhoneMic Architecture, Audio Pipeline & Testing Guide

## 1. System Architecture

PhoneMic consists of two native applications communicating directly over a local Wi-Fi network without any cloud backend, external database, or user account system:

1. **Android Transmitter (`android/`)**:
   - **UI Layer**: Kotlin + Jetpack Compose (`WelcomeScreen.kt`, `ConnectScreen.kt`, `MicDashboardScreen.kt`, `SettingsScreen.kt`, `CustomIconsAndIllustrations.kt`).
   - **Audio Capture (`AudioCaptureEngine.kt`)**: Uses `android.media.AudioRecord` with `MediaRecorder.AudioSource.VOICE_COMMUNICATION`, `48000 Hz`, `CHANNEL_IN_MONO`, `ENCODING_PCM_16BIT`, and 10 ms frame slicing (`480` samples / `960` bytes per frame). Enables hardware `NoiseSuppressor`, `AcousticEchoCanceler`, and `AutomaticGainControl` when supported by the handset DSP.
   - **Networking (`PcDiscoveryClient.kt` & `AudioStreamClient.kt`)**: Broadcasts UDP discovery probes on port `55123`, establishes a TCP control session for pairing/telemetry/mute synchronization, and transmits AES-256-GCM encrypted or raw 24-byte header `PMIC` UDP datagrams.
   - **Background Execution (`MicForegroundService.kt`)**: Android `FOREGROUND_SERVICE_MICROPHONE` service holding a partial wake lock for uninterrupted streaming while the screen is off.

2. **Windows 10/11 Receiver (`windows/`)**:
   - **UI Layer**: C# .NET 8 WPF (`MainWindow.xaml`, `MainViewModel.cs`) implementing the 6-section sidebar (`Dashboard`, `Devices`, `Audio`, `Network`, `Settings`, `About`).
   - **Network & Crypto Engine (`PhoneMicServer.cs` & `PhoneMicPacketDecoder.cs`)**: Listens on UDP/TCP port `55123`, answers UDP discovery probes, derives session keys with HKDF-SHA256, verifies AES-256-GCM tags, tracks sequence numbers/packet loss/throughput (`~ 0.9 Mbps`), and computes 36-band spectrum envelopes.
   - **Virtual Microphone Router (`VbCableAudioRouter.cs`)**: Enumerates Windows WASAPI render endpoints via NAudio `MMDeviceEnumerator`, auto-selects `VB-Cable Input (Virtual Microphone)`, and plays incoming PCM frames through an event-synced `WasapiOut` jitter buffer (`~ 40 ms` end-to-end latency).

---

## 2. Exact 1:1 UI/UX Design Fidelity

Both applications strictly match the reference specifications:

- **Color Palette**:
  - Primary Brand Green: `#15803D` / `#16A34A`
  - Active Ring & Waveform Accent: `#22C55E`
  - Soft Green Pill / Active Highlight: `#DCFCE7` / `#E8F8EE`
  - Disconnect Red: `#EF4444` (soft red button fill `#FEF2F2`, border `#FCA5A5`)
  - Surface & Card Palette: `#F8FAFC` app canvas, `#FFFFFF` cards, `#E2E8F0` / `#E5E7EB` 1px borders, `#0F172A` primary slate typography, `#64748B` secondary labels.
- **Android 4-Screen Flow (`kk.jpeg`)**:
  1. **Welcome Screen**: App badge + `PhoneMic`, 3-line headline, 3 feature rows (`Real-time Audio`, `Simple Connection`, `Lightweight & Free`), green `Get Started →` pill button, 3 pagination dots, bottom green wave.
  2. **Connect to PC Screen**: `Connect` / `History` segmented tabs, custom Canvas green-screen Windows laptop + smartphone Wi-Fi illustration, `Auto Discover PCs` button, `— OR —` divider, and `Enter PC IP address` input with monitor & scanner icons.
  3. **Microphone Dashboard**: Connected PC card (`Windows PC (192.168.1.42)`), glowing green microphone circle, 33-bar live green spectrum, circular `Mute` / `Disconnect` / `Settings` buttons, and bottom `Wi-Fi: Home_5G | 48 kHz · PCM` status bar.
  4. **Settings Screen**: Grouped cards for `Audio` (`Sample Rate`, `Audio Format`, `Channels`), `Connection` (`Auto Reconnect`, `Secure Connection`), `Appearance` (`Dark Mode`, `App Theme`), and `About` (`About PhoneMic`).
- **Windows Desktop Views**:
  - **Dashboard**: Top `● Running` pill + gear icon, `Connected` card (`Phone: iQOO Neo 10 | IP: 192.168.1.42`) with `Wi-Fi` badge, central glowing microphone circle flanked by left/right spectrum bars, circular `Mute` / `Disconnect` / `Settings` controls, bottom `Input Level` (`-12 dB`) bar, right `Virtual Microphone` (`✔ Ready`, `VB-Cable Input (Virtual Microphone)`) card, and `Audio Information` card (`48 kHz`, `PCM (16-bit)`, `Mono`, `~ 40 ms`).
  - **Devices**: `My Devices` list (`iQOO Neo 10`, `Harinath's Phone`, `Android Device`) with `⊕ Pair New Device` button, device profile card (`Android 15 | PhoneMic v1.0.0`, `IP Address 192.168.1.42`, `Port 55123`, `Battery 94%`, `Wi-Fi Home_5G`), `Live Audio from Device` waveform, and `[Mute]`, `[Device Settings]`, `[Disconnect]` action row.
  - **Audio**: `Audio Input (From Phone)` card with waveform + specs + `Current Level (-12 dB)`, `Audio Output (Virtual Microphone)` card, and `Quality & Performance` card (`High (48 kHz, 16-bit)`, `Low (~ 40 ms)`).
  - **Network**: `Connection` card (`Wi-Fi (Home_5G)`, `192.168.1.42`, `55123`, `● Listening for connections`), `Connection Security` card, `Network Performance` card (`~ 40 ms`, `0.9 Mbps`, `0.0 %` + green area chart), and `Advanced` card (`Allow connections from the same network only`, `Auto discover devices`).
