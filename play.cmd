@echo off
setlocal
cd /d "%~dp0"
call "%~dp0mvnw.cmd" -q -DskipTests package
if errorlevel 1 exit /b 1
if defined JAVA_HOME (
    "%JAVA_HOME%\bin\java.exe" -jar "%~dp0target\emberbound.jar" %*
) else (
    java -jar "%~dp0target\emberbound.jar" %*
)
exit /b %errorlevel%
