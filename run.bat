pause
@echo off
setlocal

rem -----------------------------------------------------------------
rem  KeyBase launcher for packaged installations
rem  - Prefers bundled JRE (./jre)
rem  - Falls back to system Java if needed
rem  - Works with KeyBase.jar or unpacked classes under app\classes
rem -----------------------------------------------------------------

set "SCRIPT_DIR=%~dp0"
set "SCRIPT_DIR=%SCRIPT_DIR:~0,-1%"

rem Locate Java runtime (prefer bundled JRE)
set "JAVA_EXE=%SCRIPT_DIR%\jre\bin\javaw.exe"
if exist "%JAVA_EXE%" goto :detectClasspath

set "JAVA_EXE=%SCRIPT_DIR%\jre\bin\java.exe"
if exist "%JAVA_EXE%" goto :detectClasspath

for %%J in (javaw.exe java.exe) do (
  where %%J >nul 2>&1
  if not errorlevel 1 (
    set "JAVA_EXE=%%J"
    goto :detectClasspath
  )
)

echo.
echo ERROR: A compatible Java Runtime Environment was not found.
echo Please install Java 11 or newer, or bundle a JRE under %%SCRIPT_DIR%%\jre.
pause
exit /b 1

:detectClasspath
set "APP_CLASSPATH="

if exist "%SCRIPT_DIR%\KeyBase.jar" set "APP_CLASSPATH=%SCRIPT_DIR%\KeyBase.jar"
if exist "%SCRIPT_DIR%\app\KeyBase.jar" set "APP_CLASSPATH=%SCRIPT_DIR%\app\KeyBase.jar"

if exist "%SCRIPT_DIR%\app\classes" (
  if defined APP_CLASSPATH (
    set "APP_CLASSPATH=%APP_CLASSPATH%;%SCRIPT_DIR%\app\classes"
  ) else (
    set "APP_CLASSPATH=%SCRIPT_DIR%\app\classes"
  )
)

if exist "%SCRIPT_DIR%\src" (
  if defined APP_CLASSPATH (
    set "APP_CLASSPATH=%APP_CLASSPATH%;%SCRIPT_DIR%\src"
  ) else (
    set "APP_CLASSPATH=%SCRIPT_DIR%\src"
  )
)

if exist "%SCRIPT_DIR%\lib" (
  if defined APP_CLASSPATH (
    set "APP_CLASSPATH=%APP_CLASSPATH%;%SCRIPT_DIR%\lib\*"
  ) else (
    set "APP_CLASSPATH=%SCRIPT_DIR%\lib\*"
  )
)

if not defined APP_CLASSPATH (
  echo.
  echo ERROR: Unable to locate application binaries (KeyBase.jar or app\classes).
  pause
  exit /b 1
)

rem Ensure data directory exists within installation root
if not exist "%SCRIPT_DIR%\data" mkdir "%SCRIPT_DIR%\data"

pushd "%SCRIPT_DIR%"
"%JAVA_EXE%" -cp "%APP_CLASSPATH%" -Dfile.encoding=UTF-8 src.KeyBase
set "EXIT_CODE=%ERRORLEVEL%"
popd

if "%EXIT_CODE%"=="0" exit /b 0

rem If javaw failed, retry with console java for error output
if /i not "%JAVA_EXE%"=="java.exe" (
  for %%J in (java.exe) do (
    where %%J >nul 2>&1
    if not errorlevel 1 (
      pushd "%SCRIPT_DIR%"
      "%%J" -cp "%APP_CLASSPATH%" -Dfile.encoding=UTF-8 src.KeyBase
      set "EXIT_CODE=%ERRORLEVEL%"
      popd
      goto :finish
    )
  )
)

:finish
if not "%EXIT_CODE%"=="0" (
  echo.
  echo KeyBase exited with code %EXIT_CODE%.
  pause
)

exit /b %EXIT_CODE%