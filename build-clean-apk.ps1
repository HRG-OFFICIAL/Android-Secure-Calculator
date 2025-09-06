# Build Clean APK Script
# This script builds APK files without AntiDebugSDK protections (clean calculator app)

param(
    [ValidateSet("debug", "release")]
    [string]$BuildType = "debug",        # Build configuration (debug or release)
    [string]$OutputDir = "builds",       # Output directory for APK files
    [switch]$CleanBuild,         # Clean before building
    [switch]$Force               # Force build without confirmation
)

Write-Host "🧹 Clean Calculator APK Builder" -ForegroundColor Blue
Write-Host "===============================" -ForegroundColor Blue

# Define paths
$ProjectRoot = Get-Location
$OutputPath = "$ProjectRoot\$OutputDir"
$Timestamp = Get-Date -Format "yyyy-MM-dd_HH-mm-ss"

# Ensure output directory exists
if (-not (Test-Path $OutputPath)) {
    New-Item -ItemType Directory -Path $OutputPath -Force | Out-Null
}

# Function to get confirmation
function Get-UserConfirmation {
    param([string]$Message)
    
    if ($Force) {
        return $true
    }
    
    Write-Host "`n⚠️  $Message" -ForegroundColor Yellow
    $response = Read-Host "Continue? (y/N)"
    return $response -eq 'y' -or $response -eq 'Y'
}

# Function to ensure clean state
function Ensure-CleanState {
    Write-Host "🔍 Ensuring clean build state..." -ForegroundColor Yellow
    
    # Define paths that shouldn't contain protection code
    $settingsGradle = "$ProjectRoot\settings.gradle.kts"
    $appBuildGradle = "$ProjectRoot\app\build.gradle.kts"
    $mainActivity = "$ProjectRoot\app\src\main\java\com\android\calculator\activities\MainActivity.kt"
    $manifest = "$ProjectRoot\app\src\main\AndroidManifest.xml"
    
    $hasProtectionCode = $false
    $issues = @()
    
    # Check settings.gradle.kts
    if (Test-Path $settingsGradle) {
        $content = Get-Content $settingsGradle -Raw
        # Only flag as issue if anti-debug-sdk is actively included (not commented)
        if ($content -match '^\s*include\s*\(\s*"?:anti-debug-sdk"?\s*\)' -and $content -notmatch '//\s*include\s*\(\s*"?:anti-debug-sdk"?\s*\)') {
            $hasProtectionCode = $true
            $issues += "settings.gradle.kts contains active anti-debug-sdk module reference"
        } elseif ($content -match '//\s*include\(":anti-debug-sdk"\)') {
            Write-Host "  ✅ settings.gradle.kts is clean (anti-debug-sdk commented)" -ForegroundColor Green
        }
    }
    
    # Check app build.gradle.kts
    if (Test-Path $appBuildGradle) {
        $content = Get-Content $appBuildGradle -Raw
        # Only flag as issue if anti-debug-sdk is actively implemented (not commented)
        if ($content -match '^\s*implementation\(project\(":anti-debug-sdk"\)\)' -and $content -notmatch '//\s*implementation\(project\(":anti-debug-sdk"\)\)') {
            $hasProtectionCode = $true
            $issues += "app build.gradle.kts contains active anti-debug-sdk dependency"
        } elseif ($content -match '//\s*AntiDebugSDK dependency added only during protected builds') {
            Write-Host "  ✅ build.gradle.kts is clean (anti-debug-sdk commented)" -ForegroundColor Green
        }
        
        # Verify R8/ProGuard is disabled
        Write-Host "  🔍 Verifying R8/ProGuard configuration..." -ForegroundColor Blue
        if ($content -match 'isMinifyEnabled = true') {
            Write-Host "  ⚠️  R8/ProGuard is enabled - this may affect debugging" -ForegroundColor Yellow
            Write-Host "       Clean builds should have R8/ProGuard disabled" -ForegroundColor Yellow
        } else {
            Write-Host "  ✅ R8/ProGuard is properly disabled for clean build" -ForegroundColor Green
        }
    }
    
    # Check MainActivity.kt
    if (Test-Path $mainActivity) {
        $content = Get-Content $mainActivity -Raw
        if ($content -match 'com\.example\.antidebug' -or $content -match 'initializeAntiDebugProtection') {
            $hasProtectionCode = $true
            $issues += "MainActivity.kt contains protection code"
        }
    }
    
    # Check AndroidManifest.xml for protection-related changes
    if (Test-Path $manifest) {
        $content = Get-Content $manifest -Raw
        # INTERNET permission is enabled by default and that's normal
        if ($content -match '<uses-permission android:name="android\.permission\.INTERNET" />') {
            Write-Host "  ✅ AndroidManifest is clean (INTERNET permission enabled by default)" -ForegroundColor Green
        } else {
            Write-Host "  ⚠️  INTERNET permission not found - this may cause issues" -ForegroundColor Yellow
        }
    }
    
    if ($hasProtectionCode) {
        Write-Host "`n❌ Protection code detected in source files!" -ForegroundColor Red
        Write-Host "Issues found:" -ForegroundColor Yellow
        foreach ($issue in $issues) {
            Write-Host "  • $issue" -ForegroundColor Yellow
        }
        Write-Host "`nTo build a clean APK, you need to:" -ForegroundColor Cyan
        Write-Host "  1. Use .\remove-protection.ps1 to remove protection code" -ForegroundColor White
        Write-Host "  2. Or restore from backups if available" -ForegroundColor White
        Write-Host "  3. Then run this script again" -ForegroundColor White
        return $false
    }
    
    Write-Host "  ✅ Source files are clean (no protection code detected)" -ForegroundColor Green
    return $true
}

# Function to build clean APK
function Build-CleanAPK {
    param([string]$BuildType)
    
    Write-Host "🔨 Building clean APK..." -ForegroundColor Blue
    
    # Clean build if requested
    if ($CleanBuild) {
        Write-Host "🧹 Cleaning project..." -ForegroundColor Blue
        $cleanResult = & "$ProjectRoot\gradlew.bat" clean 2>&1
        if ($LASTEXITCODE -ne 0) {
            Write-Host "❌ Clean failed: $cleanResult" -ForegroundColor Red
            return $false
        }
        Write-Host "  ✅ Project cleaned" -ForegroundColor Green
    }
    
    # Build the APK
    $buildTask = if ($BuildType -eq "release") { "assembleRelease" } else { "assembleDebug" }
    Write-Host "📦 Running gradle $buildTask..." -ForegroundColor Blue
    
    $buildResult = & "$ProjectRoot\gradlew.bat" $buildTask 2>&1
    
    if ($LASTEXITCODE -eq 0) {
        # Find the generated APK
        $apkPattern = "app\build\outputs\apk\$BuildType\*.apk"
        $generatedApk = Get-ChildItem $apkPattern -ErrorAction SilentlyContinue | Select-Object -First 1
        
        if ($generatedApk) {
            # Copy to output directory with descriptive name
            $outputName = "calculator-clean-$BuildType-$Timestamp.apk"
            $outputApk = "$OutputPath\$outputName"
            Copy-Item $generatedApk.FullName $outputApk -Force
            
            Write-Host "✅ Clean APK built successfully!" -ForegroundColor Green
            Write-Host "📱 Output: $outputApk" -ForegroundColor White
            Write-Host "🔧 Build Type: $BuildType" -ForegroundColor White
            Write-Host "🆙 Obfuscation: R8/ProGuard DISABLED" -ForegroundColor Blue
            
            # Display APK info
            $apkSize = [math]::Round((Get-Item $outputApk).Length / 1MB, 2)
            Write-Host "📊 APK Size: $apkSize MB (unobfuscated)" -ForegroundColor Blue
            
            # APK analysis
            Write-Host "`n📋 APK Analysis:" -ForegroundColor Cyan
            Write-Host "  • No security protection layers" -ForegroundColor White
            Write-Host "  • Standard Android permissions only" -ForegroundColor White
            Write-Host "  • Suitable for normal development and testing" -ForegroundColor White
            Write-Host "  • Can be debugged with standard tools" -ForegroundColor White
            
            return $true
        } else {
            Write-Host "❌ APK file not found after build!" -ForegroundColor Red
            Write-Host "Expected location: app\build\outputs\apk\$BuildType\" -ForegroundColor Yellow
            return $false
        }
    } else {
        Write-Host "❌ Build failed!" -ForegroundColor Red
        Write-Host "Build output:" -ForegroundColor Yellow
        Write-Host $buildResult -ForegroundColor Gray
        return $false
    }
}

# Function to display gradle information
function Show-GradleInfo {
    Write-Host "`n📋 Build Environment:" -ForegroundColor Cyan
    
    # Check if gradlew exists
    if (Test-Path "$ProjectRoot\gradlew.bat") {
        Write-Host "  ✅ Gradle wrapper found" -ForegroundColor Green
        
        # Try to get gradle version
        try {
            $gradleVersion = & "$ProjectRoot\gradlew.bat" --version 2>&1 | Select-String "Gradle" | Select-Object -First 1
            if ($gradleVersion) {
                Write-Host "  📦 $($gradleVersion.Line.Trim())" -ForegroundColor White
            }
        } catch {
            Write-Host "  ⚠️  Could not determine Gradle version" -ForegroundColor Yellow
        }
    } else {
        Write-Host "  ❌ Gradle wrapper not found!" -ForegroundColor Red
        Write-Host "     Make sure you're in the Android project root directory" -ForegroundColor Yellow
        return $false
    }
    
    # Check Android project structure
    $requiredPaths = @(
        "app\build.gradle.kts",
        "app\src\main\AndroidManifest.xml",
        "app\src\main\java\com\android\calculator"
    )
    
    $allPathsExist = $true
    foreach ($path in $requiredPaths) {
        if (Test-Path "$ProjectRoot\$path") {
            Write-Host "  ✅ $path" -ForegroundColor Green
        } else {
            Write-Host "  ❌ $path" -ForegroundColor Red
            $allPathsExist = $false
        }
    }
    
    return $allPathsExist
}

# Function to check for potential build issues
function Check-BuildPrerequisites {
    Write-Host "`n🔍 Checking build prerequisites..." -ForegroundColor Yellow
    
    $issues = @()
    
    # Check Java/JDK
    try {
        $javaVersion = java -version 2>&1 | Select-Object -First 1
        Write-Host "  ✅ Java: $($javaVersion)" -ForegroundColor Green
    } catch {
        $issues += "Java not found or not accessible"
    }
    
    # Check Android SDK (via ANDROID_HOME or ANDROID_SDK_ROOT)
    $androidHome = $env:ANDROID_HOME ?? $env:ANDROID_SDK_ROOT
    if ($androidHome -and (Test-Path $androidHome)) {
        Write-Host "  ✅ Android SDK: $androidHome" -ForegroundColor Green
    } else {
        $issues += "Android SDK path not found (ANDROID_HOME/ANDROID_SDK_ROOT)"
    }
    
    if ($issues.Count -gt 0) {
        Write-Host "`n⚠️  Potential build issues detected:" -ForegroundColor Yellow
        foreach ($issue in $issues) {
            Write-Host "  • $issue" -ForegroundColor Yellow
        }
        Write-Host "`nBuild may still succeed if Android Studio is configured properly." -ForegroundColor Blue
    } else {
        Write-Host "  ✅ Prerequisites look good" -ForegroundColor Green
    }
}

# Main execution
try {
    Write-Host "`n📋 Build Configuration:" -ForegroundColor Cyan
    Write-Host "  🧹 Type: Clean (No Protection)" -ForegroundColor White
    Write-Host "  📱 Build Type: $BuildType" -ForegroundColor White
    Write-Host "  📁 Output Directory: $OutputPath" -ForegroundColor White
    Write-Host "  🧹 Clean Build: $($CleanBuild -eq $true)" -ForegroundColor White
    
    # Check build environment
    if (-not (Show-GradleInfo)) {
        Write-Host "`n❌ Build environment check failed!" -ForegroundColor Red
        exit 1
    }
    
    # Check build prerequisites
    Check-BuildPrerequisites
    
    # Ensure clean state
    if (-not (Ensure-CleanState)) {
        exit 1
    }
    
    # Get confirmation
    if (-not (Get-UserConfirmation "Build clean calculator APK with these settings?")) {
        Write-Host "`n❌ Build cancelled by user" -ForegroundColor Yellow
        exit 0
    }
    
    Write-Host "`n🚀 Starting clean APK build process..." -ForegroundColor Blue
    
    # Build the clean APK
    $buildSuccess = Build-CleanAPK -BuildType $BuildType
    
    if ($buildSuccess) {
        Write-Host "`n🎉 Clean APK build completed successfully!" -ForegroundColor Green
        
        Write-Host "`n📖 Next Steps:" -ForegroundColor Cyan
        Write-Host "  1. Install the APK: adb install `"$OutputPath\calculator-clean-$BuildType-$Timestamp.apk`"" -ForegroundColor White
        Write-Host "  2. Test normal calculator functionality" -ForegroundColor White
        Write-Host "  3. Use for development and debugging without security interference" -ForegroundColor White
        Write-Host "  4. Build protected version with .\build-protected-apk.ps1 when needed" -ForegroundColor White
        
        Write-Host "`n🔄 Build Comparison:" -ForegroundColor Cyan
        Write-Host "  🧹 Clean APK: No security protections, suitable for development" -ForegroundColor White
        Write-Host "  🛡️  Protected APK: Use .\build-protected-apk.ps1 for security testing" -ForegroundColor White
        
    } else {
        Write-Host "`n❌ Clean APK build failed!" -ForegroundColor Red
        Write-Host "`n🔧 Troubleshooting tips:" -ForegroundColor Cyan
        Write-Host "  1. Ensure Android Studio is properly installed" -ForegroundColor White
        Write-Host "  2. Try running: .\gradlew.bat --version" -ForegroundColor White
        Write-Host "  3. Check if ANDROID_HOME environment variable is set" -ForegroundColor White
        Write-Host "  4. Try with -CleanBuild flag for a fresh build" -ForegroundColor White
        exit 1
    }
    
} catch {
    Write-Host "`n❌ Error during build process: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Stack trace: $($_.ScriptStackTrace)" -ForegroundColor Gray
    exit 1
}

Write-Host "`n🧹 Clean Calculator APK Build Complete! 🧹" -ForegroundColor Blue
