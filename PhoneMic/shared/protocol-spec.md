# PhoneMic Wire Protocol & Shared Architecture Specification (v1.0.0)

## 1. Overview

PhoneMic transmits real-time, low-latency microphone audio from an Android smartphone to a Windows 10/11 PC over a local Wi-Fi network and routes the received stream into a virtual microphone device (`VB-Cable Input`).

- **Default Port**: `55123` (UDP for auto-discovery & real-time audio frames; TCP for pairing, control handshake, telemetry, and mute/disconnect synchronization)
- **Default Audio Format**: `PCM (16-bit)` signed little-endian (`s16le`)
- **Sample Rates**: `48000 Hz` (default, high quality), `44100 Hz`, `16000 Hz`
- **Channels**: `1` (Mono)
- **Frame Duration**:
  - **Low Latency Mode (`~ 40 ms` end-to-end)**: `10 ms` per packet (`480` samples = `960` bytes payload at 48 kHz)
  - **Balanced Mode (`~ 60 ms`)**: `20 ms` per packet (`960` samples = `1920` bytes payload at 48 kHz)

## 2. Discovery Protocol (UDP Port 55123)

When the Android user taps **Auto Discover PCs** (or when **Auto discover devices** is enabled on Windows):

1. **Android Broadcast Probe**:
   - Sends UDP broadcast packet to `255.255.255.255:55123` and subnet broadcast:
   ```json
   {
     "magic": "PHONEMIC_DISCOVER_V1",
     "deviceName": "iQOO Neo 10",
     "androidVersion": "Android 15",
     "appVersion": "1.0.0",
     "timestamp": 1794555985000
   }
   ```
2. **Windows Discovery Response**:
   - Windows replies via unicast UDP to the sender's IP and port:
   ```json
   {
     "magic": "PHONEMIC_ANNOUNCE_V1",
     "pcName": "Windows PC",
     "ipAddress": "192.168.1.42",
     "port": 55123,
     "encryptionRequired": true,
     "sampleRate": 48000,
     "channels": 1,
     "format": "PCM_16BIT",
     "version": "1.0.0"
   }
   ```

## 3. Control & Secure Pairing Protocol (TCP Port 55123)

Before streaming audio packets, the Android client establishes a TCP control session with the Windows server on port `55123`:

1. **`HELLO` / Handshake**:
   - Exchanges X25519 / ECDH ephemeral public keys (or PSK pairing token) to derive a 256-bit session key via HKDF-SHA256 when **Use encrypted connection (recommended)** / **Secure Connection** is enabled.
   - Exchanges device metadata (`deviceName`, `androidVersion`, `batteryPercent`, `wifiSsid`, `sampleRate`, `channels`, `bitsPerSample`).
2. **Bidirectional Control Messages (JSON lines over TCP)**:
   - `{"type": "MUTE_STATE", "muted": true|false}` — Synchronizes the Mute button between Android and Windows in real time.
   - `{"type": "TELEMETRY", "battery": 94, "wifiSsid": "Home_5G", "rmsDb": -12.0}` — Updates battery, Wi-Fi SSID, and level indicators every 2 seconds.
   - `{"type": "DISCONNECT", "reason": "user_requested"}` — Cleanly tears down the session from either end.

## 4. Real-Time Audio Packet Binary Layout (UDP Port 55123)

Every UDP audio packet uses a compact 24-byte binary header followed by the PCM payload (and a 16-byte AES-GCM authentication tag when encryption is enabled):

| Offset | Size | Field | Description |
| :--- | :--- | :--- | :--- |
| `0..3` | 4 B | `Magic` | ASCII `"PMIC"` (`0x50 0x4D 0x49 0x43`) |
| `4` | 1 B | `Version` | Protocol version (`0x01`) |
| `5` | 1 B | `Flags` | Bit 0: `Encrypted` (`1` = AES-256-GCM), Bit 1: `Muted` (`1` = silence), Bit 2: `Parity` |
| `6..7` | 2 B | `HeaderReserved` | Sample rate code (`48000` -> `0x0001`, `44100` -> `0x0002`, `16000` -> `0x0003`) |
| `8..11` | 4 B | `SequenceNumber` | Unsigned 32-bit monotonic packet counter (`uint32` big-endian) |
| `12..19` | 8 B | `TimestampUs` | Capture timestamp in microseconds (`uint64` big-endian) |
| `20..21` | 2 B | `PayloadLength` | Length of PCM audio payload in bytes (`uint16` big-endian, e.g., `960`) |
| `22..23` | 2 B | `RmsLevelCentidB` | Signed 16-bit RMS level in `0.01 dBFS` units (e.g., `-1200` = `-12.0 dB`) |
| `24..N` | Var | `Payload` | Raw 16-bit little-endian mono PCM samples (or AES-256-GCM ciphertext + 16B tag) |

### Bandwidth Calculation at 48 kHz Mono 16-bit
- Raw PCM bit rate: `48,000 samples/sec × 16 bits = 768 kbps` (`0.768 Mbps`)
- With UDP/IP + PMIC header + AES-GCM tag at 100 packets/sec (10 ms frames) and optional FEC/telemetry overhead: **`~ 0.85 – 0.90 Mbps`**, matching the `0.9 Mbps` telemetry indicator in the Network Performance monitor.
