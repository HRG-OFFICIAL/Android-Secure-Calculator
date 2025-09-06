# Build Protected APK Script
# This script builds APK files with AntiDebugSDK security protections

param(
    [switch]$Minimal,            # Apply minimal protection (basic checks)
    [switch]$Full,               # Apply full protection (all checks)
    [switch]$Debug,              # Apply debug-level protection (detailed logging)
    [switch]$Production,         # Apply production-level protection (aggressive)
    [ValidateSet("LOG_ONLY", "CRASH_APP", "KILL_PROCESS", "FAKE_UI", "CORRUPT_DATA")]
    [string]$ResponseType = "LOG_ONLY",  # Response when threats detected
    [ValidateSet("debug", "release")]
    [string]$BuildType = "debug",        # Build configuration (debug or release)
    [string]$OutputDir = "builds",       # Output directory for APK files
    [switch]$CleanBuild,         # Clean before building
    [switch]$Force               # Force build without confirmation
)

Write-Host "🛡️ AntiDebugSDK Protected APK Builder" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green

# Define paths
$ProjectRoot = Get-Location
$AppBuildGradle = "$ProjectRoot\app\build.gradle.kts"
$AppManifest = "$ProjectRoot\app\src\main\AndroidManifest.xml"
$MainActivity = "$ProjectRoot\app\src\main\java\com\android\calculator\activities\MainActivity.kt"
$SettingsGradle = "$ProjectRoot\settings.gradle.kts"
$BackupDir = "$ProjectRoot\.build-backups"
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

# Function to backup original files
function Backup-OriginalFiles {
    Write-Host "📋 Creating backup of original files..." -ForegroundColor Yellow
    
    if (-not (Test-Path $BackupDir)) {
        New-Item -ItemType Directory -Path $BackupDir -Force | Out-Null
    }
    
    $filesToBackup = @{
        $AppBuildGradle = "$BackupDir\build.gradle.kts.original"
        $AppManifest = "$BackupDir\AndroidManifest.xml.original"
        $MainActivity = "$BackupDir\MainActivity.kt.original"
        $SettingsGradle = "$BackupDir\settings.gradle.kts.original"
    }
    
    foreach ($source in $filesToBackup.Keys) {
        $destination = $filesToBackup[$source]
        if ((Test-Path $source) -and -not (Test-Path $destination)) {
            Copy-Item $source $destination -Force
            Write-Host "  ✅ Backed up $(Split-Path $source -Leaf)" -ForegroundColor Green
        }
    }
}

# Function to restore from backup
function Restore-FromBackup {
    Write-Host "🔄 Restoring original files..." -ForegroundColor Yellow
    
    $filesToRestore = @{
        "$BackupDir\build.gradle.kts.original" = $AppBuildGradle
        "$BackupDir\AndroidManifest.xml.original" = $AppManifest
        "$BackupDir\MainActivity.kt.original" = $MainActivity
        "$BackupDir\settings.gradle.kts.original" = $SettingsGradle
    }
    
    foreach ($backup in $filesToRestore.Keys) {
        $target = $filesToRestore[$backup]
        if (Test-Path $backup) {
            Copy-Item $backup $target -Force
            Write-Host "  ✅ Restored $(Split-Path $target -Leaf)" -ForegroundColor Green
        }
    }
}

# Function to determine protection level
function Get-ProtectionLevel {
    if ($Production) { return "production" }
    if ($Full) { return "full" }
    if ($Debug) { return "debug" }
    if ($Minimal) { return "minimal" }
    return "minimal"  # default
}

# Function to update settings.gradle.kts
function Update-SettingsGradle {
    Write-Host "🔧 Updating settings.gradle.kts..." -ForegroundColor Yellow
    
    if (Test-Path $SettingsGradle) {
        $content = Get-Content $SettingsGradle -Raw
        
        # Check if anti-debug-sdk module is commented out and uncomment it
        if ($content -match '// include\(":anti-debug-sdk"\)\s*//\s*Added only during protected builds') {
            $content = $content -replace '// include\(":anti-debug-sdk"\)\s*//\s*Added only during protected builds', 'include(":anti-debug-sdk")  // Enabled by protected build script'
            Set-Content $SettingsGradle $content -NoNewline
            Write-Host "  ✅ Enabled anti-debug-sdk module for protected build" -ForegroundColor Green
        } elseif ($content -notmatch 'include\s*\(\s*"?:anti-debug-sdk"?\s*\)') {
            # Add the module if not present at all
            $content = $content + "`ninclude("":anti-debug-sdk"")  // Added by protected build script"
            Set-Content $SettingsGradle $content -NoNewline
            Write-Host "  ✅ Added anti-debug-sdk module" -ForegroundColor Green
        } else {
            Write-Host "  ℹ️  anti-debug-sdk module already present" -ForegroundColor Blue
        }
    }
}

# Function to update app build.gradle.kts
function Update-AppBuildGradle {
    Write-Host "🔧 Updating app build.gradle.kts..." -ForegroundColor Yellow
    
    if (Test-Path $AppBuildGradle) {
        $content = Get-Content $AppBuildGradle -Raw
        
        # Enable R8/ProGuard obfuscation for protected builds
        Write-Host "  🔒 Enabling R8/ProGuard obfuscation..." -ForegroundColor Blue
        
        # Enable minification for release builds
        $content = $content -replace '(release\s*\{[^}]*?)isMinifyEnabled = false', '$1isMinifyEnabled = true'
        $content = $content -replace '(release\s*\{[^}]*?)isShrinkResources = false', '$1isShrinkResources = true'
        
        # Enable minification for debug builds too (for protected builds)
        $content = $content -replace '(debug\s*\{[^}]*?)isMinifyEnabled = false', '$1isMinifyEnabled = true'
        
        Write-Host "  ✅ Enabled R8/ProGuard obfuscation" -ForegroundColor Green
        
        # Check if SDK dependency is commented out and uncomment it, or add it
        if ($content -match '//\s*AntiDebugSDK dependency added only during protected builds') {
            # Add the dependency after the comment
            $content = $content -replace '(//\s*AntiDebugSDK dependency added only during protected builds)', "`$1`n    implementation(project("":anti-debug-sdk"))  // Added by protected build script"
            Write-Host "  ✅ Added AntiDebugSDK dependency" -ForegroundColor Green
        } elseif ($content -notmatch 'implementation\(project\(":anti-debug-sdk"\)\)') {
            # Find the dependencies block and add the SDK if not present at all
            $dependenciesPattern = '(dependencies\s*\{)'
            if ($content -match $dependenciesPattern) {
                $content = $content -replace $dependenciesPattern, "`$1`n    implementation(project("":anti-debug-sdk""))  // Added by protected build script"
                Write-Host "  ✅ Added AntiDebugSDK dependency" -ForegroundColor Green
            }
        } else {
            Write-Host "  ℹ️  AntiDebugSDK dependency already present" -ForegroundColor Blue
        }
        
        Set-Content $AppBuildGradle $content -NoNewline
    }
}

# Function to update AndroidManifest.xml
function Update-AndroidManifest {
    Write-Host "🔧 Checking AndroidManifest.xml..." -ForegroundColor Yellow
    
    if (Test-Path $AppManifest) {
        $content = Get-Content $AppManifest -Raw
        
        # Verify INTERNET permission is present (should be enabled by default)
        if ($content -match '<uses-permission android:name="android\.permission\.INTERNET" />') {
            Write-Host "  ✅ INTERNET permission is already enabled" -ForegroundColor Green
        } else {
            Write-Host "  ⚠️  INTERNET permission not found in AndroidManifest.xml" -ForegroundColor Yellow
            Write-Host "       AntiDebugSDK may require INTERNET permission for some features" -ForegroundColor Yellow
        }
    }
}

# Function to inject protection code into MainActivity.kt
function Inject-ProtectionCode {
    param([string]$Level, [string]$Response)
    
    Write-Host "🔧 Injecting protection code into MainActivity.kt..." -ForegroundColor Yellow
    
    if (Test-Path $MainActivity) {
        $content = Get-Content $MainActivity -Raw
        
        # Check if protection code is already injected
        if ($content -match 'initializeAntiDebugProtection') {
            Write-Host "  ℹ️  Protection code already present" -ForegroundColor Blue
            return
        }
        
        # Add imports
        $imports = @"
import com.example.antidebug.AntiDebug
import com.example.antidebug.DebuggerDetection
import com.example.antidebug.RootDetection
import com.example.antidebug.EmulatorDetection
import com.example.antidebug.TamperDetection
import com.example.antidebug.HookDetection
import com.example.antidebug.BehavioralDetection
import com.example.antidebug.ResponseHandler
import com.example.antidebug.DataProtection
"@
        
        # Find the import section and add our imports
        $importPattern = '(import\s+[^\n]+\n)*'
        if ($content -match $importPattern) {
            $content = $content -replace $importPattern, "$&$imports`n"
        }
        
        # Add initialization call in onCreate
        $onCreatePattern = '(override fun onCreate\(savedInstanceState: Bundle\?\) \{[^}]*super\.onCreate\(savedInstanceState\))'
        if ($content -match $onCreatePattern) {
            $content = $content -replace $onCreatePattern, "`$1`n`n        // Initialize AntiDebugSDK Protection (Generated)`n        initializeAntiDebugProtection()"
        }
        
        # Generate protection methods based on level and response type
        $protectionMethods = Generate-ProtectionMethods -Level $Level -Response $Response
        
        # Add protection methods at the end of the class (before the last closing brace)
        $content = $content -replace '(\n\s*}\s*)$', "$protectionMethods`n`$1"
        
        Set-Content $MainActivity $content -NoNewline
        Write-Host "  ✅ Injected protection code (Level: $Level, Response: $Response)" -ForegroundColor Green
    }
}

# Function to generate protection methods based on level
function Generate-ProtectionMethods {
    param([string]$Level, [string]$Response)
    
    $methods = @"

    /**
     * Initialize and configure AntiDebugSDK protections
     * Protection Level: $Level
     * Response Type: $Response
     */
    private fun initializeAntiDebugProtection() {
        try {
            // Initialize the main AntiDebug SDK
            AntiDebug.initialize(this)
            
            // Configure response handler
            ResponseHandler.setResponseType(ResponseHandler.ResponseType.$Response)
            
            // Setup data protection
            setupDataProtection()
            
            // Perform security checks based on level
"@

    # Add checks based on protection level
    switch ($Level) {
        "minimal" {
            $methods += @"

            performDebuggerCheck()
            performRootCheck()
"@
        }
        "debug" {
            $methods += @"

            performDebuggerCheck()
            performRootCheck()
            performEmulatorCheck()
            performTamperCheck()
"@
        }
        "full" {
            $methods += @"

            performDebuggerCheck()
            performRootCheck()
            performEmulatorCheck()
            performTamperCheck()
            performHookCheck()
            performBehavioralCheck()
"@
        }
        "production" {
            $methods += @"

            // Comprehensive production-level checks
            performDebuggerCheck()
            performRootCheck()
            performEmulatorCheck()
            performTamperCheck()
            performHookCheck()
            performBehavioralCheck()
            
            // Enable continuous monitoring
            AntiDebug.enableContinuousMonitoring(true)
"@
        }
    }

    $methods += @"

            
        } catch (e: Exception) {
            ResponseHandler.handleThreatDetected("Protection initialization failed: `${e.message}", this)
        }
    }

    private fun performDebuggerCheck() {
        if (DebuggerDetection.isDebuggerConnected(this)) {
            ResponseHandler.handleThreatDetected("Debugger detected", this)
        }
    }

    private fun performRootCheck() {
        if (RootDetection.isDeviceRooted(this)) {
            ResponseHandler.handleThreatDetected("Root access detected", this)
        }
    }

    private fun performEmulatorCheck() {
        if (EmulatorDetection.isRunningOnEmulator(this)) {
            ResponseHandler.handleThreatDetected("Emulator detected", this)
        }
    }

    private fun performTamperCheck() {
        if (TamperDetection.isTampered(this)) {
            ResponseHandler.handleThreatDetected("App tampering detected", this)
        }
    }

    private fun performHookCheck() {
        if (HookDetection.isHooked(this)) {
            ResponseHandler.handleThreatDetected("Hooking framework detected", this)
        }
    }

    private fun performBehavioralCheck() {
        if (BehavioralDetection.isAnomalousActivity(this)) {
            ResponseHandler.handleThreatDetected("Suspicious behavior detected", this)
        }
    }

    private fun setupDataProtection() {
        DataProtection.initialize(this)
        DataProtection.enableMemoryProtection(true)
    }
"@

    return $methods
}

# Function to build APK
function Build-ProtectedAPK {
    param([string]$BuildType, [string]$Level)
    
    Write-Host "🔨 Building protected APK..." -ForegroundColor Yellow
    
    # Clean build if requested
    if ($CleanBuild) {
        Write-Host "🧹 Cleaning project..." -ForegroundColor Blue
        $cleanResult = & "$ProjectRoot\gradlew.bat" clean 2>&1
        if ($LASTEXITCODE -ne 0) {
            Write-Host "❌ Clean failed: $cleanResult" -ForegroundColor Red
            return $false
        }
    }
    
    # Build the APK
    $buildTask = if ($BuildType -eq "release") { "assembleRelease" } else { "assembleDebug" }
    Write-Host "📦 Running gradle $buildTask..." -ForegroundColor Blue
    
    $buildResult = & "$ProjectRoot\gradlew.bat" $buildTask 2>&1
    
    if ($LASTEXITCODE -eq 0) {
        # Find the generated APK
        $apkPattern = "app\build\outputs\apk\$BuildType\*.apk"
        $generatedApk = Get-ChildItem $apkPattern | Select-Object -First 1
        
        if ($generatedApk) {
            # Copy to output directory with descriptive name
            $outputName = "calculator-protected-$Level-$BuildType-$Timestamp.apk"
            $outputApk = "$OutputPath\$outputName"
            Copy-Item $generatedApk.FullName $outputApk -Force
            
            Write-Host "✅ Protected APK built successfully!" -ForegroundColor Green
            Write-Host "📱 Output: $outputApk" -ForegroundColor White
            Write-Host "🛡️  Protection Level: $Level" -ForegroundColor White
            Write-Host "🎯 Response Type: $ResponseType" -ForegroundColor White
            Write-Host "🔒 Obfuscation: R8/ProGuard ENABLED" -ForegroundColor Green
            
            # Display APK info
            $apkSize = [math]::Round((Get-Item $outputApk).Length / 1MB, 2)
            Write-Host "📊 APK Size: $apkSize MB (obfuscated)" -ForegroundColor Blue
            
            return $true
        } else {
            Write-Host "❌ APK file not found after build!" -ForegroundColor Red
            return $false
        }
    } else {
        Write-Host "❌ Build failed: $buildResult" -ForegroundColor Red
        return $false
    }
}

# Main execution
try {
    # Determine protection level
    $protectionLevel = Get-ProtectionLevel
    
    Write-Host "`n📋 Build Configuration:" -ForegroundColor Cyan
    Write-Host "  🛡️  Protection Level: $protectionLevel" -ForegroundColor White
    Write-Host "  📱 Build Type: $BuildType" -ForegroundColor White
    Write-Host "  🎯 Response Type: $ResponseType" -ForegroundColor White
    Write-Host "  📁 Output Directory: $OutputPath" -ForegroundColor White
    
    # Get confirmation
    if (-not (Get-UserConfirmation "Build protected APK with these settings?")) {
        Write-Host "`n❌ Build cancelled by user" -ForegroundColor Yellow
        exit 0
    }
    
    Write-Host "`n🚀 Starting protected APK build process..." -ForegroundColor Yellow
    
    # Step 1: Backup original files
    Backup-OriginalFiles
    
    # Step 2: Apply temporary protection modifications
    Write-Host "`n🔧 Applying temporary protection modifications..." -ForegroundColor Yellow
    Update-SettingsGradle
    Update-AppBuildGradle
    Update-AndroidManifest
    Inject-ProtectionCode -Level $protectionLevel -Response $ResponseType
    
    # Step 3: Build the protected APK
    Write-Host "`n📦 Building protected APK..." -ForegroundColor Yellow
    $buildSuccess = Build-ProtectedAPK -BuildType $BuildType -Level $protectionLevel
    
    # Step 4: Restore original files
    Write-Host "`n🔄 Restoring original files..." -ForegroundColor Yellow
    Restore-FromBackup
    
    if ($buildSuccess) {
        Write-Host "`n🎉 Protected APK build completed successfully!" -ForegroundColor Green
        Write-Host "`n📖 Next Steps:" -ForegroundColor Cyan
        Write-Host "  1. Install the APK: adb install `"$OutputPath\calculator-protected-$protectionLevel-$BuildType-$Timestamp.apk`"" -ForegroundColor White
        Write-Host "  2. Test with debugging tools to verify protection" -ForegroundColor White
        Write-Host "  3. Check logcat for protection events: adb logcat | grep AntiDebug" -ForegroundColor White
    } else {
        Write-Host "`n❌ Protected APK build failed!" -ForegroundColor Red
        Write-Host "🔄 Original files have been restored" -ForegroundColor Blue
        exit 1
    }
    
} catch {
    Write-Host "`n❌ Error during build process: $($_.Exception.Message)" -ForegroundColor Red
    
    # Ensure we restore files even if there's an error
    Write-Host "🔄 Attempting to restore original files..." -ForegroundColor Yellow
    Restore-FromBackup
    
    exit 1
}

Write-Host "`n🛡️ AntiDebugSDK Protected APK Build Complete! 🛡️" -ForegroundColor Green
