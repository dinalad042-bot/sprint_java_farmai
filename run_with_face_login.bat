@echo off
set "JAVA_HOME=C:\Users\Aymen Ben Salem\.jdks\jbr-17.0.14"
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo ==========================================
echo   FarmAi Face Login Launcher
echo ==========================================
echo Using JAVA_HOME: "%JAVA_HOME%"

if exist "%JAVA_HOME%\bin\java.exe" goto JavaFound
echo [ERROR] Java not found at expected path!
echo Path checked: "%JAVA_HOME%\bin\java.exe"
pause
exit /b 1

:JavaFound
echo [OK] Java detected.
"%JAVA_HOME%\bin\java.exe" -version

echo.
echo ==========================================
echo   Cleaning and Compiling...
echo ==========================================
call mvnw.cmd clean compile
if %errorlevel% neq 0 goto CompileFailed

echo.
echo ==========================================
echo   Running Application...
echo ==========================================
call mvnw.cmd javafx:run
if %errorlevel% neq 0 goto RunFailed

echo [SUCCESS] Application finished.
pause
exit /b 0

:CompileFailed
echo.
echo [ERROR] Compilation failed!
pause
exit /b 1

:RunFailed
echo.
echo [ERROR] Application failed to launch!
pause
exit /b 1
