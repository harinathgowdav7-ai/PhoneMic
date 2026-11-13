using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Runtime.CompilerServices;
using System.Windows;
using PhoneMic.Desktop.Audio;
using PhoneMic.Desktop.Network;

namespace PhoneMic.Desktop.ViewModels;

public sealed record PairedDeviceItem(
    string Name,
    string Subtitle,
    string IpAddress,
    int Port,
    string AndroidVersion,
    string AppVersion,
    int BatteryPercent,
    string WifiSsid,
    bool IsConnected
);

public sealed class MainViewModel : INotifyPropertyChanged, IDisposable
{
    private readonly PhoneMicServer _server;
    private readonly VbCableAudioRouter _audioRouter;

    private string _selectedSection = "Dashboard";
    private bool _isConnected = true;
    private bool _isMuted;
    private string _connectedDeviceName = "iQOO Neo 10";
    private string _connectedDeviceIp = "192.168.1.42";
    private int _port = 55123;
    private string _androidVersion = "Android 15";
    private string _phoneAppVersion = "PhoneMic v1.0.0";
    private string _batteryText = "94%";
    private string _wifiSsid = "Home_5G";
    private string _sampleRateText = "48 kHz";
    private string _formatText = "PCM (16-bit)";
    private string _channelsText = "Mono";
    private string _latencyText = "~ 40 ms";
    private string _rmsLevelText = "-12 dB";
    private double _inputLevelPercent = 72.0;
    private string _bandwidthText = "0.9 Mbps";
    private string _packetLossText = "0.0 %";
    private string _networkStatusText = "Listening for connections";
    private bool _useEncryption = true;
    private bool _sameNetworkOnly = true;
    private bool _autoDiscoverDevices = true;
    private string _selectedOutputDevice = "VB-Cable Input (Virtual Microphone)";
    private string _selectedAudioQuality = "High (48 kHz, 16-bit)";
    private string _selectedLatencyMode = "Low (~ 40 ms)";

    public ObservableCollection<PairedDeviceItem> Devices { get; } = new()
    {
        new PairedDeviceItem(
            Name: "iQOO Neo 10",
            Subtitle: "Connected · 192.168.1.42",
            IpAddress: "192.168.1.42",
            Port: 55123,
            AndroidVersion: "Android 15",
            AppVersion: "PhoneMic v1.0.0",
            BatteryPercent: 94,
            WifiSsid: "Home_5G",
            IsConnected: true),
        new PairedDeviceItem(
            Name: "Harinath's Phone",
            Subtitle: "Last connected: Nov 12, 2026, 10:24 PM",
            IpAddress: "192.168.1.45",
            Port: 55123,
            AndroidVersion: "Android 14",
            AppVersion: "PhoneMic v1.0.0",
            BatteryPercent: 86,
            WifiSsid: "Home_5G",
            IsConnected: false),
        new PairedDeviceItem(
            Name: "Android Device",
            Subtitle: "Last connected: Nov 10, 2026, 04:18 PM",
            IpAddress: "192.168.1.51",
            Port: 55123,
            AndroidVersion: "Android 14",
            AppVersion: "PhoneMic v1.0.0",
            BatteryPercent: 78,
            WifiSsid: "Home_5G",
            IsConnected: false)
    };

    public ObservableCollection<string> OutputDevices { get; } = new()
    {
        "VB-Cable Input (Virtual Microphone)"
    };

    public ObservableCollection<string> AudioQualityOptions { get; } = new()
    {
        "High (48 kHz, 16-bit)",
        "Standard (44.1 kHz, 16-bit)",
        "Voice (16 kHz, 16-bit)"
    };

    public ObservableCollection<string> LatencyModeOptions { get; } = new()
    {
        "Low (~ 40 ms)",
        "Balanced (~ 60 ms)",
        "Ultra Stable (~ 90 ms)"
    };

    public ObservableCollection<double> SpectrumBarHeights { get; } = new(
        new double[]
        {
            18, 26, 38, 44, 34, 48, 28, 36, 46, 24, 52, 68,
            56, 38, 30, 34, 42, 46, 34, 44, 50, 38, 48, 32,
            40, 52, 42, 28, 36, 44, 32, 24, 18
        });

    public MainViewModel()
    {
        _audioRouter = new VbCableAudioRouter();
        foreach (var dev in _audioRouter.EnumerateOutputDevices())
        {
            if (!OutputDevices.Contains(dev.FriendlyName))
                OutputDevices.Add(dev.FriendlyName);
        }
        _audioRouter.InitializeOutput(sampleRate: 48000, targetLatencyMs: 40);

        _server = new PhoneMicServer(55123);
        _server.PhoneConnected += OnPhoneConnected;
        _server.PhoneDisconnected += OnPhoneDisconnected;
        _server.RemoteMuteChanged += OnRemoteMuteChanged;
        _server.AudioFrameReceived += OnAudioFrameReceived;
        _server.Start();
    }

    public string SelectedSection
    {
        get => _selectedSection;
        set
        {
            if (SetField(ref _selectedSection, value))
            {
                OnPropertyChanged(nameof(IsDashboardVisible));
                OnPropertyChanged(nameof(IsDevicesVisible));
                OnPropertyChanged(nameof(IsAudioVisible));
                OnPropertyChanged(nameof(IsNetworkVisible));
                OnPropertyChanged(nameof(IsSettingsVisible));
                OnPropertyChanged(nameof(IsAboutVisible));
            }
        }
    }

    public Visibility IsDashboardVisible => SelectedSection == "Dashboard" ? Visibility.Visible : Visibility.Collapsed;
    public Visibility IsDevicesVisible => SelectedSection == "Devices" ? Visibility.Visible : Visibility.Collapsed;
    public Visibility IsAudioVisible => SelectedSection == "Audio" ? Visibility.Visible : Visibility.Collapsed;
    public Visibility IsNetworkVisible => SelectedSection == "Network" ? Visibility.Visible : Visibility.Collapsed;
    public Visibility IsSettingsVisible => SelectedSection == "Settings" ? Visibility.Visible : Visibility.Collapsed;
    public Visibility IsAboutVisible => SelectedSection == "About" ? Visibility.Visible : Visibility.Collapsed;

    public bool IsConnected
    {
        get => _isConnected;
        set
        {
            if (SetField(ref _isConnected, value))
                OnPropertyChanged(nameof(ConnectionHeaderText));
        }
    }

    public string ConnectionHeaderText => IsConnected ? "Connected" : "Listening";

    public bool IsMuted
    {
        get => _isMuted;
        set
        {
            if (SetField(ref _isMuted, value))
            {
                _audioRouter.IsMuted = value;
                OnPropertyChanged(nameof(MuteButtonLabel));
            }
        }
    }

    public string MuteButtonLabel => IsMuted ? "Unmute" : "Mute";

    public string ConnectedDeviceName
    {
        get => _connectedDeviceName;
        set
        {
            if (SetField(ref _connectedDeviceName, value))
                OnPropertyChanged(nameof(DashboardPhoneSubtitle));
        }
    }

    public string ConnectedDeviceIp
    {
        get => _connectedDeviceIp;
        set
        {
            if (SetField(ref _connectedDeviceIp, value))
                OnPropertyChanged(nameof(DashboardPhoneSubtitle));
        }
    }

    public string DashboardPhoneSubtitle => $"Phone: {ConnectedDeviceName}   |   IP: {ConnectedDeviceIp}";

    public int Port
    {
        get => _port;
        set => SetField(ref _port, value);
    }

    public string AndroidVersion
    {
        get => _androidVersion;
        set => SetField(ref _androidVersion, value);
    }

    public string PhoneAppVersion
    {
        get => _phoneAppVersion;
        set => SetField(ref _phoneAppVersion, value);
    }

    public string BatteryText
    {
        get => _batteryText;
        set => SetField(ref _batteryText, value);
    }

    public string WifiSsid
    {
        get => _wifiSsid;
        set
        {
            if (SetField(ref _wifiSsid, value))
                OnPropertyChanged(nameof(NetworkTypeDisplay));
        }
    }

    public string NetworkTypeDisplay => $"Wi-Fi ({WifiSsid})";

    public string SampleRateText
    {
        get => _sampleRateText;
        set => SetField(ref _sampleRateText, value);
    }

    public string FormatText
    {
        get => _formatText;
        set => SetField(ref _formatText, value);
    }

    public string ChannelsText
    {
        get => _channelsText;
        set => SetField(ref _channelsText, value);
    }

    public string LatencyText
    {
        get => _latencyText;
        set => SetField(ref _latencyText, value);
    }

    public string RmsLevelText
    {
        get => _rmsLevelText;
        set => SetField(ref _rmsLevelText, value);
    }

    public double InputLevelPercent
    {
        get => _inputLevelPercent;
        set => SetField(ref _inputLevelPercent, value);
    }

    public string BandwidthText
    {
        get => _bandwidthText;
        set => SetField(ref _bandwidthText, value);
    }

    public string PacketLossText
    {
        get => _packetLossText;
        set => SetField(ref _packetLossText, value);
    }

    public string NetworkStatusText
    {
        get => _networkStatusText;
        set => SetField(ref _networkStatusText, value);
    }

    public bool UseEncryption
    {
        get => _useEncryption;
        set
        {
            if (SetField(ref _useEncryption, value))
                _server.UseEncryption = value;
        }
    }

    public bool SameNetworkOnly
    {
        get => _sameNetworkOnly;
        set
        {
            if (SetField(ref _sameNetworkOnly, value))
                _server.SameNetworkOnly = value;
        }
    }

    public bool AutoDiscoverDevices
    {
        get => _autoDiscoverDevices;
        set
        {
            if (SetField(ref _autoDiscoverDevices, value))
                _server.AutoDiscoverDevices = value;
        }
    }

    public string SelectedOutputDevice
    {
        get => _selectedOutputDevice;
        set => SetField(ref _selectedOutputDevice, value);
    }

    public string SelectedAudioQuality
    {
        get => _selectedAudioQuality;
        set
        {
            if (SetField(ref _selectedAudioQuality, value))
            {
                int rate = value.Contains("44.1") ? 44100 : value.Contains("16") ? 16000 : 48000;
                SampleRateText = rate == 44100 ? "44.1 kHz" : $"{rate / 1000} kHz";
                _audioRouter.InitializeOutput(sampleRate: rate, targetLatencyMs: 40);
            }
        }
    }

    public string SelectedLatencyMode
    {
        get => _selectedLatencyMode;
        set
        {
            if (SetField(ref _selectedLatencyMode, value))
            {
                LatencyText = value.Contains("60") ? "~ 60 ms" : value.Contains("90") ? "~ 90 ms" : "~ 40 ms";
            }
        }
    }

    public void ToggleMute()
    {
        IsMuted = !IsMuted;
        _server.SendMuteState(IsMuted);
    }

    public void DisconnectDevice()
    {
        _server.DisconnectActivePhone();
        IsConnected = false;
        NetworkStatusText = "Listening for connections";
    }

    private void OnPhoneConnected(ConnectedPhoneInfo info)
    {
        Application.Current?.Dispatcher.Invoke(() =>
        {
            IsConnected = true;
            ConnectedDeviceName = info.DeviceName;
            ConnectedDeviceIp = info.IpAddress;
            Port = info.Port;
            AndroidVersion = info.AndroidVersion;
            PhoneAppVersion = info.AppVersion;
            BatteryText = $"{info.BatteryPercent}%";
            WifiSsid = info.WifiSsid;
            SampleRateText = $"{info.SampleRate / 1000} kHz";
        });
    }

    private void OnPhoneDisconnected(string reason)
    {
        Application.Current?.Dispatcher.Invoke(() =>
        {
            IsConnected = false;
            NetworkStatusText = "Listening for connections";
        });
    }

    private void OnRemoteMuteChanged(bool muted)
    {
        Application.Current?.Dispatcher.Invoke(() =>
        {
            IsMuted = muted;
        });
    }

    private void OnAudioFrameReceived(
        Protocol.DecodedAudioFrame frame,
        double mbps,
        double packetLoss,
        float[] bars)
    {
        _audioRouter.PushPcmSamples(frame.PcmBytes);

        Application.Current?.Dispatcher.InvokeAsync(() =>
        {
            int roundedDb = (int)Math.Round(frame.RmsDb);
            RmsLevelText = $"{roundedDb} dB";
            InputLevelPercent = Math.Clamp((frame.RmsDb + 60.0) / 60.0 * 100.0, 5.0, 100.0);
            BandwidthText = $"{mbps:0.0} Mbps";
            PacketLossText = $"{packetLoss:0.0} %";

            int count = Math.Min(SpectrumBarHeights.Count, bars.Length);
            for (int i = 0; i < count; i++)
            {
                SpectrumBarHeights[i] = Math.Round(10.0 + bars[i] * 58.0, 1);
            }
        });
    }

    public event PropertyChangedEventHandler? PropertyChanged;

    private void OnPropertyChanged([CallerMemberName] string? name = null) =>
        PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(name));

    private bool SetField<T>(ref T field, T value, [CallerMemberName] string? name = null)
    {
        if (EqualityComparer<T>.Default.Equals(field, value))
            return false;
        field = value;
        OnPropertyChanged(name);
        return true;
    }

    public void Dispose()
    {
        _server.Dispose();
        _audioRouter.Dispose();
    }
}
