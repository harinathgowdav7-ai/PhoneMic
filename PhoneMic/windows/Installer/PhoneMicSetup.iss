; PhoneMic Windows 10/11 Installer Script (Inno Setup 6)
#define MyAppName "PhoneMic"
#define MyAppVersion "1.0.0"
#define MyAppPublisher "PhoneMic Open Source Project"
#define MyAppURL "https://github.com/phonemic/PhoneMic"
#define MyAppExeName "PhoneMic.exe"

[Setup]
AppId={{8B6F2D4A-9E31-4C53-A1E7-71F9D8C35012}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppPublisher={#MyAppPublisher}
AppPublisherURL={#MyAppURL}
DefaultDirName={autopf}\{#MyAppName}
DefaultGroupName={#MyAppName}
AllowNoIcons=yes
LicenseFile=..\..\LICENSE
OutputDir=..\dist
OutputBaseFilename=PhoneMic-Windows-Setup-v1.0.0
Compression=lzma2
SolidCompression=yes
WizardStyle=modern
ArchitecturesAllowed=x64compatible
ArchitecturesInstallIn64BitMode=x64compatible
PrivilegesRequired=admin

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked
Name: "firewallrule"; Description: "Add Windows Defender Firewall rule for PhoneMic (TCP/UDP Port 55123)"; GroupDescription: "Network Configuration:"

[Files]
Source: "..\PhoneMic.Desktop\bin\Release\net8.0-windows\win-x64\publish\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{group}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"
Name: "{autodesktop}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; Tasks: desktopicon

[Run]
Filename: "netsh"; Parameters: "advfirewall firewall add rule name=""PhoneMic Audio Server (UDP 55123)"" dir=in action=allow protocol=UDP localport=55123"; Flags: runhidden; Tasks: firewallrule
Filename: "netsh"; Parameters: "advfirewall firewall add rule name=""PhoneMic Control Server (TCP 55123)"" dir=in action=allow protocol=TCP localport=55123"; Flags: runhidden; Tasks: firewallrule
Filename: "{app}\{#MyAppExeName}"; Description: "{cm:LaunchProgram,{#StringChange(MyAppName, '&', '&&')}}"; Flags: nowait postinstall skipifsilent
