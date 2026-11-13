package com.phonemic.app.network

import android.os.Build
import com.phonemic.app.model.DiscoveredPc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InterfaceAddress
import java.net.NetworkInterface
import java.util.Collections

/**
 * Broadcasts `PHONEMIC_DISCOVER_V1` over UDP port 55123 and collects responses
 * from Windows PCs running PhoneMic on the local Wi-Fi network.
 */
class PcDiscoveryClient(private val defaultPort: Int = 55123) {

    suspend fun discoverPcs(timeoutMs: Int = 1800): List<DiscoveredPc> = withContext(Dispatchers.IO) {
        val discovered = linkedMapOf<String, DiscoveredPc>()
        var socket: DatagramSocket? = null
        try {
            socket = DatagramSocket()
            socket.broadcast = true
            socket.soTimeout = 450

            val probeJson = JSONObject().apply {
                put("magic", "PHONEMIC_DISCOVER_V1")
                put("deviceName", "${Build.MANUFACTURER} ${Build.MODEL}".trim())
                put("androidVersion", "Android ${Build.VERSION.RELEASE}")
                put("appVersion", "1.0.0")
                put("timestamp", System.currentTimeMillis())
            }.toString().toByteArray(Charsets.UTF_8)

            val broadcastTargets = collectBroadcastAddresses()
            for (addr in broadcastTargets) {
                runCatching {
                    val pkt = DatagramPacket(probeJson, probeJson.size, addr, defaultPort)
                    socket.send(pkt)
                }
            }

            val recvBuf = ByteArray(2048)
            val deadline = System.currentTimeMillis() + timeoutMs
            while (System.currentTimeMillis() < deadline) {
                try {
                    val respPkt = DatagramPacket(recvBuf, recvBuf.size)
                    socket.receive(respPkt)
                    val text = String(respPkt.data, 0, respPkt.length, Charsets.UTF_8)
                    val json = JSONObject(text)
                    if (json.optString("magic") == "PHONEMIC_ANNOUNCE_V1") {
                        val ip = json.optString("ipAddress").ifBlank {
                            respPkt.address.hostAddress ?: "192.168.1.42"
                        }
                        val pcName = json.optString("pcName", "Windows PC")
                        val port = json.optInt("port", defaultPort)
                        val enc = json.optBoolean("encryptionRequired", true)
                        discovered[ip] = DiscoveredPc(
                            pcName = pcName,
                            ipAddress = ip,
                            port = port,
                            encryptionRequired = enc,
                            lastConnectedText = "Available on Wi-Fi · $ip"
                        )
                    }
                } catch (_: Exception) {
                    // Timeout slice elapsed; continue until overall deadline
                }
            }
        } catch (_: Exception) {
        } finally {
            runCatching { socket?.close() }
        }
        discovered.values.toList()
    }

    private fun collectBroadcastAddresses(): List<InetAddress> {
        val list = mutableListOf<InetAddress>()
        runCatching { list.add(InetAddress.getByName("255.255.255.255")) }
        runCatching {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (ni in interfaces) {
                if (!ni.isUp || ni.isLoopback) continue
                for (ia: InterfaceAddress in ni.interfaceAddresses) {
                    ia.broadcast?.let { list.add(it) }
                }
            }
        }
        return list.distinct()
    }
}
