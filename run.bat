@echo off
echo KeyBase - Key Management System
echo ================================

REM Check if lib directory exists
if not exist lib (
  echo ERROR: lib directory not found. Please make sure all required JAR files are in the lib folder.
  pause
  exit /b 1
)

REM Always compile to ensure latest changes are included
echo Compiling application...

REM Create build directory if it doesn't exist
if not exist build mkdir build
if not exist build\classes mkdir build\classes

REM Create sources list
dir /s /b src\*.java > build\sources.txt

javac -encoding UTF-8 -cp "lib/*" -d build\classes @build\sources.txt

if %ERRORLEVEL% NEQ 0 (
  echo ERROR: Compilation failed.
  pause
  exit /b 1
)
echo Compilation successful.

REM Ensure data directory exists
if not exist data mkdir data

REM Check if database exists, if not initialize it
if not exist data\keybase.mv.db (
  echo Initializing database for the first time...
  java -cp "lib/*;build\classes" src.DatabaseInitializer
  
  if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Database initialization failed.
    pause
    exit /b 1
  )
)

echo Starting KeyBase application...
java -cp "lib/*;build\classes" src.KeyBase

pause