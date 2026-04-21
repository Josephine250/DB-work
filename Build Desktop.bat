@echo off
echo ====================================================
echo   COLLEGE PORTAL - BUILD SCRIPT
echo   Run this ONLY when you have changed Java code.
echo   For normal use, run 'Launch Desktop.bat' instead.
echo ====================================================
echo.

echo Building application (this may take ~60 seconds)...
echo.

.\mvnw package -DskipTests -q

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Build FAILED! Check for compilation errors above.
    pause
    exit /b 1
)

echo.
echo [SUCCESS] Build complete! You can now run 'Launch Desktop.bat'.
echo.
pause
