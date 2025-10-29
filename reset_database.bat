@echo off
echo KeyBase - Database Reset Utility
echo ==================================
echo.
echo WARNING: This will delete all existing data!
echo.
set /p confirm="Are you sure you want to continue? (yes/no): "

if /i not "%confirm%"=="yes" (
  echo Operation cancelled.
  pause
  exit /b 0
)

echo.
echo Compiling reset utility...
if not exist build\classes mkdir build\classes

javac -cp "lib/*;build\classes" -d build\classes src\ResetDatabase.java

if %ERRORLEVEL% NEQ 0 (
  echo ERROR: Compilation failed.
  pause
  exit /b 1
)

echo.
echo Resetting database and loading test data...
java -cp "lib/*;build\classes" src.ResetDatabase

if %ERRORLEVEL% NEQ 0 (
  echo ERROR: Database reset failed.
  pause
  exit /b 1
)

echo.
echo Done! You can now run the application with run.bat
pause
