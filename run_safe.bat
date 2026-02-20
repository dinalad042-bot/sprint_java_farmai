@echo off
set "JDK_PATH=C:\Users\Aymen Ben Salem\.jdks\jbr-17.0.14"
set "PROJECT_ROOT=%~dp0"
if "%PROJECT_ROOT:~-1%"=="\" set "PROJECT_ROOT=%PROJECT_ROOT:~0,-1%"

> build.log 2>&1 (
    echo [INFO] Starting ULTRA SAFE build...
    
    :: Mount JDK to J: to avoid spaces in JAVA_HOME
    if exist J:\ subst J: /d
    echo [INFO] Mounting JDK to J: ...
    subst J: "%JDK_PATH%"
    if errorlevel 1 (
        echo [ERROR] Failed to mount J: drive.
        exit /b 1
    )
    set "JAVA_HOME=J:\"
    set "PATH=%JAVA_HOME%bin;%PATH%"
    
    echo [INFO] JAVA_HOME: "%JAVA_HOME%"
    java -version

    :: Mount Project to Z: to avoid spaces and parentheses
    if exist Z:\ subst Z: /d
    echo [INFO] Mounting Project to Z: ...
    subst Z: "%PROJECT_ROOT%"
    if errorlevel 1 (
        echo [ERROR] Failed to mount Z: drive.
        exit /b 1
    )

    pushd Z:\
    echo [INFO] Cleaning and Compiling...
    call mvnw.cmd clean compile
    if errorlevel 1 (
        echo [ERROR] Compilation failed!
        popd
        subst Z: /d
        subst J: /d
        exit /b 1
    )

    echo [INFO] Running...
    call mvnw.cmd javafx:run
    
    popd
    subst Z: /d
    subst J: /d
    echo [INFO] Done.
)
