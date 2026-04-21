@echo off
echo ====================================================
echo   COLLEGE PORTAL - FAST DESKTOP LAUNCHER
echo ====================================================
echo.

set JAR_FILE=target\demo-0.0.1-SNAPSHOT.jar

:: Check if the compiled JAR exists
if not exist "%JAR_FILE%" (
    echo [ERROR] JAR not found! Please run 'Build Desktop.bat' first.
    echo         This compiles the application. Only needed once after code changes.
    echo.
    pause
    exit /b 1
)

echo Starting College Portal Desktop...
echo Logs are being saved to 'debug_log.txt'
echo.

java ^
  -Dprism.order=sw ^
  -Dprism.vsync=false ^
  -Dprism.dirtyopts=true ^
  -Dprism.lcdtext=false ^
  -Dcom.sun.webkit.acceleratedLayerRendering=false ^
  -Xms256m -Xmx768m ^
  -XX:+UseG1GC ^
  -jar "%JAR_FILE%" > debug_log.txt 2>&1

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] The application crashed! Check 'debug_log.txt' for details.
    echo.
    type debug_log.txt | findstr /i "error"
    echo.
    pause
)
