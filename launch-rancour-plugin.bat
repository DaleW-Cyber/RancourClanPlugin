@echo off
setlocal

cd /d "%~dp0"

set "JAR="
for %%F in ("%~dp0rancour-clan-plugin-*-all.jar") do (
	set "JAR=%%~fF"
)

if "%JAR%"=="" (
	echo Could not find rancour-clan-plugin-*-all.jar in this folder.
	echo.
	echo Put this launcher file in the same folder as the Rancour plugin test jar.
	pause
	exit /b 1
)

where java >nul 2>nul
if errorlevel 1 (
	echo Java was not found.
	echo.
	echo Install Java 11 or newer, then run this launcher again.
	echo Recommended: Eclipse Temurin Java 17 from https://adoptium.net/
	pause
	exit /b 1
)

echo Starting RuneLite with the Rancour PvM test plugin...
echo.
java -ea -jar "%JAR%" --developer-mode --debug

if errorlevel 1 (
	echo.
	echo RuneLite closed with an error. Take a screenshot of this window and send it to staff.
	pause
	exit /b %errorlevel%
)
