@echo off
setlocal

set "targetdir=target"

if not defined JAVA_HOME (
	for /d %%J in ("C:\Program Files\Eclipse Adoptium\jdk-*-hotspot") do set "JAVA_HOME=%%~fJ"
)

if defined JAVA_HOME (
	set "JAVAC=%JAVA_HOME%\bin\javac.exe"
) else (
	set "JAVAC=javac"
)

if not exist "%JAVAC%" (
	where javac >nul 2>nul
	if errorlevel 1 (
		echo No se ha encontrado javac. Instala un JDK o configura JAVA_HOME.
		exit /b 1
	)
	set "JAVAC=javac"
)

IF NOT EXIST "%targetdir%" mkdir %targetdir%

"%JAVAC%" -sourcepath src -d %targetdir% -cp lib/ECLA.jar;lib/DTNConsoleConnection.jar src/core/*.java src/movement/*.java src/report/*.java src/routing/*.java src/routing/anycast/*.java src/gui/*.java src/input/*.java src/applications/*.java src/interfaces/*.java
if errorlevel 1 exit /b %errorlevel%



IF NOT EXIST "%targetdir%\gui\buttonGraphics" (
	mkdir %targetdir%\gui\buttonGraphics
	copy src\gui\buttonGraphics\* %targetdir%\gui\buttonGraphics\
)
