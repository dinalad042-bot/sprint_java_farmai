$ErrorActionPreference = "Stop"

$sourceDir = "c:\Users\Aymen Ben Salem\Desktop\FarmAi (3)\FarmAi"
$tempDir = "$env:TEMP\FarmAi_Clean_Build"
$javaPath = "C:\Users\Aymen Ben Salem\.jdks\jbr-17.0.14"

Write-Host "==========================================" -ForegroundColor Green
Write-Host "  FarmAi Clean Build Launcher" -ForegroundColor Green
Write-Host "=========================================="

# 1. Clean and Create Temp Dir
if (Test-Path $tempDir) {
    Write-Host "Cleaning old temp build..." -ForegroundColor Yellow
    Remove-Item -Path $tempDir -Recurse -Force
}
New-Item -ItemType Directory -Path $tempDir | Out-Null
Write-Host "Created temp dir: $tempDir"

# 2. Copy Project Files
Write-Host "Copying project files..." -ForegroundColor Cyan
# Copy pom.xml
Copy-Item "$sourceDir\pom.xml" "$tempDir\pom.xml"
# Copy src
Copy-Item "$sourceDir\src" "$tempDir\src" -Recurse
# Copy .mvn (wrapper)
Copy-Item "$sourceDir\.mvn" "$tempDir\.mvn" -Recurse
# Copy mvnw files
Copy-Item "$sourceDir\mvnw.cmd" "$tempDir\mvnw.cmd"
Copy-Item "$sourceDir\mvnw" "$tempDir\mvnw"

# 3. Setup Java
$env:JAVA_HOME = $javaPath
$env:PATH = "$javaPath\bin;$env:PATH"
& java -version

# 4. Build and Run in Temp
Set-Location $tempDir
Write-Host "`nBuilding in clean environment..." -ForegroundColor Cyan

# We use cmd /c to run the batch file properly
cmd /c "mvnw.cmd clean compile"

if ($LASTEXITCODE -eq 0) {
    Write-Host "`nBuild Success! Running App..." -ForegroundColor Green
    cmd /c "mvnw.cmd javafx:run"
} else {
    Write-Error "Build Failed in temp dir!"
}
