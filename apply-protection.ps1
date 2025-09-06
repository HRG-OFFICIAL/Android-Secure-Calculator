# Apply AntiDebugSDK Protection Script
# This script intelligently applies security protections to the calculator app

param(
    [switch]$Full,              # Apply all protections
    [switch]$Minimal,           # Apply only basic protections
    [switch]$Debug,             # Apply with debug-friendly settings
    [switch]$Production,        # Apply with production settings
    [string]$ResponseType = "LOG_ONLY"  # Default response type
)

Write-Host "🛡️ AntiDebugSDK Protection Application Script" -ForegroundColor Cyan
Write-Host "=================================================" -ForegroundColor Cyan

# Define paths
$ProjectRoot = Get-Location
$AppBuildGradle = "$ProjectRoot\app\build.gradle.kts"
$AppManifest = "$ProjectRoot\app\src\main\AndroidManifest.xml"
$MainActivity = "$ProjectRoot\app\src\main\java\com\android\calculator\activities\MainActivity.kt"
$SettingsGradle = "$ProjectRoot\settings.gradle.kts"

# Backup original files
$BackupDir = "$ProjectRoot\.protection-backups"
if (-not (Test-Path $BackupDir)) {
    New-Item -ItemType Directory -Path $BackupDir | Out-Null
}

Write-Host "📦 Creating backups..." -ForegroundColor Yellow

# Create backups with timestamp
$Timestamp = Get-Date -Format "yyyy-MM-dd_HH-mm-ss"
Copy-Item $AppBuildGradle "$BackupDir\build.gradle.kts.$Timestamp" -Force
Copy-Item $AppManifest "$BackupDir\AndroidManifest.xml.$Timestamp" -Force
Copy-Item $MainActivity "$BackupDir\MainActivity.kt.$Timestamp" -Force
Copy-Item $SettingsGradle "$BackupDir\settings.gradle.kts.$Timestamp" -Force

Write-Host "✅ Backups created in .protection-backups/" -ForegroundColor Green

# Function to update settings.gradle.kts
function Update-SettingsGradle {
    Write-Host "🔧 Updating settings.gradle.kts..." -ForegroundColor Yellow
    
    $content = Get-Content $SettingsGradle -Raw
    if ($content -notmatch 'include\(":anti-debug-sdk"\)') {
        $content = $content -replace 'include\(":app"\)', 'include(":app")' + "`ninclude(`":anti-debug-sdk`")"
        Set-Content $SettingsGradle $content -NoNewline
        Write-Host "✅ Added anti-debug-sdk module to settings.gradle.kts" -ForegroundColor Green
    } else {
        Write-Host "ℹ️  anti-debug-sdk module already present in settings.gradle.kts" -ForegroundColor Blue
    }
}

# Function to update app build.gradle.kts
function Update-AppBuildGradle {
    Write-Host "🔧 Updating app build.gradle.kts..." -ForegroundColor Yellow
    
    $content = Get-Content $AppBuildGradle -Raw
    
    # Add SDK dependency if not present
    if ($content -notmatch 'implementation\(project\(":anti-debug-sdk"\)\)') {
        $content = $content -replace '(implementation\(fileTree\(mapOf\("dir" to "libs", "include" to listOf\("\*\.jar"\)\)\)\))', 
            '$1' + "`n    implementation(project(`":anti-debug-sdk`"))  // AntiDebugSDK Protection"
        Set-Content $AppBuildGradle $content -NoNewline
        Write-Host "✅ Added AntiDebugSDK dependency to build.gradle.kts" -ForegroundColor Green
    } else {
        Write-Host "ℹ️  AntiDebugSDK dependency already present in build.gradle.kts" -ForegroundColor Blue
    }
}

# Function to update AndroidManifest.xml
function Update-AndroidManifest {
    Write-Host "🔧 Updating AndroidManifest.xml..." -ForegroundColor Yellow
    
    $content = Get-Content $AppManifest -Raw
    
    # Add INTERNET permission if not present
    if ($content -notmatch 'android\.permission\.INTERNET') {
        $content = $content -replace '(<uses-permission android:name="android\.permission\.USE_FULL_SCREEN_INTENT" />)', 
            '$1' + "`n    <uses-permission android:name=`"android.permission.INTERNET`" />"
        Set-Content $AppManifest $content -NoNewline
        Write-Host "✅ Added INTERNET permission to AndroidManifest.xml" -ForegroundColor Green
    } else {
        Write-Host "ℹ️  INTERNET permission already present in AndroidManifest.xml" -ForegroundColor Blue
    }
}

# Function to create protection configuration based on parameters
function Get-ProtectionConfig {
    param([string]$Level)
    
    $config = @{
        EnableMonitoring = $false
        ResponseType = "LOG_ONLY"
        EnableDebuggerDetection = $false
        EnableRootDetection = $false
        EnableEmulatorDetection = $false
        EnableTamperDetection = $false
        EnableHookDetection = $false
        EnableBehavioralDetection = $false
        EnableDataProtection = $false
    }
    
    switch ($Level) {
        "Minimal" {
            $config.EnableDebuggerDetection = $true
            $config.EnableDataProtection = $true
        }
        "Full" {
            $config.EnableMonitoring = $true
            $config.EnableDebuggerDetection = $true
            $config.EnableRootDetection = $true
            $config.EnableEmulatorDetection = $true
            $config.EnableTamperDetection = $true
            $config.EnableHookDetection = $true
            $config.EnableBehavioralDetection = $true
            $config.EnableDataProtection = $true
        }
        "Debug" {
            $config.ResponseType = "LOG_ONLY"
            $config.EnableDebuggerDetection = $true
            $config.EnableDataProtection = $true
        }
        "Production" {
            $config.EnableMonitoring = $true
            $config.ResponseType = "DELAYED_EXIT"
            $config.EnableDebuggerDetection = $true
            $config.EnableRootDetection = $true
            $config.EnableEmulatorDetection = $true
            $config.EnableTamperDetection = $true
            $config.EnableHookDetection = $true
            $config.EnableBehavioralDetection = $true
            $config.EnableDataProtection = $true
        }
        default {
            # Custom configuration
            $config.EnableDebuggerDetection = $true
            $config.EnableDataProtection = $true
        }
    }
    
    $config.ResponseType = $ResponseType
    return $config
}

# Function to generate protection code based on configuration
function Generate-ProtectionCode {
    param([hashtable]$Config)
    
    $imports = @"
import com.example.antidebug.AntiDebug
import com.example.antidebug.ResponseHandler
import com.example.antidebug.ThreatType
"@

    $initCode = @"

    /**
     * Initialize and configure AntiDebugSDK protections
     * Generated by apply-protection.ps1 script
     */
    private fun initializeAntiDebugProtection() {
        try {
            // Initialize SDK with monitoring: $($Config.EnableMonitoring)
            AntiDebug.init(this, enableContinuousMonitoring = $($Config.EnableMonitoring.ToString().ToLower()))
            
            // Configure response type
            AntiDebug.configureResponse(ResponseHandler.ResponseType.$($Config.ResponseType))
            
"@

    if ($Config.EnableDebuggerDetection) {
        $initCode += @"
            // Enable debugger detection
            performDebuggerCheck()
            
"@
    }

    if ($Config.EnableRootDetection) {
        $initCode += @"
            // Enable root detection
            performRootCheck()
            
"@
    }

    if ($Config.EnableEmulatorDetection) {
        $initCode += @"
            // Enable emulator detection
            performEmulatorCheck()
            
"@
    }

    if ($Config.EnableTamperDetection) {
        $initCode += @"
            // Enable tamper detection
            performTamperCheck()
            
"@
    }

    if ($Config.EnableHookDetection) {
        $initCode += @"
            // Enable hook detection
            performHookCheck()
            
"@
    }

    if ($Config.EnableBehavioralDetection) {
        $initCode += @"
            // Enable behavioral detection
            performBehavioralCheck()
            
"@
    }

    if ($Config.EnableDataProtection) {
        $initCode += @"
            // Initialize data protection
            setupDataProtection()
            
"@
    }

    $initCode += @"
            Log.i("Protection", "AntiDebugSDK protection applied successfully")
            
        } catch (e: Exception) {
            Log.e("Protection", "Failed to initialize AntiDebugSDK protection", e)
        }
    }
"@

    $helperMethods = ""

    if ($Config.EnableDebuggerDetection) {
        $helperMethods += @"

    private fun performDebuggerCheck() {
        if (AntiDebug.isDebuggerAttached()) {
            Log.w("Protection", "🔍 Debugger detected!")
            AntiDebug.handleThreat(ThreatType.DEBUGGER)
        }
    }
"@
    }

    if ($Config.EnableRootDetection) {
        $helperMethods += @"

    private fun performRootCheck() {
        if (AntiDebug.isDeviceRooted()) {
            Log.w("Protection", "🔓 Root detected!")
            AntiDebug.handleThreat(ThreatType.ROOT)
        }
    }
"@
    }

    if ($Config.EnableEmulatorDetection) {
        $helperMethods += @"

    private fun performEmulatorCheck() {
        if (AntiDebug.isRunningOnEmulator()) {
            Log.w("Protection", "📱 Emulator detected!")
            AntiDebug.handleThreat(ThreatType.EMULATOR)
        }
    }
"@
    }

    if ($Config.EnableTamperDetection) {
        $helperMethods += @"

    private fun performTamperCheck() {
        if (AntiDebug.isApplicationTampered()) {
            Log.w("Protection", "🔒 Tampering detected!")
            AntiDebug.handleThreat(ThreatType.TAMPERING)
        }
    }
"@
    }

    if ($Config.EnableHookDetection) {
        $helperMethods += @"

    private fun performHookCheck() {
        if (AntiDebug.areHooksDetected()) {
            Log.w("Protection", "🎣 Hooks detected!")
            AntiDebug.handleThreat(ThreatType.HOOKS)
        }
    }
"@
    }

    if ($Config.EnableBehavioralDetection) {
        $helperMethods += @"

    private fun performBehavioralCheck() {
        if (AntiDebug.isSuspiciousBehavior()) {
            Log.w("Protection", "📊 Suspicious behavior detected!")
            AntiDebug.handleThreat(ThreatType.SUSPICIOUS_BEHAVIOR)
        }
    }
"@
    }

    if ($Config.EnableDataProtection) {
        $helperMethods += @"

    private fun setupDataProtection() {
        val dataProtection = AntiDebug.getDataProtection()
        dataProtection.storeSecureValue("protection_enabled", "true")
        dataProtection.storeSecureValue("protection_level", "$($Config | ConvertTo-Json -Compress)")
        Log.i("Protection", "🔐 Data protection initialized")
    }
"@
    }

    $cleanupCode = @"

    /**
     * Cleanup AntiDebugSDK resources
     * Generated by apply-protection.ps1 script
     */
    private fun cleanupAntiDebugProtection() {
        try {
            AntiDebug.cleanup()
            Log.i("Protection", "AntiDebugSDK cleanup completed")
        } catch (e: Exception) {
            Log.e("Protection", "Error during AntiDebugSDK cleanup", e)
        }
    }
"@

    return @{
        Imports = $imports
        InitCode = $initCode
        HelperMethods = $helperMethods
        CleanupCode = $cleanupCode
    }
}

# Function to update MainActivity.kt
function Update-MainActivity {
    param([hashtable]$Config)
    
    Write-Host "🔧 Updating MainActivity.kt with protection code..." -ForegroundColor Yellow
    
    $content = Get-Content $MainActivity -Raw
    $protectionCode = Generate-ProtectionCode -Config $Config
    
    # Add imports
    if ($content -notmatch 'import com\.example\.antidebug\.AntiDebug') {
        $content = $content -replace '(import com\.android\.calculator\.util\.ScientificModeTypes)', 
            $protectionCode.Imports + "`n" + '$1'
    }
    
    # Add initialization call in onCreate
    if ($content -notmatch 'initializeAntiDebugProtection') {
        $content = $content -replace '(// Initialize AntiDebugSDK[\r\n\s]*initializeAntiDebugSDK\(\))', 
            "// Initialize AntiDebugSDK Protection (Generated)`n        initializeAntiDebugProtection()"
    }
    
    # Add protection methods before the existing private methods
    if ($content -notmatch 'initializeAntiDebugProtection') {
        # Find a good place to insert (before setupUI method)
        $content = $content -replace '(\s+private fun setupUI\(\))', 
            $protectionCode.InitCode + $protectionCode.HelperMethods + '$1'
    }
    
    # Add cleanup in onDestroy
    if ($content -notmatch 'cleanupAntiDebugProtection') {
        $content = $content -replace '(override fun onDestroy\(\) \{[\s\S]*?AntiDebug\.cleanup\(\)[\s\S]*?\})', 
            $protectionCode.CleanupCode.Replace("private fun", "override fun") + 
            "`n`n    // Original onDestroy preserved`n    " + '$1'.Replace("AntiDebug.cleanup()", "cleanupAntiDebugProtection()")
    }
    
    Set-Content $MainActivity $content -NoNewline
    Write-Host "✅ Updated MainActivity.kt with protection code" -ForegroundColor Green
}

# Main execution
try {
    # Determine protection level
    $Level = "Custom"
    if ($Full) { $Level = "Full" }
    elseif ($Minimal) { $Level = "Minimal" }
    elseif ($Debug) { $Level = "Debug" }
    elseif ($Production) { $Level = "Production" }
    
    Write-Host "🔧 Applying protection level: $Level" -ForegroundColor Yellow
    Write-Host "📋 Response type: $ResponseType" -ForegroundColor Yellow
    
    # Get configuration
    $config = Get-ProtectionConfig -Level $Level
    
    # Display configuration
    Write-Host "`n📊 Protection Configuration:" -ForegroundColor Cyan
    $config.GetEnumerator() | Sort-Object Name | ForEach-Object {
        $status = if ($_.Value) { "✅" } else { "❌" }
        Write-Host "  $status $($_.Key): $($_.Value)" -ForegroundColor White
    }
    
    Write-Host "`n🚀 Applying protections..." -ForegroundColor Yellow
    
    # Apply all updates
    Update-SettingsGradle
    Update-AppBuildGradle
    Update-AndroidManifest
    Update-MainActivity -Config $config
    
    # Create protection metadata
    $metadata = @{
        AppliedAt = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
        Level = $Level
        Configuration = $config
        BackupTimestamp = $Timestamp
    }
    
    $metadata | ConvertTo-Json -Depth 3 | Set-Content "$ProjectRoot\.protection-metadata.json"
    
    Write-Host "`n🎉 Protection application completed!" -ForegroundColor Green
    Write-Host "📁 Backup files saved with timestamp: $Timestamp" -ForegroundColor Green
    Write-Host "📋 Protection metadata saved to .protection-metadata.json" -ForegroundColor Green
    
    Write-Host "`n📖 Next Steps:" -ForegroundColor Cyan
    Write-Host "  1. Run 'gradle sync' in Android Studio" -ForegroundColor White
    Write-Host "  2. Build and test your app" -ForegroundColor White
    Write-Host "  3. Check logs for protection status" -ForegroundColor White
    Write-Host "  4. Use 'remove-protection.ps1' to revert changes" -ForegroundColor White
    
} catch {
    Write-Host "`n❌ Error applying protection: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "🔄 You can restore from backups in .protection-backups/" -ForegroundColor Yellow
    exit 1
}

Write-Host "`n🛡️ AntiDebugSDK Protection Applied Successfully! 🛡️" -ForegroundColor Green
