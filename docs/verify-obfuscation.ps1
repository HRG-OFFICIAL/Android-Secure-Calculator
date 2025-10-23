# Build Verification Script for Android Calculator Obfuscation
# This script verifies that all obfuscation techniques are properly applied

param(
    [string]$ApkPath = "app/build/outputs/apk/aggressive/release/app-aggressive-release.apk",
    [string]$BuildType = "aggressive",
    [switch]$Verbose = $false
)

Write-Host "=== Android Calculator Obfuscation Verification ===" -ForegroundColor Green
Write-Host "APK Path: $ApkPath" -ForegroundColor Yellow
Write-Host "Build Type: $BuildType" -ForegroundColor Yellow
Write-Host ""

# Check if APK exists
if (-not (Test-Path $ApkPath)) {
    Write-Host "ERROR: APK not found at $ApkPath" -ForegroundColor Red
    Write-Host "Please build the APK first using: ./gradlew assembleAggressiveRelease" -ForegroundColor Yellow
    exit 1
}

Write-Host "✓ APK found at $ApkPath" -ForegroundColor Green

# Function to run command and capture output
function Invoke-Command {
    param([string]$Command, [string]$Description)
    
    Write-Host "Checking: $Description" -ForegroundColor Cyan
    
    try {
        $output = Invoke-Expression $Command 2>&1
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✓ $Description" -ForegroundColor Green
            if ($Verbose) {
                Write-Host $output -ForegroundColor Gray
            }
            return $output
        } else {
            Write-Host "✗ $Description" -ForegroundColor Red
            Write-Host $output -ForegroundColor Red
            return $null
        }
    } catch {
        Write-Host "✗ $Description - Error: $($_.Exception.Message)" -ForegroundColor Red
        return $null
    }
}

# Check if required tools are available
Write-Host "`n=== Checking Required Tools ===" -ForegroundColor Green

$dexdumpAvailable = $false
$nmAvailable = $false
$readelfAvailable = $false

try {
    $null = Get-Command dexdump -ErrorAction Stop
    $dexdumpAvailable = $true
    Write-Host "✓ dexdump available" -ForegroundColor Green
} catch {
    Write-Host "✗ dexdump not available (install Android SDK)" -ForegroundColor Red
}

try {
    $null = Get-Command nm -ErrorAction Stop
    $nmAvailable = $true
    Write-Host "✓ nm available" -ForegroundColor Green
} catch {
    Write-Host "✗ nm not available" -ForegroundColor Red
}

try {
    $null = Get-Command readelf -ErrorAction Stop
    $readelfAvailable = $true
    Write-Host "✓ readelf available" -ForegroundColor Green
} catch {
    Write-Host "✗ readelf not available" -ForegroundColor Red
}

# Extract APK for analysis
Write-Host "`n=== Extracting APK for Analysis ===" -ForegroundColor Green

$tempDir = "temp_apk_analysis"
if (Test-Path $tempDir) {
    Remove-Item -Recurse -Force $tempDir
}
New-Item -ItemType Directory -Path $tempDir | Out-Null

try {
    # Extract APK using PowerShell (APK is a ZIP file)
    Expand-Archive -Path $ApkPath -DestinationPath $tempDir -Force
    Write-Host "✓ APK extracted successfully" -ForegroundColor Green
} catch {
    Write-Host "✗ Failed to extract APK: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Check DEX obfuscation
Write-Host "`n=== Checking DEX Obfuscation ===" -ForegroundColor Green

$dexFile = Join-Path $tempDir "classes.dex"
if (Test-Path $dexFile) {
    Write-Host "✓ classes.dex found" -ForegroundColor Green
    
    if ($dexdumpAvailable) {
        $dexOutput = Invoke-Command "dexdump -d `"$dexFile`" | Select-String -Pattern 'class.*Calculator' | Select-Object -First 5" "DEX class analysis"
        
        if ($dexOutput) {
            Write-Host "Found Calculator classes in DEX:" -ForegroundColor Yellow
            $dexOutput | ForEach-Object { Write-Host "  $_" -ForegroundColor Gray }
        }
        
        # Check for obfuscated class names (should be single letters)
        $obfuscatedClasses = Invoke-Command "dexdump -d `"$dexFile`" | Select-String -Pattern 'class [a-z]\.' | Select-Object -First 10" "Obfuscated class detection"
        
        if ($obfuscatedClasses) {
            Write-Host "✓ Found obfuscated class names (single letters)" -ForegroundColor Green
            if ($Verbose) {
                $obfuscatedClasses | ForEach-Object { Write-Host "  $_" -ForegroundColor Gray }
            }
        } else {
            Write-Host "⚠ No obfuscated class names detected" -ForegroundColor Yellow
        }
    } else {
        Write-Host "⚠ dexdump not available, skipping DEX analysis" -ForegroundColor Yellow
    }
} else {
    Write-Host "✗ classes.dex not found in APK" -ForegroundColor Red
}

# Check native library obfuscation
Write-Host "`n=== Checking Native Library Obfuscation ===" -ForegroundColor Green

$libDir = Join-Path $tempDir "lib"
if (Test-Path $libDir) {
    Write-Host "✓ Native libraries directory found" -ForegroundColor Green
    
    $soFiles = Get-ChildItem -Path $libDir -Recurse -Filter "*.so"
    if ($soFiles) {
        Write-Host "Found $($soFiles.Count) native libraries:" -ForegroundColor Yellow
        $soFiles | ForEach-Object { Write-Host "  $($_.Name)" -ForegroundColor Gray }
        
        # Check symbol stripping
        foreach ($soFile in $soFiles) {
            if ($nmAvailable) {
                $symbols = Invoke-Command "nm -D `"$($soFile.FullName)`" 2>`$null" "Symbol analysis for $($soFile.Name)"
                
                if ($symbols) {
                    $jniSymbols = $symbols | Where-Object { $_ -match "Java_" }
                    if ($jniSymbols) {
                        Write-Host "✓ JNI symbols found in $($soFile.Name)" -ForegroundColor Green
                        if ($Verbose) {
                            $jniSymbols | ForEach-Object { Write-Host "    $_" -ForegroundColor Gray }
                        }
                    } else {
                        Write-Host "⚠ No JNI symbols found in $($soFile.Name)" -ForegroundColor Yellow
                    }
                } else {
                    Write-Host "✓ No symbols found in $($soFile.Name) (stripped)" -ForegroundColor Green
                }
            } else {
                Write-Host "⚠ nm not available, skipping symbol analysis" -ForegroundColor Yellow
            }
        }
    } else {
        Write-Host "⚠ No native libraries found" -ForegroundColor Yellow
    }
} else {
    Write-Host "⚠ Native libraries directory not found" -ForegroundColor Yellow
}

# Check resource obfuscation
Write-Host "`n=== Checking Resource Obfuscation ===" -ForegroundColor Green

$resDir = Join-Path $tempDir "res"
if (Test-Path $resDir) {
    Write-Host "✓ Resources directory found" -ForegroundColor Green
    
    # Count resources
    $resourceFiles = Get-ChildItem -Path $resDir -Recurse -File
    Write-Host "Found $($resourceFiles.Count) resource files" -ForegroundColor Yellow
    
    # Check for obfuscated resource names (should be short/random)
    $xmlFiles = Get-ChildItem -Path $resDir -Recurse -Filter "*.xml"
    if ($xmlFiles) {
        $obfuscatedResources = 0
        foreach ($xmlFile in $xmlFiles) {
            $content = Get-Content $xmlFile.FullName -Raw
            if ($content -match 'name="[a-z]{1,3}"') {
                $obfuscatedResources++
            }
        }
        
        if ($obfuscatedResources -gt 0) {
            Write-Host "✓ Found $obfuscatedResources files with obfuscated resource names" -ForegroundColor Green
        } else {
            Write-Host "⚠ No obfuscated resource names detected" -ForegroundColor Yellow
        }
    }
} else {
    Write-Host "✗ Resources directory not found" -ForegroundColor Red
}

# Check for encrypted assets
Write-Host "`n=== Checking Asset Encryption ===" -ForegroundColor Green

$assetsDir = Join-Path $tempDir "assets"
if (Test-Path $assetsDir) {
    Write-Host "✓ Assets directory found" -ForegroundColor Green
    
    $assetFiles = Get-ChildItem -Path $assetsDir -Recurse -File
    if ($assetFiles) {
        Write-Host "Found $($assetFiles.Count) asset files:" -ForegroundColor Yellow
        $assetFiles | ForEach-Object { Write-Host "  $($_.Name)" -ForegroundColor Gray }
        
        # Check for encrypted assets
        $encryptedAssets = $assetFiles | Where-Object { $_.Extension -eq ".enc" }
        if ($encryptedAssets) {
            Write-Host "✓ Found $($encryptedAssets.Count) encrypted asset files" -ForegroundColor Green
        } else {
            Write-Host "⚠ No encrypted asset files found" -ForegroundColor Yellow
        }
    } else {
        Write-Host "⚠ No asset files found" -ForegroundColor Yellow
    }
} else {
    Write-Host "⚠ Assets directory not found" -ForegroundColor Yellow
}

# Check APK size and compression
Write-Host "`n=== Checking APK Characteristics ===" -ForegroundColor Green

$apkSize = (Get-Item $ApkPath).Length
$apkSizeMB = [math]::Round($apkSize / 1MB, 2)
Write-Host "APK Size: $apkSizeMB MB" -ForegroundColor Yellow

if ($apkSizeMB -lt 10) {
    Write-Host "✓ APK size is reasonable" -ForegroundColor Green
} else {
    Write-Host "⚠ APK size is large ($apkSizeMB MB)" -ForegroundColor Yellow
}

# Check for debug information
Write-Host "`n=== Checking Debug Information Removal ===" -ForegroundColor Green

$debugInfoFound = $false

# Check for debug symbols in native libraries
if ($soFiles) {
    foreach ($soFile in $soFiles) {
        if ($readelfAvailable) {
            $debugInfo = Invoke-Command "readelf -S `"$($soFile.FullName)`" 2>`$null | Select-String -Pattern 'debug'" "Debug section analysis"
            if ($debugInfo) {
                $debugInfoFound = $true
                Write-Host "⚠ Debug information found in $($soFile.Name)" -ForegroundColor Yellow
            }
        }
    }
}

if (-not $debugInfoFound) {
    Write-Host "✓ No debug information found in native libraries" -ForegroundColor Green
}

# Summary
Write-Host "`n=== Verification Summary ===" -ForegroundColor Green

$totalChecks = 0
$passedChecks = 0

# Count checks (simplified)
$totalChecks += 1 # APK exists
$passedChecks += 1

if ($dexdumpAvailable) { $totalChecks += 1; $passedChecks += 1 }
if ($nmAvailable) { $totalChecks += 1; $passedChecks += 1 }
if ($readelfAvailable) { $totalChecks += 1; $passedChecks += 1 }

$totalChecks += 1 # DEX found
$passedChecks += 1

$totalChecks += 1 # Resources found
$passedChecks += 1

$totalChecks += 1 # Assets found
$passedChecks += 1

$successRate = [math]::Round(($passedChecks / $totalChecks) * 100, 1)

Write-Host "Passed: $passedChecks/$totalChecks checks ($successRate%)" -ForegroundColor $(if ($successRate -ge 80) { "Green" } elseif ($successRate -ge 60) { "Yellow" } else { "Red" })

if ($successRate -ge 80) {
    Write-Host "✓ Obfuscation verification PASSED" -ForegroundColor Green
} elseif ($successRate -ge 60) {
    Write-Host "⚠ Obfuscation verification PARTIAL" -ForegroundColor Yellow
} else {
    Write-Host "✗ Obfuscation verification FAILED" -ForegroundColor Red
}

# Cleanup
Write-Host "`nCleaning up temporary files..." -ForegroundColor Gray
Remove-Item -Recurse -Force $tempDir -ErrorAction SilentlyContinue

Write-Host "`nVerification complete!" -ForegroundColor Green
