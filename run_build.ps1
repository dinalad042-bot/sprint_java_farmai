$ErrorActionPreference = "Stop"

# 1. Define Paths
$projectRoot = $PSScriptRoot
$javaPath = "C:\Users\Aymen Ben Salem\.jdks\jbr-17.0.14"

Write-Host "==========================================" -ForegroundColor Green
Write-Host "  FarmAi PowerShell Launcher" -ForegroundColor Green
Write-Host "=========================================="
Write-Host "Project Root: $projectRoot"
Write-Host "Java Path:    $javaPath"

# 2. Verify Java
if (-not (Test-Path "$javaPath\bin\java.exe")) {
    Write-Error "Java not found at $javaPath"
}
$env:JAVA_HOME = $javaPath
$env:PATH = "$javaPath\bin;$env:PATH"

& java -version

# 3. Find Maven (mvn.cmd)
Write-Host "`nFinding Maven..." -ForegroundColor Cyan
$mvnCmd = Get-ChildItem -Path "$env:USERPROFILE\.m2\wrapper\dists" -Recurse -Filter "mvn.cmd" -ErrorAction SilentlyContinue | Select-Object -First 1 -ExpandProperty FullName

if (-not $mvnCmd) {
    Write-Error "Maven (mvn.cmd) not found in .m2 wrapper!"
}
Write-Host "Maven found at: $mvnCmd"

# 4. Clean & Compile
Write-Host "`nCleaning and Compiling..." -ForegroundColor Cyan
& $mvnCmd clean compile -f "$projectRoot\pom.xml"
if ($LASTEXITCODE -ne 0) {
    Write-Error "Compilation Failed!"
}

# 5. Run App
Write-Host "`nRunning Application..." -ForegroundColor Cyan
& $mvnCmd javafx:run -f "$projectRoot\pom.xml"
if ($LASTEXITCODE -ne 0) {
    Write-Error "Application Run Failed!"
}

Write-Host "`n[DONE]" -ForegroundColor Green
