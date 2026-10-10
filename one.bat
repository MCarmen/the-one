@echo off
setlocal

if not defined JAVA_HOME (
	for /d %%J in ("C:\Program Files\Eclipse Adoptium\jdk-*-hotspot") do set "JAVA_HOME=%%~fJ"
)

if defined JAVA_HOME (
	set "JAVA=%JAVA_HOME%\bin\java.exe"
) else (
	set "JAVA=java"
)

if not exist "%JAVA%" (
	where java >nul 2>nul
	if errorlevel 1 (
		echo No se ha encontrado java. Instala un JDK/JRE o configura JAVA_HOME.
		exit /b 1
	)
	set "JAVA=java"
)

"%JAVA%" -Xmx512M -cp target;lib/ECLA.jar;lib/DTNConsoleConnection.jar core.DTNSim %*
