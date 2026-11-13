package com.phonemic.app.protocol

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Implements the 24-byte PhoneMic binary packet framing (`PMIC`),
 * HKDF-SHA256 session key derivation, and AES-256-GCM payload encryption.
 */
object PhoneMicPacket {
    const val MAGIC_0: Byte = 0x50 // 'P'
    const val MAGIC_1: Byte = 0x4D // 'M'
    const val MAGIC_2: Byte = 0x49 // 'I'
    const val MAGIC_3: Byte = 0x43 // 'C'
    const val VERSION: Byte = 0x01
    const val HEADER_SIZE = 24
    const val GCM_TAG_BITS = 128
    const val GCM_TAG_BYTES = 16

    const val FLAG_ENCRYPTED: Int = 0x01
    const val FLAG_MUTED: Int = 0x02

    fun sampleRateToCode(sampleRate: Int): Short = when (sampleRate) {
        48000 -> 1
        44100 -> 2
        16000 -> 3
        else -> 1
    }

    fun codeToSampleRate(code: Short): Int = when (code.toInt()) {
        1 -> 48000
        2 -> 44100
        3 -> 16000
        else -> 48000
    }

    /**
     * Derives a 12-byte deterministic per-packet GCM nonce from the session salt and sequence number.
     */
    fun buildNonce(sessionSalt4B: ByteArray, sequenceNumber: Long, timestampUs: Long): ByteArray {
        val nonce = ByteArray(12)
        System.arraycopy(sessionSalt4B, 0, nonce, 0, 4.coerceAtMost(sessionSalt4B.size))
        val buf = ByteBuffer.wrap(nonce, 4, 8).order(ByteOrder.BIG_ENDIAN)
        buf.putInt((sequenceNumber and 0xFFFFFFFFL).toInt())
        buf.putInt(((timestampUs xor sequenceNumber) and 0xFFFFFFFFL).toInt())
        return nonce
    }

    /**
     * Encodes a 16-bit Mono PCM frame into a wire-ready UDP datagram buffer.
     */
    fun encodeFrame(
        pcmBytes: ByteArray,
        pcmLength: Int,
        sequenceNumber: Long,
        timestampUs: Long,
        sampleRate: Int,
        rmsDb: Float,
        isMuted: Boolean,
        aesKey: ByteArray? = null,
        sessionSalt4B: ByteArray = byteArrayOf(0x50, 0x4D, 0x30, 0x31)
    ): ByteArray {
        val encrypted = aesKey != null && aesKey.size == 32
        var flags = 0
        if (encrypted) flags = flags or FLAG_ENCRYPTED
        if (isMuted) flags = flags or FLAG_MUTED

        val rmsCentiDb = (rmsDb * 100f).toInt().coerceIn(-10000, 0).toShort()
        val header = ByteArray(HEADER_SIZE)
        val headerBuf = ByteBuffer.wrap(header).order(ByteOrder.BIG_ENDIAN)
        headerBuf.put(MAGIC_0)
        headerBuf.put(MAGIC_1)
        headerBuf.put(MAGIC_2)
        headerBuf.put(MAGIC_3)
        headerBuf.put(VERSION)
        headerBuf.put(flags.toByte())
        headerBuf.putShort(sampleRateToCode(sampleRate))
        headerBuf.putInt((sequenceNumber and 0xFFFFFFFFL).toInt())
        headerBuf.putLong(timestampUs)

        val payloadToSend: ByteArray = if (encrypted) {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val nonce = buildNonce(sessionSalt4B, sequenceNumber, timestampUs)
            val keySpec = SecretKeySpec(aesKey!!, "AES")
            val gcmSpec = GCMParameterSpec(GCM_TAG_BITS, nonce)
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec)
            // Authenticate the first 20 bytes of header as AAD
            cipher.updateAAD(header, 0, 20)
            cipher.doFinal(pcmBytes, 0, pcmLength)
        } else {
            pcmBytes.copyOfRange(0, pcmLength)
        }

        headerBuf.putShort(payloadToSend.size.toShort())
        headerBuf.putShort(rmsCentiDb)

        val packet = ByteArray(HEADER_SIZE + payloadToSend.size)
        System.arraycopy(header, 0, packet, 0, HEADER_SIZE)
        System.arraycopy(payloadToSend, 0, packet, HEADER_SIZE, payloadToSend.size)
        return packet
    }

    /**
     * Derives a 32-byte AES-256 key from a shared secret using HKDF-SHA256.
     */
    fun deriveAesKeyHkdf(sharedSecret: ByteArray, info: String = "PhoneMic-AES256-GCM-v1"): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        val salt = "PhoneMic-v1-Salt".toByteArray(Charsets.UTF_8)
        mac.init(SecretKeySpec(salt, "HmacSHA256"))
        val prk = mac.doFinal(sharedSecret)

        mac.init(SecretKeySpec(prk, "HmacSHA256"))
        mac.update(info.toByteArray(Charsets.UTF_8))
        mac.update(0x01.toByte())
        return mac.doFinal()
    }
}
