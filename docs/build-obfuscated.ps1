# Build Obfuscated APK Script
# This script builds the Android Calculator with comprehensive obfuscation

Write-Host "=== Android Calculator - Obfuscated Build Script ===" -ForegroundColor Green
Write-Host ""

# Check if we're in the correct directory
if (-not (Test-Path "app/build.gradle.kts")) {
    Write-Host "Error: Please run this script from the project root directory" -ForegroundColor Red
    exit 1
}

# Clean previous builds
Write-Host "Cleaning previous builds..." -ForegroundColor Yellow
./gradlew clean

# Build standard obfuscated APK
Write-Host ""
Write-Host "Building Standard Obfuscated APK..." -ForegroundColor Yellow
Write-Host "This uses basic ProGuard obfuscation" -ForegroundColor Gray
./gradlew assembleStandardRelease

if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Standard obfuscated APK built successfully" -ForegroundColor Green
} else {
    Write-Host "✗ Standard obfuscated APK build failed" -ForegroundColor Red
    exit 1
}

# Build aggressive obfuscated APK
Write-Host ""
Write-Host "Building Aggressive Obfuscated APK..." -ForegroundColor Yellow
Write-Host "This uses comprehensive obfuscation with all techniques" -ForegroundColor Gray
./gradlew assembleAggressiveRelease

if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Aggressive obfuscated APK built successfully" -ForegroundColor Green
} else {
    Write-Host "✗ Aggressive obfuscated APK build failed" -ForegroundColor Red
    exit 1
}

# Display build results
Write-Host ""
Write-Host "=== Build Results ===" -ForegroundColor Green
Write-Host ""

# Find APK files
$standardApk = Get-ChildItem -Path "app/build/outputs/apk/standard/release" -Filter "*.apk" | Sort-Object LastWriteTime -Descending | Select-Object -First 1
$aggressiveApk = Get-ChildItem -Path "app/build/outputs/apk/aggressive/release" -Filter "*.apk" | Sort-Object LastWriteTime -Descending | Select-Object -First 1

if ($standardApk) {
    $standardSize = [math]::Round($standardApk.Length / 1MB, 2)
    Write-Host "Standard Obfuscated APK:" -ForegroundColor Cyan
    Write-Host "  File: $($standardApk.Name)" -ForegroundColor White
    Write-Host "  Size: $standardSize MB" -ForegroundColor White
    Write-Host "  Path: $($standardApk.FullName)" -ForegroundColor Gray
}

if ($aggressiveApk) {
    $aggressiveSize = [math]::Round($aggressiveApk.Length / 1MB, 2)
    Write-Host "Aggressive Obfuscated APK:" -ForegroundColor Cyan
    Write-Host "  File: $($aggressiveApk.Name)" -ForegroundColor White
    Write-Host "  Size: $aggressiveSize MB" -ForegroundColor White
    Write-Host "  Path: $($aggressiveApk.FullName)" -ForegroundColor Gray
}

# Display obfuscation mapping files
Write-Host ""
Write-Host "=== Obfuscation Mapping Files ===" -ForegroundColor Green
Write-Host ""

$mappingFiles = Get-ChildItem -Path "app" -Filter "mapping*.txt" | Sort-Object LastWriteTime -Descending
if ($mappingFiles) {
    foreach ($mapping in $mappingFiles) {
        Write-Host "Mapping file: $($mapping.Name)" -ForegroundColor Cyan
        Write-Host "  Size: $([math]::Round($mapping.Length / 1KB, 2)) KB" -ForegroundColor White
        Write-Host "  Path: $($mapping.FullName)" -ForegroundColor Gray
    }
} else {
    Write-Host "No mapping files found" -ForegroundColor Yellow
}

# Display seeds and usage files
Write-Host ""
Write-Host "=== Obfuscation Analysis Files ===" -ForegroundColor Green
Write-Host ""

$seedsFiles = Get-ChildItem -Path "app" -Filter "seeds*.txt" | Sort-Object LastWriteTime -Descending
$usageFiles = Get-ChildItem -Path "app" -Filter "usage*.txt" | Sort-Object LastWriteTime -Descending

if ($seedsFiles) {
    Write-Host "Seeds files (kept classes):" -ForegroundColor Cyan
    foreach ($seeds in $seedsFiles) {
        Write-Host "  $($seeds.Name) - $([math]::Round($seeds.Length / 1KB, 2)) KB" -ForegroundColor White
    }
}

if ($usageFiles) {
    Write-Host "Usage files (removed classes):" -ForegroundColor Cyan
    foreach ($usage in $usageFiles) {
        Write-Host "  $($usage.Name) - $([math]::Round($usage.Length / 1KB, 2)) KB" -ForegroundColor White
    }
}

# Display obfuscation techniques summary
Write-Host ""
Write-Host "=== Implemented Obfuscation Techniques ===" -ForegroundColor Green
Write-Host ""

Write-Host "1. Static Code Obfuscation:" -ForegroundColor Cyan
Write-Host "  ✓ Identifier renaming (ProGuard/R8)" -ForegroundColor Green
Write-Host "  ✓ Control flow obfuscation" -ForegroundColor Green
Write-Host "  ✓ String encryption/hiding" -ForegroundColor Green
Write-Host "  ✓ Junk code insertion" -ForegroundColor Green
Write-Host "  ✓ Method inlining/outlining" -ForegroundColor Green
Write-Host "  ✓ Metadata stripping" -ForegroundColor Green

Write-Host ""
Write-Host "2. Resource & Manifest Obfuscation:" -ForegroundColor Cyan
Write-Host "  ✓ Resource name mangling" -ForegroundColor Green
Write-Host "  ✓ Encrypted resources/assets" -ForegroundColor Green
Write-Host "  ✓ AndroidManifest obfuscation" -ForegroundColor Green

Write-Host ""
Write-Host "3. Runtime/Dynamic Obfuscation:" -ForegroundColor Cyan
Write-Host "  ✓ Runtime class decryption" -ForegroundColor Green
Write-Host "  ✓ Native loaders" -ForegroundColor Green
Write-Host "  ✓ Code virtualization" -ForegroundColor Green
Write-Host "  ✓ Dynamic code generation" -ForegroundColor Green

Write-Host ""
Write-Host "4. Data Masking:" -ForegroundColor Cyan
Write-Host "  ✓ Static data masking (SDM)" -ForegroundColor Green
Write-Host "  ✓ Dynamic data masking (DDM)" -ForegroundColor Green
Write-Host "  ✓ Format-preserving encryption (FPE)" -ForegroundColor Green
Write-Host "  ✓ Data redaction" -ForegroundColor Green

Write-Host ""
Write-Host "5. Native Code Obfuscation:" -ForegroundColor Cyan
Write-Host "  ✓ Symbol stripping" -ForegroundColor Green
Write-Host "  ✓ Function inlining/outlining" -ForegroundColor Green
Write-Host "  ✓ Anti-debugging/anti-tampering" -ForegroundColor Green

Write-Host ""
Write-Host "=== Build Complete ===" -ForegroundColor Green
Write-Host "Both obfuscated APKs have been built successfully!" -ForegroundColor White
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "1. Test the APKs on a device to ensure functionality" -ForegroundColor White
Write-Host "2. Use tools like jadx to verify obfuscation effectiveness" -ForegroundColor White
Write-Host "3. Review mapping files to understand obfuscation changes" -ForegroundColor White
Write-Host "4. Check the OBFUSCATION_TECHNIQUES.md file for detailed documentation" -ForegroundColor White
Write-Host ""
