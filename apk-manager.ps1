# AntiDebugSDK APK Manager
# Central script to build and manage APK files with or without security protections

param(
    [ValidateSet("build-protected", "build-clean", "status", "help", "clean-builds")]
    [string]$Action = "help",
    [ValidateSet("minimal", "full", "debug", "production")]
    [string]$Level = "minimal",
    [ValidateSet("LOG_ONLY", "CRASH_APP", "KILL_PROCESS", "FAKE_UI", "CORRUPT_DATA")]
    [string]$ResponseType = "LOG_ONLY",
    [ValidateSet("debug", "release")]
    [string]$BuildType = "debug",
    [string]$OutputDir = "builds",
    [switch]$CleanBuild,
    [switch]$Force
)

Write-Host "🏗️ AntiDebugSDK APK Manager" -ForegroundColor Magenta
Write-Host "============================" -ForegroundColor Magenta

# Define paths
$ProjectRoot = Get-Location
$OutputPath = "$ProjectRoot\$OutputDir"
$ProtectedBuilder = "$ProjectRoot\build-protected-apk.ps1"
$CleanBuilder = "$ProjectRoot\build-clean-apk.ps1"
$RemoveProtection = "$ProjectRoot\remove-protection.ps1"

# Function to show current status
function Show-Status {
    Write-Host "`n📊 Current Project Status:" -ForegroundColor Cyan
    
    # Check if source has protection code
    $hasProtection = $false
    $protectionFiles = @(
        "$ProjectRoot\settings.gradle.kts",
        "$ProjectRoot\app\build.gradle.kts",
        "$ProjectRoot\app\src\main\java\com\android\calculator\activities\MainActivity.kt"
    )
    
    foreach ($file in $protectionFiles) {
        if (Test-Path $file) {
            $content = Get-Content $file -Raw
            if ($content -match 'anti-debug-sdk|initializeAntiDebugProtection|com\.example\.antidebug') {
                $hasProtection = $true
                break
            }
        }
    }
    
    if ($hasProtection) {
        Write-Host "  🛡️  Source State: PROTECTED (contains AntiDebugSDK code)" -ForegroundColor Yellow
        Write-Host "  ⚠️  You may want to remove protection before building clean APK" -ForegroundColor Yellow
    } else {
        Write-Host "  🧹 Source State: CLEAN (no protection code)" -ForegroundColor Green
    }
    
    # Check build scripts
    if (Test-Path $ProtectedBuilder) {
        Write-Host "  ✅ Protected APK builder: Available" -ForegroundColor Green
    } else {
        Write-Host "  ❌ Protected APK builder: Missing" -ForegroundColor Red
    }
    
    if (Test-Path $CleanBuilder) {
        Write-Host "  ✅ Clean APK builder: Available" -ForegroundColor Green
    } else {
        Write-Host "  ❌ Clean APK builder: Missing" -ForegroundColor Red
    }
    
    # Check output directory and existing APKs
    if (Test-Path $OutputPath) {
        $apkFiles = Get-ChildItem "$OutputPath\*.apk" -ErrorAction SilentlyContinue
        if ($apkFiles.Count -gt 0) {
            Write-Host "  📦 Built APKs: $($apkFiles.Count) files" -ForegroundColor Blue
            
            # Group APKs by type
            $cleanApks = $apkFiles | Where-Object { $_.Name -match "clean" }
            $protectedApks = $apkFiles | Where-Object { $_.Name -match "protected" }
            
            if ($cleanApks.Count -gt 0) {
                Write-Host "    🧹 Clean APKs: $($cleanApks.Count)" -ForegroundColor Blue
                $latestClean = $cleanApks | Sort-Object LastWriteTime -Descending | Select-Object -First 1
                Write-Host "      Latest: $($latestClean.Name) ($(Get-Date $latestClean.LastWriteTime -Format 'MMM dd, HH:mm'))" -ForegroundColor Gray
            }
            
            if ($protectedApks.Count -gt 0) {
                Write-Host "    🛡️  Protected APKs: $($protectedApks.Count)" -ForegroundColor Blue
                $latestProtected = $protectedApks | Sort-Object LastWriteTime -Descending | Select-Object -First 1
                Write-Host "      Latest: $($latestProtected.Name) ($(Get-Date $latestProtected.LastWriteTime -Format 'MMM dd, HH:mm'))" -ForegroundColor Gray
            }
        } else {
            Write-Host "  📦 Built APKs: None" -ForegroundColor Gray
        }
    } else {
        Write-Host "  📦 Build Output: Directory not created" -ForegroundColor Gray
    }
    
    # Check backups
    $backupDir = "$ProjectRoot\.build-backups"
    if (Test-Path $backupDir) {
        $backupCount = (Get-ChildItem $backupDir -File).Count
        Write-Host "  💾 Build Backups: $backupCount files" -ForegroundColor Blue
    } else {
        Write-Host "  💾 Build Backups: None" -ForegroundColor Gray
    }
    
    Write-Host "`n📖 Usage:" -ForegroundColor Cyan
    Write-Host "  .\apk-manager.ps1 build-clean     # Build clean calculator APK" -ForegroundColor White
    Write-Host "  .\apk-manager.ps1 build-protected -Level full -ResponseType CRASH_APP" -ForegroundColor White
    Write-Host "  .\apk-manager.ps1 status          # Show this status" -ForegroundColor White
}

# Function to build protected APK
function Build-ProtectedAPK {
    Write-Host "`n🛡️ Building Protected APK..." -ForegroundColor Green
    Write-Host "Configuration:" -ForegroundColor Cyan
    Write-Host "  • Protection Level: $Level" -ForegroundColor White
    Write-Host "  • Response Type: $ResponseType" -ForegroundColor White
    Write-Host "  • Build Type: $BuildType" -ForegroundColor White
    Write-Host "  • Output Directory: $OutputDir" -ForegroundColor White
    
    if (-not (Test-Path $ProtectedBuilder)) {
        Write-Host "`n❌ Protected APK builder script not found!" -ForegroundColor Red
        Write-Host "Expected: $ProtectedBuilder" -ForegroundColor Yellow
        return $false
    }
    
    # Build argument list
    $buildArgs = @()
    
    # Add protection level
    switch ($Level) {
        "minimal" { $buildArgs += "-Minimal" }
        "full" { $buildArgs += "-Full" }
        "debug" { $buildArgs += "-Debug" }
        "production" { $buildArgs += "-Production" }
    }
    
    # Add other parameters
    $buildArgs += "-ResponseType", $ResponseType
    $buildArgs += "-BuildType", $BuildType
    $buildArgs += "-OutputDir", $OutputDir
    
    if ($CleanBuild) { $buildArgs += "-CleanBuild" }
    if ($Force) { $buildArgs += "-Force" }
    
    # Execute the build script
    Write-Host "`n🚀 Executing: $ProtectedBuilder $($buildArgs -join ' ')" -ForegroundColor Yellow
    
    try {
        & $ProtectedBuilder @buildArgs
        return $LASTEXITCODE -eq 0
    } catch {
        Write-Host "`n❌ Error executing protected build: $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

# Function to build clean APK
function Build-CleanAPK {
    Write-Host "`n🧹 Building Clean APK..." -ForegroundColor Blue
    Write-Host "Configuration:" -ForegroundColor Cyan
    Write-Host "  • Type: Clean (No Protection)" -ForegroundColor White
    Write-Host "  • Build Type: $BuildType" -ForegroundColor White
    Write-Host "  • Output Directory: $OutputDir" -ForegroundColor White
    
    if (-not (Test-Path $CleanBuilder)) {
        Write-Host "`n❌ Clean APK builder script not found!" -ForegroundColor Red
        Write-Host "Expected: $CleanBuilder" -ForegroundColor Yellow
        return $false
    }
    
    # Build argument list
    $buildArgs = @()
    $buildArgs += "-BuildType", $BuildType
    $buildArgs += "-OutputDir", $OutputDir
    
    if ($CleanBuild) { $buildArgs += "-CleanBuild" }
    if ($Force) { $buildArgs += "-Force" }
    
    # Execute the build script
    Write-Host "`n🚀 Executing: $CleanBuilder $($buildArgs -join ' ')" -ForegroundColor Yellow
    
    try {
        & $CleanBuilder @buildArgs
        return $LASTEXITCODE -eq 0
    } catch {
        Write-Host "`n❌ Error executing clean build: $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

# Function to clean build directory
function Clean-BuildDirectory {
    Write-Host "`n🧹 Cleaning build directory..." -ForegroundColor Yellow
    
    if (Test-Path $OutputPath) {
        $apkFiles = Get-ChildItem "$OutputPath\*.apk" -ErrorAction SilentlyContinue
        if ($apkFiles.Count -gt 0) {
            Write-Host "Found $($apkFiles.Count) APK files to remove:" -ForegroundColor Blue
            foreach ($apk in $apkFiles) {
                Write-Host "  • $($apk.Name)" -ForegroundColor Gray
            }
            
            if (-not $Force) {
                $response = Read-Host "`nDelete all APK files? (y/N)"
                if ($response -ne 'y' -and $response -ne 'Y') {
                    Write-Host "❌ Cleanup cancelled" -ForegroundColor Yellow
                    return
                }
            }
            
            Remove-Item "$OutputPath\*.apk" -Force
            Write-Host "✅ Deleted $($apkFiles.Count) APK files" -ForegroundColor Green
        } else {
            Write-Host "📁 No APK files found to clean" -ForegroundColor Blue
        }
    } else {
        Write-Host "📁 Build directory doesn't exist" -ForegroundColor Gray
    }
}

# Function to show help
function Show-Help {
    Write-Host "`n📖 AntiDebugSDK APK Manager Help" -ForegroundColor Cyan
    Write-Host "=====================================`n" -ForegroundColor Cyan
    
    Write-Host "🎯 Purpose:" -ForegroundColor Yellow
    Write-Host "  Build APK files with or without AntiDebugSDK security protections`n" -ForegroundColor White
    Write-Host "  • Protected APKs: Include AntiDebugSDK + R8/ProGuard obfuscation" -ForegroundColor White
    Write-Host "  • Clean APKs: No protection, R8/ProGuard disabled for debugging`n" -ForegroundColor White
    
    Write-Host "📋 Commands:" -ForegroundColor Yellow
    Write-Host "  build-protected    Build APK with security protections" -ForegroundColor White
    Write-Host "  build-clean        Build APK without any protections" -ForegroundColor White
    Write-Host "  status             Show current project status" -ForegroundColor White
    Write-Host "  clean-builds       Remove all built APK files" -ForegroundColor White
    Write-Host "  help               Show this help message`n" -ForegroundColor White
    
    Write-Host "🛡️ Protection Levels (for build-protected):" -ForegroundColor Yellow
    Write-Host "  minimal            Basic checks (debugger, root)" -ForegroundColor White
    Write-Host "  debug              Standard checks + emulator/tamper detection" -ForegroundColor White
    Write-Host "  full               All checks including hooks and behavioral analysis" -ForegroundColor White
    Write-Host "  production         Full protection + continuous monitoring`n" -ForegroundColor White
    
    Write-Host "🎯 Response Types (for build-protected):" -ForegroundColor Yellow
    Write-Host "  LOG_ONLY           Log threats without taking action (default)" -ForegroundColor White
    Write-Host "  CRASH_APP          Crash the application when threat detected" -ForegroundColor White
    Write-Host "  KILL_PROCESS       Kill the application process" -ForegroundColor White
    Write-Host "  FAKE_UI            Display fake UI to confuse attackers" -ForegroundColor White
    Write-Host "  CORRUPT_DATA       Corrupt application data`n" -ForegroundColor White
    
    Write-Host "📱 Build Types:" -ForegroundColor Yellow
    Write-Host "  debug              Debug build (default, faster builds)" -ForegroundColor White
    Write-Host "  release            Release build (optimized, requires signing)`n" -ForegroundColor White
    
    Write-Host "🔧 Common Options:" -ForegroundColor Yellow
    Write-Host "  -CleanBuild        Clean project before building" -ForegroundColor White
    Write-Host "  -Force             Skip confirmations" -ForegroundColor White
    Write-Host "  -OutputDir <path>  Custom output directory (default: builds)`n" -ForegroundColor White
    
    Write-Host "💡 Usage Examples:" -ForegroundColor Yellow
    Write-Host "  # Build clean APK for development" -ForegroundColor Green
    Write-Host "  .\apk-manager.ps1 build-clean`n" -ForegroundColor White
    
    Write-Host "  # Build protected APK with full security" -ForegroundColor Green
    Write-Host "  .\apk-manager.ps1 build-protected -Level full -ResponseType CRASH_APP`n" -ForegroundColor White
    
    Write-Host "  # Build production release with maximum protection" -ForegroundColor Green
    Write-Host "  .\apk-manager.ps1 build-protected -Level production -BuildType release -ResponseType KILL_PROCESS`n" -ForegroundColor White
    
    Write-Host "  # Quick debug build with minimal protection" -ForegroundColor Green
    Write-Host "  .\apk-manager.ps1 build-protected -Level minimal -Force`n" -ForegroundColor White
    
    Write-Host "  # Clean all previously built APKs" -ForegroundColor Green
    Write-Host "  .\apk-manager.ps1 clean-builds -Force`n" -ForegroundColor White
    
    Write-Host "🔄 Typical Workflow:" -ForegroundColor Yellow
    Write-Host "  1. Check status: .\apk-manager.ps1 status" -ForegroundColor White
    Write-Host "  2. Build clean APK for testing: .\apk-manager.ps1 build-clean" -ForegroundColor White
    Write-Host "  3. Build protected APK for security testing: .\apk-manager.ps1 build-protected -Level full" -ForegroundColor White
    Write-Host "  4. Install and test both versions" -ForegroundColor White
    Write-Host "  5. Clean builds when needed: .\apk-manager.ps1 clean-builds`n" -ForegroundColor White
    
    Write-Host "📁 File Structure:" -ForegroundColor Yellow
    Write-Host "  builds/                           # Output directory" -ForegroundColor White
    Write-Host "  ├── calculator-clean-debug-*.apk         # Clean APKs" -ForegroundColor White
    Write-Host "  └── calculator-protected-*-debug-*.apk   # Protected APKs`n" -ForegroundColor White
    
    Write-Host "🔧 Troubleshooting:" -ForegroundColor Yellow
    Write-Host "  • Ensure Android Studio is installed and configured" -ForegroundColor White
    Write-Host "  • Set ANDROID_HOME environment variable" -ForegroundColor White
    Write-Host "  • Run from the project root directory" -ForegroundColor White
    Write-Host "  • Use -CleanBuild if you encounter build issues" -ForegroundColor White
}

# Main execution
try {
    switch ($Action.ToLower()) {
        "status" {
            Show-Status
        }
        "build-protected" {
            $success = Build-ProtectedAPK
            if ($success) {
                Write-Host "`n🎉 Protected APK build completed!" -ForegroundColor Green
            } else {
                Write-Host "`n❌ Protected APK build failed!" -ForegroundColor Red
                exit 1
            }
        }
        "build-clean" {
            $success = Build-CleanAPK
            if ($success) {
                Write-Host "`n🎉 Clean APK build completed!" -ForegroundColor Green
            } else {
                Write-Host "`n❌ Clean APK build failed!" -ForegroundColor Red
                exit 1
            }
        }
        "clean-builds" {
            Clean-BuildDirectory
        }
        "help" {
            Show-Help
        }
        default {
            Write-Host "`n❌ Unknown action: $Action" -ForegroundColor Red
            Write-Host "Use 'help' to see available commands" -ForegroundColor Yellow
            exit 1
        }
    }
} catch {
    Write-Host "`n❌ Error: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Stack trace: $($_.ScriptStackTrace)" -ForegroundColor Gray
    exit 1
}

Write-Host "`n🏗️ APK Manager Complete! 🏗️" -ForegroundColor Magenta
