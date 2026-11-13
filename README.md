# PhoneMic

## Download PhoneMic (v1.0.0)
- **Download for Android (Phone APK):** [PhoneMic-v1.0.0.apk](../../releases/latest/download/PhoneMic-v1.0.0.apk)
- **Download for Windows 10/11 (PC Installer):** [PhoneMic-Windows-Setup-v1.0.0.exe](../../releases/latest/download/PhoneMic-Windows-Setup-v1.0.0.exe)
- **Download Complete Package (ZIP):** [PhoneMic-v1.0.0-Complete-Repository.zip](./PhoneMic-v1.0.0-Complete-Repository.zip?raw=true)
- **All Releases Page:** [View GitHub Releases](../../releases)

Turn your Android smartphone into a low-latency 48 kHz wireless microphone for Windows 10/11 over Wi-Fi with VB-CABLE routing.

# PhoneMic v1.0.0

Turn your Android smartphone into a low-latency 48 kHz wireless microphone for Windows 10/11 over Wi-Fi with VB-CABLE routing.

## Overview
PhoneMic v1.0.0 enables low-latency audio capture on Android devices and streams high-fidelity raw PCM audio over Wi-Fi to a Windows receiver application. The receiver routes incoming audio directly into VB-CABLE Virtual Audio Device, allowing your smartphone to act as a system-wide microphone for Discord, Zoom, OBS, and other applications.

## Repository Structure
- `android/` - Android client application built with Kotlin and Jetpack Compose.
- `windows/` - Windows receiver application built with C# and .NET 8 WPF.
- `shared/` - Shared network protocol definitions, constants, and packet formats.
- `tests/` - Unit tests, integration tests, and audio latency benchmarks.
- `documentation/` - Architecture documentation, user guides, and API specs.

## Audio Streaming Protocol
- **Sample Rate:** 48 kHz (48,000 Hz)
- **Bit Depth:** 16-bit PCM (Pulse Code Modulation), Mono
- **Transport:** Low-latency UDP / TCP streaming over local Wi-Fi network
- **Port:** 55123
- **Packet Format:** 1024 samples per frame (~21.3 ms audio frame duration)

## VB-CABLE Setup Instructions
1. Download and install the VB-CABLE Virtual Audio Device driver on Windows.
2. Open the PhoneMic Windows receiver app.
3. Select `CABLE Input (VB-Audio Virtual Cable)` as the target output device.
4. In your preferred Windows communication software (e.g. Discord, Zoom, OBS, Teams), set the input device to `CABLE Output (VB-Audio Virtual Cable)`. Windows 10/11 over Wi-Fi with VB-CABLE routing.
# PhoneMic v1.0.0

Turn your Android smartphone into a low-latency 48 kHz wireless microphone for Windows 10/11 over Wi-Fi with VB-CABLE routing.

## Overview
PhoneMic v1.0.0 enables low-latency audio capture on Android devices and streams high-fidelity raw PCM audio over Wi-Fi to a Windows receiver application. The receiver routes incoming audio directly into VB-CABLE Virtual Audio Device, allowing your smartphone to act as a system-wide microphone for Discord, Zoom, OBS, and other applications.

## Repository Structure
- `android/` - Android client application built with Kotlin and Jetpack Compose.
- `windows/` - Windows receiver application built with C# and .NET 8 WPF.
- `shared/` - Shared network protocol definitions, constants, and packet formats.
- `tests/` - Unit tests, integration tests, and audio latency benchmarks.
- `documentation/` - Architecture documentation, user guides, and API specs.

## Audio Streaming Protocol
- **Sample Rate:** 48 kHz (48,000 Hz)
- **Bit Depth:** 16-bit PCM (Pulse Code Modulation), Mono
- **Transport:** Low-latency UDP / TCP streaming over local Wi-Fi network
- **Port:** 55123
- **Packet Format:** 1024 samples per frame (~21.3 ms audio frame duration)

## VB-CABLE Setup Instructions
1. Download and install the VB-CABLE Virtual Audio Device driver on Windows.
2. Open the PhoneMic Windows receiver app.
3. Select `CABLE Input (VB-Audio Virtual Cable)` as the target output device.
4. In your preferred Windows communication software (e.g. Discord, Zoom, OBS, Teams), set the input audio/microphone device to `CABLE Output (VB-Audio Virtual Cable)`.

## Build Instructions

### Android Client (Kotlin + Jetpack Compose)
1. Open the `android/` directory in Android Studio.
2. Build the project using Gradle: `./gradlew assembleDebug`
3. Install and run on an Android device running Android 8.0+ (API level 26+).

### Windows Receiver (C# + .NET 8 WPF)
1. Open the `windows/` directory in Visual Studio 2022 or VS Code.
2. Restore NuGet dependencies: `dotnet restore`
3. Build and run the project: `dotnet run --project PhoneMic.csproj`
