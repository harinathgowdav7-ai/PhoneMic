using System.IO;
using System.Net;
using System.Net.NetworkInformation;
using System.Net.Sockets;
using System.Text;
using System.Text.Json;
using PhoneMic.Desktop.Protocol;

namespace PhoneMic.Desktop.Network;

public sealed record ConnectedPhoneInfo(
    string DeviceName,
    string IpAddress,
    int Port,
    string AndroidVersion,
    string AppVersion,
    int BatteryPercent,
    string WifiSsid,
    int SampleRate,
    bool Encrypted
);

/// <summary>
/// Listens on TCP/UDP port 55123 for Android discovery probes, control handshakes,
/// and real-time encrypted or raw 48 kHz PCM audio frames.
/// </summary>
public sealed class PhoneMicServer : IDisposable
{
    private readonly int _port;
    private CancellationTokenSource? _cts;
    private TcpListener? _tcpListener;
    private UdpClient? _udpListener;
    private TcpClient? _activeControlClient;
    private StreamWriter? _activeControlWriter;
    private byte[]? _activeAesKey;

    private uint _lastSeq;
    private uint _receivedPackets;
    private uint _lostPackets;
    private long _bytesReceivedWindow;
    private DateTime _windowStart = DateTime.UtcNow;

    public bool UseEncryption { get; set; } = true;
    public bool SameNetworkOnly { get; set; } = true;
    public bool AutoDiscoverDevices { get; set; } = true;

    public event Action<ConnectedPhoneInfo>? PhoneConnected;
    public event Action<string>? PhoneDisconnected;
    public event Action<bool>? RemoteMuteChanged;
    public event Action<DecodedAudioFrame, double, double, float[]>? AudioFrameReceived;

    public PhoneMicServer(int port = 55123)
    {
        _port = port;
    }

    public void Start()
    {
        Stop();
        _cts = new CancellationTokenSource();
        var token = _cts.Token;

        string localIp = GetLocalIPv4Address();
        _activeAesKey = PhoneMicPacketDecoder.DeriveAesKeyHkdf($"PhoneMic-PairedKey-v1-{localIp}");

        Task.Run(() => RunTcpControlLoopAsync(token), token);
        Task.Run(() => RunUdpAudioAndDiscoveryLoopAsync(token), token);
    }

    public void Stop()
    {
        try
        {
            _cts?.Cancel();
            _activeControlWriter?.Dispose();
            _activeControlClient?.Close();
            _tcpListener?.Stop();
            _udpListener?.Close();
        }
        catch
        {
        }
    }

    public void SendMuteState(bool isMuted)
    {
        try
        {
            if (_activeControlWriter is not null)
            {
                string json = JsonSerializer.Serialize(new { type = "MUTE_STATE", muted = isMuted });
                _activeControlWriter.WriteLine(json);
                _activeControlWriter.Flush();
            }
        }
        catch
        {
        }
    }

    public void DisconnectActivePhone()
    {
        try
        {
            if (_activeControlWriter is not null)
            {
                string json = JsonSerializer.Serialize(new { type = "DISCONNECT", reason = "pc_disconnected" });
                _activeControlWriter.WriteLine(json);
                _activeControlWriter.Flush();
            }
            _activeControlClient?.Close();
        }
        catch
        {
        }
        PhoneDisconnected?.Invoke("Disconnected by user");
    }

    private async Task RunTcpControlLoopAsync(CancellationToken ct)
    {
        try
        {
            _tcpListener = new TcpListener(IPAddress.Any, _port);
            _tcpListener.Start();

            while (!ct.IsCancellationRequested)
            {
                var client = await _tcpListener.AcceptTcpClientAsync(ct);
                var remoteEndPoint = client.Client.RemoteEndPoint as IPEndPoint;
                string remoteIp = remoteEndPoint?.Address.ToString() ?? "192.168.1.42";

                if (SameNetworkOnly && !IsSameSubnet(remoteIp, GetLocalIPv4Address()))
                {
                    client.Close();
                    continue;
                }

                _activeControlClient?.Close();
                _activeControlClient = client;

                _ = Task.Run(() => HandleControlClientAsync(client, remoteIp, ct), ct);
            }
        }
        catch
        {
        }
    }

    private async Task HandleControlClientAsync(TcpClient client, string remoteIp, CancellationToken ct)
    {
        try
        {
            using var stream = client.GetStream();
            using var reader = new StreamReader(stream, Encoding.UTF8);
            var writer = new StreamWriter(stream, new UTF8Encoding(false)) { AutoFlush = true };
            _activeControlWriter = writer;

            string localIp = GetLocalIPv4Address();
            _activeAesKey = PhoneMicPacketDecoder.DeriveAesKeyHkdf($"PhoneMic-PairedKey-v1-{localIp}");

            while (!ct.IsCancellationRequested)
            {
                string? line = await reader.ReadLineAsync(ct);
                if (line is null) break;

                using var doc = JsonDocument.Parse(line);
                var root = doc.RootElement;
                string type = root.TryGetProperty("type", out var tProp) ? tProp.GetString() ?? "" : "";

                if (type == "HELLO")
                {
                    string deviceName = root.TryGetProperty("deviceName", out var d) ? d.GetString() ?? "iQOO Neo 10" : "iQOO Neo 10";
                    string androidVer = root.TryGetProperty("androidVersion", out var av) ? av.GetString() ?? "Android 15" : "Android 15";
                    string appVer = root.TryGetProperty("appVersion", out var ap) ? ap.GetString() ?? "PhoneMic v1.0.0" : "PhoneMic v1.0.0";
                    int sampleRate = root.TryGetProperty("sampleRate", out var sr) ? sr.GetInt32() : 48000;
                    bool encrypted = root.TryGetProperty("encrypted", out var enc) && enc.GetBoolean();
                    string wifiSsid = root.TryGetProperty("wifiSsid", out var wf) ? wf.GetString() ?? "Home_5G" : "Home_5G";
                    int battery = root.TryGetProperty("battery", out var bat) ? bat.GetInt32() : 94;

                    string ack = JsonSerializer.Serialize(new
                    {
                        type = "HELLO_ACK",
                        pcName = Environment.MachineName,
                        port = _port,
                        encrypted = UseEncryption && encrypted
                    });
                    await writer.WriteLineAsync(ack);

                    PhoneConnected?.Invoke(new ConnectedPhoneInfo(
                        DeviceName: deviceName,
                        IpAddress: remoteIp,
                        Port: _port,
                        AndroidVersion: androidVer,
                        AppVersion: appVer,
                        BatteryPercent: battery,
                        WifiSsid: wifiSsid,
                        SampleRate: sampleRate,
                        Encrypted: encrypted
                    ));
                }
                else if (type == "MUTE_STATE")
                {
                    bool muted = root.TryGetProperty("muted", out var m) && m.GetBoolean();
                    RemoteMuteChanged?.Invoke(muted);
                }
                else if (type == "DISCONNECT")
                {
                    PhoneDisconnected?.Invoke("Phone disconnected");
                    break;
                }
            }
        }
        catch
        {
            PhoneDisconnected?.Invoke("Connection closed");
        }
    }

    private async Task RunUdpAudioAndDiscoveryLoopAsync(CancellationToken ct)
    {
        try
        {
            _udpListener = new UdpClient(_port);
            _windowStart = DateTime.UtcNow;

            while (!ct.IsCancellationRequested)
            {
                var result = await _udpListener.ReceiveAsync(ct);
                byte[] buffer = result.Buffer;

                // Check if this is an ASCII JSON discovery probe
                if (buffer.Length > 10 && buffer[0] == (byte)'{')
                {
                    if (AutoDiscoverDevices)
                    {
                        await TryRespondDiscoveryProbeAsync(buffer, result.RemoteEndPoint);
                    }
                    continue;
                }

                // Decode binary PMIC audio frame
                if (PhoneMicPacketDecoder.TryDecodeFrame(buffer, _activeAesKey, out var frame) && frame is not null)
                {
                    _receivedPackets++;
                    if (_lastSeq != 0 && frame.SequenceNumber > _lastSeq + 1)
                    {
                        _lostPackets += (frame.SequenceNumber - _lastSeq - 1);
                    }
                    _lastSeq = frame.SequenceNumber;
                    _bytesReceivedWindow += buffer.Length;

                    double elapsedSec = Math.Max(0.25, (DateTime.UtcNow - _windowStart).TotalSeconds);
                    double mbps = (_bytesReceivedWindow * 8.0) / (elapsedSec * 1_000_000.0);
                    if (elapsedSec > 2.0)
                    {
                        _bytesReceivedWindow = 0;
                        _windowStart = DateTime.UtcNow;
                    }

                    uint total = _receivedPackets + _lostPackets;
                    double lossPercent = total > 0 ? (_lostPackets * 100.0 / total) : 0.0;
                    float[] bars = ComputeSpectrumBars(frame.PcmBytes, 36);

                    AudioFrameReceived?.Invoke(frame, Math.Round(mbps, 2), Math.Round(lossPercent, 1), bars);
                }
            }
        }
        catch
        {
        }
    }

    private async Task TryRespondDiscoveryProbeAsync(byte[] buffer, IPEndPoint remoteEndPoint)
    {
        try
        {
            string text = Encoding.UTF8.GetString(buffer);
            using var doc = JsonDocument.Parse(text);
            if (doc.RootElement.TryGetProperty("magic", out var m) &&
                m.GetString() == "PHONEMIC_DISCOVER_V1")
            {
                string reply = JsonSerializer.Serialize(new
                {
                    magic = "PHONEMIC_ANNOUNCE_V1",
                    pcName = "Windows PC",
                    ipAddress = GetLocalIPv4Address(),
                    port = _port,
                    encryptionRequired = UseEncryption,
                    sampleRate = 48000,
                    channels = 1,
                    format = "PCM_16BIT",
                    version = "1.0.0"
                });
                byte[] replyBytes = Encoding.UTF8.GetBytes(reply);
                if (_udpListener is not null)
                {
                    await _udpListener.SendAsync(replyBytes, replyBytes.Length, remoteEndPoint);
                }
            }
        }
        catch
        {
        }
    }

    public static float[] ComputeSpectrumBars(ReadOnlySpan<byte> pcmBytes, int barCount = 36)
    {
        float[] bars = new float[barCount];
        int sampleCount = pcmBytes.Length / 2;
        if (sampleCount <= 0)
        {
            Array.Fill(bars, 0.1f);
            return bars;
        }

        int chunk = Math.Max(1, sampleCount / barCount);
        for (int b = 0; b < barCount; b++)
        {
            int start = Math.Min(b * chunk, sampleCount - 1);
            int end = Math.Min((b + 1) * chunk, sampleCount);
            float peak = 0f;
            for (int i = start; i < end; i++)
            {
                short s = (short)(pcmBytes[i * 2] | (pcmBytes[i * 2 + 1] << 8));
                float v = Math.Abs((int)s) / 32768f;
                if (v > peak) peak = v;
            }
            bars[b] = Math.Clamp(MathF.Sqrt(peak), 0.10f, 0.98f);
        }
        return bars;
    }

    public static string GetLocalIPv4Address()
    {
        try
        {
            foreach (var ni in NetworkInterface.GetAllNetworkInterfaces())
            {
                if (ni.OperationalStatus != OperationalStatus.Up ||
                    ni.NetworkInterfaceType == NetworkInterfaceType.Loopback)
                    continue;

                foreach (var ua in ni.GetIPProperties().UnicastAddresses)
                {
                    if (ua.Address.AddressFamily == AddressFamily.InterNetwork)
                    {
                        string ip = ua.Address.ToString();
                        if (ip.StartsWith("192.168.") || ip.StartsWith("10.") || ip.StartsWith("172."))
                            return ip;
                    }
                }
            }
        }
        catch
        {
        }
        return "192.168.1.42";
    }

    private static bool IsSameSubnet(string ipA, string ipB)
    {
        var partsA = ipA.Split('.');
        var partsB = ipB.Split('.');
        if (partsA.Length == 4 && partsB.Length == 4)
        {
            return partsA[0] == partsB[0] && partsA[1] == partsB[1] && partsA[2] == partsB[2];
        }
        return true;
    }

    public void Dispose()
    {
        Stop();
    }
}
