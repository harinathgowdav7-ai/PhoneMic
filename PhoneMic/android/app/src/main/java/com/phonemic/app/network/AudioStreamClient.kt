package com.phonemic.app.network

import android.os.Build
import com.phonemic.app.protocol.PhoneMicPacket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.atomic.AtomicLong

/**
 * Manages the TCP control/pairing channel and UDP low-latency audio streaming socket
 * between the Android phone and the Windows desktop receiver.
 */
class AudioStreamClient(
    private val scope: CoroutineScope,
    private val onRemoteMuteChanged: (Boolean) -> Unit,
    private val onDisconnected: (String) -> Unit
) {
    private var controlSocket: Socket? = null
    private var controlWriter: PrintWriter? = null
    private var udpSocket: DatagramSocket? = null
    private var targetAddress: InetAddress? = null
    private var targetPort: Int = 55123
    private var controlListenerJob: Job? = null
    private var telemetryJob: Job? = null

    private val sequenceCounter = AtomicLong(0L)
    private var aesSessionKey: ByteArray? = null

    suspend fun connect(
        pcIp: String,
        port: Int = 55123,
        sampleRate: Int = 48000,
        secureConnection: Boolean = true,
        wifiSsid: String = "Home_5G",
        batteryPercent: Int = 94
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            disconnectInternal()
            val addr = InetAddress.getByName(pcIp.trim())
            targetAddress = addr
            targetPort = port

            val tcp = Socket()
            tcp.tcpNoDelay = true
            tcp.connect(InetSocketAddress(addr, port), 3000)

            val writer = PrintWriter(tcp.getOutputStream(), true)
            val reader = BufferedReader(InputStreamReader(tcp.getInputStream()))

            // Send HELLO handshake
            val sharedToken = "PhoneMic-PairedKey-v1-${pcIp}"
            aesSessionKey = if (secureConnection) {
                PhoneMicPacket.deriveAesKeyHkdf(sharedToken.toByteArray(Charsets.UTF_8))
            } else {
                null
            }

            val hello = JSONObject().apply {
                put("type", "HELLO")
                put("deviceName", "${Build.MANUFACTURER} ${Build.MODEL}".trim().ifBlank { "iQOO Neo 10" })
                put("androidVersion", "Android ${Build.VERSION.RELEASE}")
                put("appVersion", "PhoneMic v1.0.0")
                put("sampleRate", sampleRate)
                put("channels", 1)
                put("bitsPerSample", 16)
                put("encrypted", secureConnection)
                put("wifiSsid", wifiSsid)
                put("battery", batteryPercent)
            }
            writer.println(hello.toString())

            tcp.soTimeout = 3000
            val ackLine = reader.readLine()
            tcp.soTimeout = 0

            val pcName = if (ackLine != null) {
                val ackJson = JSONObject(ackLine)
                ackJson.optString("pcName", "Windows PC")
            } else {
                "Windows PC"
            }

            controlSocket = tcp
            controlWriter = writer
            udpSocket = DatagramSocket().apply {
                trafficClass = 0x10 or 0x08 // IPTOS_LOWDELAY | IPTOS_THROUGHPUT
            }
            sequenceCounter.set(0L)

            startControlListener(reader)
            startTelemetryLoop(wifiSsid, batteryPercent)

            Result.success(pcName)
        } catch (e: Exception) {
            disconnectInternal()
            Result.failure(e)
        }
    }

    private fun startControlListener(reader: BufferedReader) {
        controlListenerJob = scope.launch(Dispatchers.IO) {
            try {
                while (isActive) {
                    val line = reader.readLine() ?: break
                    val json = JSONObject(line)
                    when (json.optString("type")) {
                        "MUTE_STATE" -> {
                            onRemoteMuteChanged(json.optBoolean("muted", false))
                        }
                        "DISCONNECT" -> {
                            onDisconnected("Disconnected by Windows PC")
                            break
                        }
                    }
                }
            } catch (_: Exception) {
                if (isActive) {
                    onDisconnected("Connection lost")
                }
            }
        }
    }

    private fun startTelemetryLoop(wifiSsid: String, batteryPercent: Int) {
        telemetryJob = scope.launch(Dispatchers.IO) {
            while (isActive) {
                delay(2000L)
                runCatching {
                    val msg = JSONObject().apply {
                        put("type", "TELEMETRY")
                        put("wifiSsid", wifiSsid)
                        put("battery", batteryPercent)
                    }
                    controlWriter?.println(msg.toString())
                }
            }
        }
    }

    fun sendAudioFrame(
        pcmBytes: ByteArray,
        length: Int,
        sampleRate: Int,
        rmsDb: Float,
        isMuted: Boolean
    ) {
        val socket = udpSocket ?: return
        val addr = targetAddress ?: return
        val seq = sequenceCounter.getAndIncrement()
        val timestampUs = System.nanoTime() / 1000L

        val datagramBytes = PhoneMicPacket.encodeFrame(
            pcmBytes = pcmBytes,
            pcmLength = length,
            sequenceNumber = seq,
            timestampUs = timestampUs,
            sampleRate = sampleRate,
            rmsDb = rmsDb,
            isMuted = isMuted,
            aesKey = aesSessionKey
        )
        runCatching {
            val packet = DatagramPacket(datagramBytes, datagramBytes.size, addr, targetPort)
            socket.send(packet)
        }
    }

    fun sendMuteState(isMuted: Boolean) {
        scope.launch(Dispatchers.IO) {
            runCatching {
                val msg = JSONObject().apply {
                    put("type", "MUTE_STATE")
                    put("muted", isMuted)
                }
                controlWriter?.println(msg.toString())
            }
        }
    }

    fun disconnect() {
        scope.launch(Dispatchers.IO) {
            runCatching {
                val msg = JSONObject().apply {
                    put("type", "DISCONNECT")
                    put("reason", "user_requested")
                }
                controlWriter?.println(msg.toString())
            }
            disconnectInternal()
        }
    }

    private fun disconnectInternal() {
        controlListenerJob?.cancel()
        telemetryJob?.cancel()
        runCatching { udpSocket?.close() }
        runCatching { controlWriter?.close() }
        runCatching { controlSocket?.close() }
        udpSocket = null
        controlWriter = null
        controlSocket = null
    }
}
