@echo off
chcp 65001 >nul
setlocal

rem ===========================================================
rem  Simulated QQ server (OneBot 11, port 3000)
rem
rem  Double-click to start. Press Ctrl+C in this window to stop.
rem  Data (chat history + images) lives in .\data\ next to this jar.
rem
rem  NOTE: This file is intentionally ASCII-only. cmd.exe reads .bat
rem  as GBK before chcp takes effect, so any non-ASCII text here
rem  would be executed as commands instead of being printed.
rem ===========================================================

cd /d "%~dp0"

if not exist "qq.jar" (
    echo [ERROR] qq.jar not found in this folder.
    pause
    exit /b 1
)

where java >nul 2>nul
if errorlevel 1 (
    echo [ERROR] java not found on PATH.
    echo         Install JDK 17+ and add its bin directory to PATH.
    pause
    exit /b 1
)

echo ===========================================================
echo   Simulated QQ server
echo   Listen   http://127.0.0.1:3000/
echo   Data     %~dp0data
echo ===========================================================
echo.
echo   Press Ctrl+C to stop
echo.

rem Explicit paths keep behaviour identical no matter where the
rem script is launched from (double-click vs command line).
java -jar qq.jar ^
    --sim.data-file="%~dp0data\chat.json" ^
    --sim.image-dir="%~dp0data\img"

echo.
echo Server exited.
pause
