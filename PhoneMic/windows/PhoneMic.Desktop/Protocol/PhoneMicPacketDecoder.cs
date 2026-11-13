using System.Buffers.Binary;
using System.Security.Cryptography;
using System.Text;

namespace PhoneMic.Desktop.Protocol;

public sealed record DecodedAudioFrame(
    uint SequenceNumber,
    ulong TimestampUs,
    int SampleRate,
    float RmsDb,
    bool IsMuted,
    bool IsEncrypted,
    byte[] PcmBytes
);

/// <summary>
/// Decodes and authenticates 24-byte PhoneMic ("PMIC") UDP audio frames,
/// including HKDF-SHA256 session key derivation and AES-256-GCM decryption.
/// </summary>
public static class PhoneMicPacketDecoder
{
    public const int HeaderSize = 24;
    public const int GcmTagBytes = 16;
    public const byte FlagEncrypted = 0x01;
    public const byte FlagMuted = 0x02;

    private static readonly byte[] DefaultSalt4B = { 0x50, 0x4D, 0x30, 0x31 };

    public static byte[] DeriveAesKeyHkdf(string sharedSecret, string info = "PhoneMic-AES256-GCM-v1")
    {
        byte[] ikm = Encoding.UTF8.GetBytes(sharedSecret);
        byte[] salt = Encoding.UTF8.GetBytes("PhoneMic-v1-Salt");
        using var hmacExtract = new HMACSHA256(salt);
        byte[] prk = hmacExtract.ComputeHash(ikm);

        using var hmacExpand = new HMACSHA256(prk);
        byte[] infoBytes = Encoding.UTF8.GetBytes(info);
        byte[] expandInput = new byte[infoBytes.Length + 1];
        Buffer.BlockCopy(infoBytes, 0, expandInput, 0, infoBytes.Length);
        expandInput[^1] = 0x01;
        return hmacExpand.ComputeHash(expandInput);
    }

    public static byte[] BuildNonce(ReadOnlySpan<byte> salt4B, uint sequenceNumber, ulong timestampUs)
    {
        byte[] nonce = new byte[12];
        salt4B[..Math.Min(4, salt4B.Length)].CopyTo(nonce.AsSpan(0, 4));
        BinaryPrimitives.WriteUInt32BigEndian(nonce.AsSpan(4, 4), sequenceNumber);
        uint mixedTime = (uint)((timestampUs ^ sequenceNumber) & 0xFFFFFFFFUL);
        BinaryPrimitives.WriteUInt32BigEndian(nonce.AsSpan(8, 4), mixedTime);
        return nonce;
    }

    public static bool TryDecodeFrame(
        ReadOnlySpan<byte> datagram,
        byte[]? aesKey,
        out DecodedAudioFrame? frame)
    {
        frame = null;
        if (datagram.Length < HeaderSize)
            return false;

        // Verify "PMIC" magic
        if (datagram[0] != 0x50 || datagram[1] != 0x4D || datagram[2] != 0x49 || datagram[3] != 0x43)
            return false;

        byte version = datagram[4];
        if (version != 0x01)
            return false;

        byte flags = datagram[5];
        bool isEncrypted = (flags & FlagEncrypted) != 0;
        bool isMuted = (flags & FlagMuted) != 0;

        short rateCode = BinaryPrimitives.ReadInt16BigEndian(datagram.Slice(6, 2));
        int sampleRate = rateCode switch
        {
            1 => 48000,
            2 => 44100,
            3 => 16000,
            _ => 48000
        };

        uint seq = BinaryPrimitives.ReadUInt32BigEndian(datagram.Slice(8, 4));
        ulong timestampUs = BinaryPrimitives.ReadUInt64BigEndian(datagram.Slice(12, 8));
        ushort payloadLen = BinaryPrimitives.ReadUInt16BigEndian(datagram.Slice(20, 2));
        short rmsCentiDb = BinaryPrimitives.ReadInt16BigEndian(datagram.Slice(22, 2));
        float rmsDb = Math.Clamp(rmsCentiDb / 100.0f, -60.0f, 0.0f);

        if (datagram.Length < HeaderSize + payloadLen)
            return false;

        ReadOnlySpan<byte> payloadSpan = datagram.Slice(HeaderSize, payloadLen);
        byte[] pcmBytes;

        if (isEncrypted)
        {
            if (aesKey is null || aesKey.Length != 32 || payloadLen < GcmTagBytes)
                return false;

            int cipherLen = payloadLen - GcmTagBytes;
            ReadOnlySpan<byte> ciphertext = payloadSpan.Slice(0, cipherLen);
            ReadOnlySpan<byte> tag = payloadSpan.Slice(cipherLen, GcmTagBytes);
            ReadOnlySpan<byte> aad = datagram.Slice(0, 20);
            byte[] nonce = BuildNonce(DefaultSalt4B, seq, timestampUs);

            pcmBytes = new byte[cipherLen];
            try
            {
                using var aesGcm = new AesGcm(aesKey, GcmTagBytes);
                aesGcm.Decrypt(nonce, ciphertext, tag, pcmBytes, aad);
            }
            catch (CryptographicException)
            {
                return false;
            }
        }
        else
        {
            pcmBytes = payloadSpan.ToArray();
        }

        frame = new DecodedAudioFrame(
            SequenceNumber: seq,
            TimestampUs: timestampUs,
            SampleRate: sampleRate,
            RmsDb: rmsDb,
            IsMuted: isMuted,
            IsEncrypted: isEncrypted,
            PcmBytes: pcmBytes
        );
        return true;
    }
}
