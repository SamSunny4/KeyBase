#define ProjectRoot "E:\\KeyBase"
#define StageRoot   "E:\\KeyBase\\dist\\KeyBase"
#define MyAppName "KeyBase"
#define MyAppVersion "4.0"
#define MyCompany "KeyBase.sam"
#define InstallDirName "KeyBase"

[Setup]
AppId={{1A2B3C4D-KEYBASE-2025}}   ; generate GUID once
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppPublisher={#MyCompany}
DefaultDirName={autopf}\{#InstallDirName}
DefaultGroupName={#MyAppName}
OutputBaseFilename=KeyBase-Setup
Compression=lzma
SolidCompression=yes
ArchitecturesInstallIn64BitMode=x64
; bundle prerequisites by inflating size limit if needed
DisableDirPage=no
DisableProgramGroupPage=no
PrivilegesRequired=admin
SetupLogging=yes

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Files]
; Core launcher / optional executable
Source: "{#StageRoot}\run.bat"; DestDir: "{app}"; Flags: ignoreversion
Source: "{#StageRoot}\KeyBase.exe"; DestDir: "{app}"; Flags: ignoreversion
Source: "{#StageRoot}\KeyBase.jar"; DestDir: "{app}"; Flags: ignoreversion

; Application binaries (compiled classes only, exclude license key generator)
Source: "{#StageRoot}\app\classes\*"; DestDir: "{app}\app\classes"; Excludes: "LicenseKeyGenerator.class"; Flags: recursesubdirs createallsubdirs ignoreversion

; Runtime dependencies
Source: "{#StageRoot}\lib\*"; DestDir: "{app}\lib"; Flags: recursesubdirs createallsubdirs ignoreversion
Source: "{#StageRoot}\resources\*"; DestDir: "{app}\resources"; Flags: recursesubdirs createallsubdirs ignoreversion

; Configuration and seed data
Source: "{#StageRoot}\config\*"; DestDir: "{app}\config"; Flags: recursesubdirs createallsubdirs ignoreversion
Source: "{#StageRoot}\data\*"; DestDir: "{app}\data"; Flags: recursesubdirs createallsubdirs ignoreversion

; Bundled portable JRE (required runtime)
Source: "{#StageRoot}\jre\*"; DestDir: "{app}\jre"; Flags: recursesubdirs createallsubdirs ignoreversion

[Icons]
Name: "{autoprograms}\KeyBase"; Filename: "{app}\KeyBase.exe"; WorkingDir: "{app}"; Check: FileExists(ExpandConstant('{app}\KeyBase.exe'))
Name: "{autoprograms}\KeyBase"; Filename: "{app}\run.bat"; WorkingDir: "{app}"; Check: not FileExists(ExpandConstant('{app}\KeyBase.exe'))
Name: "{autodesktop}\KeyBase"; Filename: "{app}\KeyBase.exe"; WorkingDir: "{app}"; Tasks: desktopicon; Check: FileExists(ExpandConstant('{app}\KeyBase.exe'))
Name: "{autodesktop}\KeyBase"; Filename: "{app}\run.bat"; WorkingDir: "{app}"; Tasks: desktopicon; Check: not FileExists(ExpandConstant('{app}\KeyBase.exe'))

[Tasks]
Name: "desktopicon"; Description: "Create a &desktop icon"; GroupDescription: "Additional shortcuts:"; Flags: unchecked

[Run]
; Post-install launch (skip if silent install)
Filename: "{app}\KeyBase.exe"; Description: "Launch KeyBase"; Flags: nowait postinstall skipifsilent; Check: FileExists(ExpandConstant('{app}\KeyBase.exe'))
Filename: "{app}\run.bat"; Description: "Launch KeyBase"; Flags: nowait postinstall skipifsilent; Check: not FileExists(ExpandConstant('{app}\KeyBase.exe'))

; No external JRE installation logic needed; bundled runtime is always included.