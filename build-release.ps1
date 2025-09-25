# Android Calculator - Release Build Script
# Builds the final production APK with security features and obfuscation

param(
    [switch]$Clean = $false,
    [string]$OutputDir = "releases"
)

Write-Host "Android Calculator - Release Build Script" -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Green

# Create output directory
if (!(Test-Path $OutputDir)) {
    New-Item -ItemType Directory -Path $OutputDir | Out-Null
    Write-Host "Created output directory: $OutputDir" -ForegroundColor Yellow
}

# Clean if requested
if ($Clean) {
    Write-Host "Cleaning project..." -ForegroundColor Yellow
    .\gradlew clean
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Clean failed!" -ForegroundColor Red
        exit 1
    }
}

# Build the release APK
Write-Host "Building release APK..." -ForegroundColor Yellow
.\gradlew assembleaggressiveRelease -x lintVitalAnalyzeAggressiveRelease -x lintVitalReportAggressiveRelease -x lintVitalAggressiveRelease

if ($LASTEXITCODE -ne 0) {
    Write-Host "Build failed!" -ForegroundColor Red
    exit 1
}

# Generate timestamp
$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"

# Copy APK to releases directory
$apkName = "calculator-secure-$timestamp.apk"
$apkPath = "app\build\outputs\apk\aggressive\release\app-aggressive-release.apk"
$outputPath = "$OutputDir\$apkName"

Copy-Item $apkPath $outputPath

# Copy mapping files
$mappingName = "mapping-$timestamp.txt"
$seedsName = "seeds-$timestamp.txt"
$usageName = "usage-$timestamp.txt"

Copy-Item "app\build\outputs\mapping\aggressiveRelease\mapping.txt" "$OutputDir\$mappingName"
Copy-Item "app\build\outputs\mapping\aggressiveRelease\seeds.txt" "$OutputDir\$seedsName"
Copy-Item "app\build\outputs\mapping\aggressiveRelease\usage.txt" "$OutputDir\$usageName"

# Display results
Write-Host ""
Write-Host "Build completed successfully!" -ForegroundColor Green
Write-Host "APK: $outputPath" -ForegroundColor Cyan
Write-Host "Size: $([math]::Round((Get-Item $outputPath).Length / 1MB, 2)) MB" -ForegroundColor Cyan
Write-Host "Mapping: $OutputDir\$mappingName" -ForegroundColor Cyan
Write-Host "Seeds: $OutputDir\$seedsName" -ForegroundColor Cyan
Write-Host "Usage: $OutputDir\$usageName" -ForegroundColor Cyan
Write-Host ""
Write-Host "Features enabled:" -ForegroundColor Yellow
Write-Host "- AntiDebug protection" -ForegroundColor White
Write-Host "- Code obfuscation" -ForegroundColor White
Write-Host "- Resource shrinking" -ForegroundColor White
Write-Host "- Selective testing mode" -ForegroundColor White
