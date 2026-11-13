using System.Windows;
using System.Windows.Controls;
using System.Windows.Media;
using PhoneMic.Desktop.ViewModels;

namespace PhoneMic.Desktop;

public partial class MainWindow : Window
{
    private readonly MainViewModel _viewModel;

    public MainWindow()
    {
        InitializeComponent();
        _viewModel = new MainViewModel();
        DataContext = _viewModel;
        Closed += (_, _) => _viewModel.Dispose();
    }

    private void HighlightSidebarButton(Button activeBtn)
    {
        var buttons = new[] { BtnNavDashboard, BtnNavDevices, BtnNavAudio, BtnNavNetwork, BtnNavSettings, BtnNavAbout };
        foreach (var btn in buttons)
        {
            btn.Background = btn == activeBtn
                ? new SolidColorBrush(Color.FromRgb(0xDC, 0xFC, 0xE7))
                : Brushes.Transparent;
        }
    }

    private void OnNavDashboardClick(object sender, RoutedEventArgs e)
    {
        _viewModel.SelectedSection = "Dashboard";
        HighlightSidebarButton(BtnNavDashboard);
    }

    private void OnNavDevicesClick(object sender, RoutedEventArgs e)
    {
        _viewModel.SelectedSection = "Devices";
        HighlightSidebarButton(BtnNavDevices);
    }

    private void OnNavAudioClick(object sender, RoutedEventArgs e)
    {
        _viewModel.SelectedSection = "Audio";
        HighlightSidebarButton(BtnNavAudio);
    }

    private void OnNavNetworkClick(object sender, RoutedEventArgs e)
    {
        _viewModel.SelectedSection = "Network";
        HighlightSidebarButton(BtnNavNetwork);
    }

    private void OnNavSettingsClick(object sender, RoutedEventArgs e)
    {
        _viewModel.SelectedSection = "Settings";
        HighlightSidebarButton(BtnNavSettings);
    }

    private void OnNavAboutClick(object sender, RoutedEventArgs e)
    {
        _viewModel.SelectedSection = "About";
        HighlightSidebarButton(BtnNavAbout);
    }

    private void OnToggleMuteClick(object sender, RoutedEventArgs e)
    {
        _viewModel.ToggleMute();
    }

    private void OnDisconnectClick(object sender, RoutedEventArgs e)
    {
        _viewModel.DisconnectDevice();
    }

    private void OnCopyIpClick(object sender, RoutedEventArgs e)
    {
        Clipboard.SetText(_viewModel.ConnectedDeviceIp);
    }

    private void OnCopyPortClick(object sender, RoutedEventArgs e)
    {
        Clipboard.SetText(_viewModel.Port.ToString());
    }
}
