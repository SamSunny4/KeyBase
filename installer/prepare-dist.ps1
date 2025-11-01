param(
    [string]$Configuration = "release"
)

$ErrorActionPreference = "Stop"

function Write-Stage([string]$Message) {
    Write-Host "[KeyBase] $Message"
}

$projectRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$buildRoot   = Join-Path $projectRoot "build"
$classesDir  = Join-Path $buildRoot "classes"
$distRoot    = Join-Path $projectRoot "dist"
$stageRoot   = Join-Path $distRoot "KeyBase"

Write-Stage "Project root: $projectRoot"

if (Test-Path $stageRoot) {
    Write-Stage "Cleaning existing stage folder"
    Remove-Item $stageRoot -Recurse -Force
}
New-Item -ItemType Directory -Path $stageRoot | Out-Null

if (Test-Path $classesDir) {
    Write-Stage "Cleaning previous compiled classes"
    Remove-Item $classesDir -Recurse -Force
}
New-Item -ItemType Directory -Path $classesDir | Out-Null

$sourceList = Join-Path $buildRoot "sources.txt"
Get-ChildItem (Join-Path $projectRoot "src") -Recurse -Filter *.java | Select-Object -ExpandProperty FullName | Out-File -FilePath $sourceList -Encoding ascii

$libPath = Join-Path $projectRoot "lib\*"
$javacArgs = @('-encoding', 'UTF-8', '-cp', $libPath, '-d', $classesDir, "@$sourceList")
Write-Stage "Compiling sources"
& javac @javacArgs
if ($LASTEXITCODE -ne 0) {
    throw "javac failed with exit code $LASTEXITCODE"
}

$jarOutput = Join-Path $distRoot "KeyBase.jar"
if (Test-Path $jarOutput) { Remove-Item $jarOutput -Force }

$jarTool = $null
$jarTool = Get-Command jar -ErrorAction SilentlyContinue
if (-not $jarTool) {
    $javacCommand = Get-Command javac -ErrorAction SilentlyContinue
    if ($javacCommand) {
        $candidate = Join-Path (Split-Path $javacCommand.Source -Parent) "jar.exe"
        if (Test-Path $candidate) {
            $jarTool = $candidate
        }
    }
}

if (-not $jarTool) {
    throw "jar tool not found. Ensure a full JDK is installed and jar.exe is on PATH."
}

Write-Stage "Creating application JAR"
& $jarTool --create --file $jarOutput --main-class src.KeyBase -C $classesDir .
if ($LASTEXITCODE -ne 0) {
    throw "jar command failed with exit code $LASTEXITCODE"
}

Copy-Item $jarOutput (Join-Path $stageRoot "KeyBase.jar") -Force

function Copy-Tree {
    param(
        [string]$Source,
        [string]$Destination,
        [string[]]$Exclude = @("*.md", "*.bat", "*.ps1", "*.xml", "*.bak")
    )

    if (-not (Test-Path $Source)) { return }
    Write-Stage "Copying $(Split-Path $Source -Leaf)"
    Get-ChildItem $Source -Recurse | ForEach-Object {
        if ($_.PSIsContainer) { return }
        foreach ($pattern in $Exclude) {
            if ($_.Name -like $pattern) { return }
        }
        $relative = $_.FullName.Substring($Source.Length)
        $relative = $relative.TrimStart('\', '/')
        $destPath = Join-Path $Destination $relative
        $destDir  = Split-Path $destPath
        if (-not (Test-Path $destDir)) {
            New-Item -ItemType Directory -Path $destDir -Force | Out-Null
        }
        Copy-Item $_.FullName $destPath -Force
    }
}

Copy-Tree (Join-Path $projectRoot "config")    (Join-Path $stageRoot "config")    @("*.bak")
Copy-Tree (Join-Path $projectRoot "lib")       (Join-Path $stageRoot "lib")       @()
Copy-Tree (Join-Path $projectRoot "resources") (Join-Path $stageRoot "resources") @()
Copy-Tree (Join-Path $projectRoot "images")    (Join-Path $stageRoot "images")    @()
Copy-Tree (Join-Path $projectRoot "data")      (Join-Path $stageRoot "data")      @("*.trace.db")

$stageClasses = Join-Path $stageRoot "app\classes"
Copy-Tree $classesDir $stageClasses @()

$toolsDir = Join-Path $stageRoot "tools"
if (Test-Path $toolsDir) { Remove-Item $toolsDir -Recurse -Force }

Copy-Item (Join-Path $projectRoot "run.bat") (Join-Path $stageRoot "run.bat") -Force

if (Test-Path (Join-Path $projectRoot "jre")) {
    Copy-Tree (Join-Path $projectRoot "jre") (Join-Path $stageRoot "jre") @()
}

Write-Stage "Staging complete: $stageRoot"
