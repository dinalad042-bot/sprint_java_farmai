@echo off
echo Building project...
call mvnw.cmd clean compile 2>nul
if errorlevel 1 (
    echo Maven wrapper not found. Using Maven directly...
    mvn clean compile
    if errorlevel 1 (
        echo ERROR: Maven not found. Please install Maven or use IntelliJ IDEA.
        pause
        exit /b 1
    )
    mvn javafx:run
) else (
    call mvnw.cmd javafx:run
)
pause
