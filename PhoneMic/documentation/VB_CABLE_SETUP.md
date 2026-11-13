# VB-CABLE Virtual Audio Driver Setup & Integration Guide

PhoneMic routes live 48 kHz 16-bit Mono PCM audio received from your Android smartphone into **VB-CABLE** (`VB-Cable Input`), exposing your phone as a standard Windows microphone (`CABLE Output`) across Zoom, OBS Studio, Discord, Microsoft Teams, and ChatGPT.

---

## 1. Installing VB-CABLE on Windows 10/11

1. Download the free **VB-CABLE Virtual Audio Device** package from the official VB-Audio website:
   - Official page: `https://vb-audio.com/Cable/`
2. Extract the downloaded ZIP archive (`VBCABLE_Driver_Pack43.zip` or newer).
3. Right-click **`VBCABLE_Setup_x64.exe`** and select **Run as administrator**.
4. Click **Install Driver** and reboot Windows if prompted.

---

## 2. How PhoneMic Connects to VB-CABLE

1. Launch **PhoneMic** on your Windows 10/11 laptop.
2. In the **Dashboard** or **Audio** tab, check the **Virtual Microphone** card:
   - **Output Device** automatically selects **`VB-Cable Input (Virtual Microphone)`**.
   - The green status badge displays **`✔ Ready`** (`Virtual Microphone Ready`).
3. Internally, `VbCableAudioRouter.cs` uses NAudio's low-latency `WasapiOut` in event-driven shared mode to stream the decoded 48 kHz 16-bit Mono PCM samples directly into the **`CABLE Input (VB-Audio Virtual Cable)`** render endpoint.

---

## 3. Selecting PhoneMic in Your Applications (OBS, Zoom, Discord, ChatGPT)

Because PhoneMic feeds audio into **`VB-Cable Input`**, Windows automatically loops that signal out through the recording device named **`CABLE Output (VB-Audio Virtual Cable)`**:

- **Windows 11 Sound Settings**: Go to *Settings → System → Sound → Input* and select **CABLE Output (VB-Audio Virtual Cable)** (match format to `1 channel, 16 bit, 48000 Hz (DVD Quality)` in Advanced Properties for zero resampling latency).
- **Zoom / Microsoft Teams / Discord**: Open *Audio Settings → Microphone* and choose **CABLE Output (VB-Audio Virtual Cable)**.
- **OBS Studio**: Under *Settings → Audio → Global Audio Devices → Mic/Auxiliary Audio*, select **CABLE Output (VB-Audio Virtual Cable)**.
