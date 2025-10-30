@echo off
echo KeyBase License Key Generator
echo ================================
echo.

REM Check if lib directory exists
if not exist lib (
  echo ERROR: lib directory not found.
  pause
  exit /b 1
)

REM Check if build directory exists
if not exist build\classes (
  echo Compiling...
  if not exist build mkdir build
  if not exist build\classes mkdir build\classes
  
  dir /s /b src\*.java > build\sources.txt
  javac -encoding UTF-8 -cp "lib/*" -d build\classes @build\sources.txt
  
  if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Compilation failed.
    pause
    exit /b 1
  )
)

echo Starting License Key Generator...
java -cp "lib/*;build\classes" src.LicenseKeyGenerator

pause
