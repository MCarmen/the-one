@echo off
setlocal
cd /d "%~dp0"

if not defined JAVA_HOME (
	for /d %%J in ("C:\Program Files\Eclipse Adoptium\jdk-*-hotspot") do set "JAVA_HOME=%%~fJ"
)

if defined JAVA_HOME (
	set "JAVAC=%JAVA_HOME%\bin\javac.exe"
	set "JAVA=%JAVA_HOME%\bin\java.exe"
) else (
	set "JAVAC=javac"
	set "JAVA=java"
)

set "CP=target;lib/ECLA.jar;lib/DTNConsoleConnection.jar;lib/test/junit-4.13.2.jar;lib/test/hamcrest-core-1.3.jar"

call "%~dp0compile.bat"
if errorlevel 1 exit /b %errorlevel%

"%JAVAC%" -sourcepath src -d target -cp "%CP%" src/test/*.java
if errorlevel 1 exit /b %errorlevel%

"%JAVA%" -Xmx512M -cp "%CP%" junit.textui.TestRunner test.AllTests
