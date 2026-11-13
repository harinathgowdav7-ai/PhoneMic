# PhoneMic – Wireless Android-to-Windows Microphone (`v1.0.0`)

**PhoneMic** turns your Android smartphone into a low-latency (`~ 40 ms`), 48 kHz 16-bit Mono PCM wireless microphone for your Windows 10/11 PC over local Wi-Fi and routes the audio stream directly into **VB-CABLE (`VB-Cable Input`)**.

- **License**: MIT Open Source License (`LICENSE`)
- **Zero Cloud / Zero Bloat**: No database, no cloud server, no user accounts, no subscriptions, and no local audio file recording.

---

## Repository Structure

```text
PhoneMic/
├── android/                  # Kotlin + Jetpack Compose Android transmitter app
│   ├── app/
│   │   ├── build.gradle.kts
│   │   └── src/main/
│   │       ├── AndroidManifest.xml
│   │       └── java/com/phonemic/app/
│   │           ├── MainActivity.kt
│   │           ├── audio/AudioCaptureEngine.kt
│   │           ├── model/PhoneMicState.kt
│   │           ├── network/AudioStreamClient.kt
│   │           ├── network/PcDiscoveryClient.kt
│   │           ├── protocol/PhoneMicPacket.kt
│   │           ├── service/MicForegroundService.kt
│   │           ├── viewmodel/PhoneMicViewModel.kt
│   │           └── ui/
│   │               ├── components/CustomIconsAndIllustrations.kt
│   │               ├── screens/WelcomeScreen.kt
│   │               ├── screens/ConnectScreen.kt
│   │               ├── screens/MicDashboardScreen.kt
│   │               ├── screens/SettingsScreen.kt
│   │               └── theme/Theme.kt
│   ├── build.gradle.kts
│   ├── gradle.properties
│   └── settings.gradle.kts
├── windows/                  # C# + .NET 8 WPF Windows 10/11 desktop receiver
│   ├── PhoneMic.sln
│   ├── PhoneMic.Desktop/
│   │   ├── PhoneMic.Desktop.csproj
│   │   ├── App.xaml & App.xaml.cs
│   │   ├── MainWindow.xaml & MainWindow.xaml.cs
│   │   ├── Audio/VbCableAudioRouter.cs
│   │   ├── Network/PhoneMicServer.cs
│   │   ├── Protocol/PhoneMicPacketDecoder.cs
│   │   └── ViewModels/MainViewModel.cs
│   └── Installer/
│       └── PhoneMicSetup.iss # Inno Setup 6 Windows 11 installer script
├── shared/                   # Wire protocol & framing specification
│   ├── protocol-spec.md
│   └── phonemic_protocol.json
├── tests/                    # Automated protocol, DSP, jitter-buffer & UDP streaming tests
│   └── test_phonemic_protocol.py
├── documentation/            # Architecture, UI/UX & VB-CABLE setup guides
│   ├── ARCHITECTURE.md
│   └── VB_CABLE_SETUP.md
├── .github/workflows/        # Automated GitHub Actions APK + Windows Installer release pipeline
│   └── build-and-release.yml
├── publish_to_github.ps1     # One-command GitHub repository creation & release publisher
├── README.md
├── LICENSE
└── .gitignore
```

---

## 1:1 UI/UX Design Implementation

### Android Application (Kotlin + Jetpack Compose)
Replicates all 4 reference screens (`kk.jpeg`) with exact colors (`#15803D`, `#16A34A`, `#22C55E`, `#E8F8EE`, `#EF4444`, `#F8FAFC`), typography, and layouts:
1. **Welcome Screen (`WelcomeScreen.kt`)**: Rounded-square green microphone emblem, `PhoneMic` header, 3-line headline (*Use your phone as a wireless microphone for your Windows PC*), 3 feature rows (*Real-time Audio*, *Simple Connection*, *Lightweight & Free*), green `Get Started →` pill button, 3 pagination dots, and bottom soft green wave.
2. **Connect to PC Screen (`ConnectScreen.kt`)**: Segmented `Connect` / `History` tabs, custom vector green-screen Windows laptop + smartphone Wi-Fi illustration, `Auto Discover PCs` primary pill button, `— OR —` divider, and `Enter PC IP address` input with desktop and QR scanner icons.
3. **Microphone Dashboard (`MicDashboardScreen.kt`)**: Top connected PC card (`● Connected` / `Windows PC (192.168.1.42)`), glowing green circular microphone emblem, 33-bar live green audio spectrum visualizer, circular `Mute` / `Disconnect` / `Settings` buttons, and bottom `Wi-Fi: Home_5G | 48 kHz · PCM` status bar.
4. **Settings Screen (`SettingsScreen.kt`)**: Grouped cards for **Audio** (`Sample Rate: 48 kHz`, `Audio Format: PCM (16-bit)`, `Channels: Mono`), **Connection** (`Auto Reconnect`, `Secure Connection`), **Appearance** (`Dark Mode`, `App Theme: System`), and **About** (`About PhoneMic`).

### Windows 10/11 Application (C# + .NET 8 WPF)
Replicates the desktop interface across all sidebar views (`MainWindow.xaml`):
- **Dashboard**: Top `● Running` pill badge + gear button, `Connected` card (`Phone: iQOO Neo 10 | IP: 192.168.1.42`, `Wi-Fi` badge, glowing center mic circle flanked by left/right spectrum bars, circular `Mute` / `Disconnect` / `Settings` buttons), `Input Level` (`-12 dB`) bar, `Virtual Microphone` card (`✔ Ready`, `VB-Cable Input (Virtual Microphone)` dropdown, info banner), and `Audio Information` card (`48 kHz`, `PCM (16-bit)`, `Mono`, `~ 40 ms`).
- **Devices**: `My Devices` list (`iQOO Neo 10`, `Harinath's Phone`, `Android Device`) with `⊕ Pair New Device` button, selected device profile card (`Android 15 | PhoneMic v1.0.0`, `IP Address 192.168.1.42`, `Port 55123`, `Battery 94%`, `Wi-Fi Home_5G`), `Live Audio from Device` 36-bar waveform, and `[Mute]`, `[Device Settings]`, `[Disconnect]` action row.
- **Audio**: `Audio Input (From Phone)` card with live spectrum and `-12 dB` level meter, `Audio Output (Virtual Microphone)` card, and `Quality & Performance` card (`High (48 kHz, 16-bit)`, `Low (~ 40 ms)`).
- **Network**: `Connection` card (`Wi-Fi (Home_5G)`, `192.168.1.42`, `55123`, `● Listening for connections`), `Connection Security` card (`Use encrypted connection (recommended)`), `Network Performance` card (`~ 40 ms`, `0.9 Mbps`, `0.0 %` + green area graph), and `Advanced` card (`Allow connections from the same network only`, `Auto discover devices`).

---

## Build, Installation & Publishing Instructions

### 1. Publish Directly to Your GitHub Account & Build Release Binaries Automatically
Extract `PhoneMic-v1.0.0-Complete-Repository.zip`, open PowerShell in the `PhoneMic` folder, and run:
```powershell
.\publish_to_github.ps1
```
Or push manually with Git:
```powershell
git remote add origin https://github.com/<YOUR_GITHUB_USERNAME>/PhoneMic.git
git push -u origin main --tags
```
The bundled GitHub Actions workflow (`.github/workflows/build-and-release.yml`) automatically compiles and attaches both release binaries to your GitHub Releases page under tag `v1.0.0`:
- **Android APK**: `https://github.com/<YOUR_GITHUB_USERNAME>/PhoneMic/releases/download/v1.0.0/PhoneMic-v1.0.0.apk`
- **Windows 11 Installer**: `https://github.com/<YOUR_GITHUB_USERNAME>/PhoneMic/releases/download/v1.0.0/PhoneMic-Windows-Setup-v1.0.0.exe`

### 2. Local Android APK Build & Installation
```bash
cd android
./gradlew assembleRelease
# Output APK: android/app/build/outputs/apk/release/app-release.apk
adb install -r app/build/outputs/apk/release/app-release.apk
```

### 3. Local Windows 10/11 Application & Installer Build
```powershell
cd windows
dotnet publish PhoneMic.Desktop/PhoneMic.Desktop.csproj -c Release -r win-x64 --self-contained true
# Optional: Build Inno Setup installer (.exe)
iscc Installer/PhoneMicSetup.iss
# Output Installer: windows/dist/PhoneMic-Windows-Setup-v1.0.0.exe
```

### 4. VB-CABLE Virtual Microphone Setup (Required on Windows)
1. Download the free **VB-CABLE Virtual Audio Device** from `https://vb-audio.com/Cable/`.
2. Extract the archive, right-click `VBCABLE_Setup_x64.exe`, and choose **Run as administrator**.
3. Open **PhoneMic** on Windows — it automatically detects and routes incoming phone audio to **`VB-Cable Input (Virtual Microphone)`**.
4. In **OBS Studio, Zoom, Discord, Microsoft Teams, or ChatGPT**, select **`CABLE Output (VB-Audio Virtual Cable)`** as your microphone input.
