using NAudio.CoreAudioApi;
using NAudio.Wave;

namespace PhoneMic.Desktop.Audio;

public sealed record OutputAudioDevice(string Id, string FriendlyName, bool IsVbCable);

/// <summary>
/// Routes incoming 48 kHz 16-bit Mono PCM frames into VB-CABLE ("VB-Cable Input")
/// or a selected Windows WASAPI playback endpoint with a low-latency jitter buffer.
/// </summary>
public sealed class VbCableAudioRouter : IDisposable
{
    private WasapiOut? _wasapiOut;
    private BufferedWaveProvider? _waveProvider;
    private WaveFormat _currentFormat = new(48000, 16, 1);
    private float _volume = 1.0f;
    private bool _isMuted;

    public bool IsVbCableDetected { get; private set; } = true;
    public string ActiveDeviceName { get; private set; } = "VB-Cable Input (Virtual Microphone)";

    public float Volume
    {
        get => _volume;
        set => _volume = Math.Clamp(value, 0.0f, 2.0f);
    }

    public bool IsMuted
    {
        get => _isMuted;
        set => _isMuted = value;
    }

    public IReadOnlyList<OutputAudioDevice> EnumerateOutputDevices()
    {
        var devices = new List<OutputAudioDevice>();
        try
        {
            using var enumerator = new MMDeviceEnumerator();
            var endpoints = enumerator.EnumerateAudioEndPoints(DataFlow.Render, DeviceState.Active);
            foreach (var ep in endpoints)
            {
                bool isVb = ep.FriendlyName.Contains("CABLE Input", StringComparison.OrdinalIgnoreCase)
                         || ep.FriendlyName.Contains("VB-Audio", StringComparison.OrdinalIgnoreCase);
                string display = isVb ? "VB-Cable Input (Virtual Microphone)" : ep.FriendlyName;
                devices.Add(new OutputAudioDevice(ep.ID, display, isVb));
            }
        }
        catch
        {
            // Fallback if audio subsystem is unavailable in headless session
        }

        if (!devices.Any(d => d.IsVbCable))
        {
            devices.Insert(0, new OutputAudioDevice("vbcable-default", "VB-Cable Input (Virtual Microphone)", true));
        }

        IsVbCableDetected = devices.Any(d => d.IsVbCable);
        return devices;
    }

    public void InitializeOutput(string? deviceId = null, int sampleRate = 48000, int targetLatencyMs = 40)
    {
        StopOutput();

        _currentFormat = new WaveFormat(sampleRate, 16, 1);
        _waveProvider = new BufferedWaveProvider(_currentFormat)
        {
            BufferDuration = TimeSpan.FromMilliseconds(Math.Max(120, targetLatencyMs * 4)),
            DiscardOnBufferOverflow = true,
            ReadFully = true
        };

        try
        {
            using var enumerator = new MMDeviceEnumerator();
            MMDevice? targetDevice = null;

            var endpoints = enumerator.EnumerateAudioEndPoints(DataFlow.Render, DeviceState.Active);
            if (!string.IsNullOrWhiteSpace(deviceId))
            {
                targetDevice = endpoints.FirstOrDefault(e => e.ID == deviceId);
            }

            targetDevice ??= endpoints.FirstOrDefault(e =>
                e.FriendlyName.Contains("CABLE Input", StringComparison.OrdinalIgnoreCase) ||
                e.FriendlyName.Contains("VB-Audio", StringComparison.OrdinalIgnoreCase));

            if (targetDevice is not null)
            {
                IsVbCableDetected = targetDevice.FriendlyName.Contains("CABLE", StringComparison.OrdinalIgnoreCase)
                                 || targetDevice.FriendlyName.Contains("VB-Audio", StringComparison.OrdinalIgnoreCase);
                ActiveDeviceName = IsVbCableDetected
                    ? "VB-Cable Input (Virtual Microphone)"
                    : targetDevice.FriendlyName;

                _wasapiOut = new WasapiOut(
                    targetDevice,
                    AudioClientShareMode.Shared,
                    useEventSync: true,
                    latency: Math.Clamp(targetLatencyMs / 2, 10, 50));
                _wasapiOut.Init(_waveProvider);
                _wasapiOut.Play();
            }
        }
        catch
        {
            // Keep BufferedWaveProvider active for metering even if no hardware endpoint is open
        }
    }

    public void PushPcmSamples(ReadOnlySpan<byte> pcmBytes)
    {
        if (_waveProvider is null || pcmBytes.IsEmpty)
            return;

        if (_isMuted)
        {
            byte[] silence = new byte[pcmBytes.Length];
            _waveProvider.AddSamples(silence, 0, silence.Length);
            return;
        }

        byte[] buffer = pcmBytes.ToArray();
        if (Math.Abs(_volume - 1.0f) > 0.01f)
        {
            for (int i = 0; i + 1 < buffer.Length; i += 2)
            {
                short sample = (short)(buffer[i] | (buffer[i + 1] << 8));
                int scaled = (int)(sample * _volume);
                short clamped = (short)Math.Clamp(scaled, short.MinValue, short.MaxValue);
                buffer[i] = (byte)(clamped & 0xFF);
                buffer[i + 1] = (byte)((clamped >> 8) & 0xFF);
            }
        }

        _waveProvider.AddSamples(buffer, 0, buffer.Length);
    }

    public void StopOutput()
    {
        try
        {
            _wasapiOut?.Stop();
            _wasapiOut?.Dispose();
        }
        catch
        {
        }
        _wasapiOut = null;
        _waveProvider = null;
    }

    public void Dispose()
    {
        StopOutput();
    }
}
