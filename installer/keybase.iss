#define ProjectRoot "E:\\KeyBase"
#define StageRoot   "E:\\KeyBase\\dist"
#define MyAppName "KeyBase"
#define MyAppVersion "1.0.0"
#define MyCompany "YourCompany"
#define InstallDirName "KeyBase"
#define JreInstaller "E:\\KeyBase\\installer\\packages\\jre-8u471-windows-x64.exe"

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

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Files]
; Core launcher / optional executable
Source: "{#ProjectRoot}\run.bat"; DestDir: "{app}"; Flags: ignoreversion
Source: "{#StageRoot}\KeyBase.exe"; DestDir: "{app}"; Flags: ignoreversion; Check: FileExists(ExpandConstant('{#StageRoot}\KeyBase.exe'))

; Application binaries (prefer jar, fallback to compiled classes)
Source: "{#StageRoot}\KeyBase.jar"; DestDir: "{app}"; Flags: ignoreversion; Check: FileExists(ExpandConstant('{#StageRoot}\KeyBase.jar'))
Source: "{#StageRoot}\app\KeyBase.jar"; DestDir: "{app}\app"; Flags: ignoreversion; Check: FileExists(ExpandConstant('{#StageRoot}\app\KeyBase.jar'))
Source: "{#StageRoot}\app\classes\*"; DestDir: "{app}\app\classes"; Flags: recursesubdirs createallsubdirs ignoreversion; Check: DirExists(ExpandConstant('{#StageRoot}\app\classes'))
Source: "{#StageRoot}\src\*"; DestDir: "{app}\src"; Flags: recursesubdirs createallsubdirs ignoreversion; Check: DirExists(ExpandConstant('{#StageRoot}\src'))

; Runtime dependencies
Source: "{#StageRoot}\lib\*"; DestDir: "{app}\lib"; Flags: recursesubdirs createallsubdirs ignoreversion; Check: DirExists(ExpandConstant('{#StageRoot}\lib'))
Source: "{#StageRoot}\resources\*"; DestDir: "{app}\resources"; Flags: recursesubdirs createallsubdirs ignoreversion; Check: DirExists(ExpandConstant('{#StageRoot}\resources'))

; Configuration and seed data
Source: "{#StageRoot}\config\*"; DestDir: "{app}\config"; Flags: recursesubdirs createallsubdirs ignoreversion; Check: DirExists(ExpandConstant('{#StageRoot}\config'))
Source: "{#StageRoot}\data\*"; DestDir: "{app}\data"; Flags: recursesubdirs createallsubdirs ignoreversion; Check: DirExists(ExpandConstant('{#StageRoot}\data'))
Source: "{#StageRoot}\images\*"; DestDir: "{app}\images"; Flags: recursesubdirs createallsubdirs ignoreversion; Check: DirExists(ExpandConstant('{#StageRoot}\images'))

; Optional bundled JRE
Source: "{#StageRoot}\jre\*"; DestDir: "{app}\jre"; Flags: recursesubdirs createallsubdirs ignoreversion; Check: DirExists(ExpandConstant('{#StageRoot}\jre'))

; External JRE installer (copied only when needed)
Source: "{#JreInstaller}"; DestDir: "{tmp}"; Flags: deleteafterinstall; Check: not HasJre()

[Icons]
Name: "{autoprograms}\KeyBase"; Filename: "{app}\KeyBase.exe"; WorkingDir: "{app}"; Check: FileExists(ExpandConstant('{app}\KeyBase.exe'))
Name: "{autoprograms}\KeyBase"; Filename: "{app}\run.bat"; WorkingDir: "{app}"; Check: not FileExists(ExpandConstant('{app}\KeyBase.exe'))
Name: "{autodesktop}\KeyBase"; Filename: "{app}\KeyBase.exe"; WorkingDir: "{app}"; Tasks: desktopicon; Check: FileExists(ExpandConstant('{app}\KeyBase.exe'))
Name: "{autodesktop}\KeyBase"; Filename: "{app}\run.bat"; WorkingDir: "{app}"; Tasks: desktopicon; Check: not FileExists(ExpandConstant('{app}\KeyBase.exe'))

[Tasks]
Name: "desktopicon"; Description: "Create a &desktop icon"; GroupDescription: "Additional shortcuts:"; Flags: unchecked

[Run]
; Install standalone JRE when the system lacks one and no bundled runtime is provided
Filename: "{tmp}\jre-8u471-windows-x64.exe"; Parameters: "/s"; StatusMsg: "Installing Java Runtime Environment..."; Check: not HasJre()
; Post-install launch (skip if silent install)
Filename: "{app}\KeyBase.exe"; Description: "Launch KeyBase"; Flags: nowait postinstall skipifsilent; Check: FileExists(ExpandConstant('{app}\KeyBase.exe'))
Filename: "{app}\run.bat"; Description: "Launch KeyBase"; Flags: nowait postinstall skipifsilent; Check: not FileExists(ExpandConstant('{app}\KeyBase.exe'))

[Code]
function HasJre(): Boolean;
begin
  Result :=
    ExpandConstant('{app}\jre\bin\javaw.exe') <> '' and
    FileExists(ExpandConstant('{app}\jre\bin\javaw.exe')) or
    RegKeyExists(HKLM64, 'SOFTWARE\JavaSoft\JRE') or
    RegKeyExists(HKLM, 'SOFTWARE\JavaSoft\JRE');
end;