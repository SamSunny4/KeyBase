# Installer packaging notes

This folder contains the scripts and configuration used to build the Windows installer for KeyBase.

Files of interest

- `prepare-dist.ps1` - PowerShell staging script. It:
  - Compiles the Java sources and creates `KeyBase.jar`.
  - Copies `config/`, `lib/`, `resources/`, `images/`, `data/`, and compiled classes into `dist/KeyBase`.
  - Optionally copies or expands a portable JRE into `dist/KeyBase/jre` when a folder is placed at `installer/packages/jre-portable/` or a zip `installer/packages/portable-jre.zip` is present.
  - Runs Launch4j (if installed) to produce `KeyBase.exe`.

- `keybase.iss` - Inno Setup script used to create `KeyBase-Setup.exe` from the staged distribution under `dist/KeyBase`.

How to bundle a portable JRE

1. Download a portable JRE (recommended: Java 17 LTS) such as Azul Zulu or Eclipse Temurin.
2. Extract the JRE to a folder named `jre` and place that folder under `installer/packages/jre-portable/` (so the path looks like `installer/packages/jre-portable/jre/bin/java.exe`).
   - Alternatively, place a zip archive of the JRE at `installer/packages/portable-jre.zip` — the staging script will extract it and flatten a single top-level folder if necessary.
3. Run the staging script:

```powershell
& 'powershell' -NoProfile -ExecutionPolicy Bypass -File 'installer\prepare-dist.ps1'
```

4. Rebuild the installer with Inno Setup (command-line compiler):

```powershell
& 'C:\Program Files (x86)\Inno Setup 6\ISCC.exe' 'installer\keybase.iss'
```

Notes

- The installer will include the staged `jre` folder verbatim. Make sure the JRE you bundle matches the Launch4j configuration (x64 vs x86) and the Java version targeted by the build.
- Do not commit large JRE binaries to source control; add them to the `.gitignore` and use a release artifact or package server for distribution.
