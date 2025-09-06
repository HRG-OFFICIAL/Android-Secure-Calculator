# Anti-Debug Testing APK Builder
# Temporarily bypasses specific protections for testing purposes

param(
    [Parameter(Mandatory=$false)]
    [ValidateSet("debug", "release")]
    [string]$BuildType = "debug",
    
    [Parameter(Mandatory=$false)]
    [switch]$BypassEmulator = $false,
    
    [Parameter(Mandatory=$false)]
    [switch]$BypassDebugger = $false,
    
    [Parameter(Mandatory=$false)]
    [switch]$BypassRoot = $false,
    
    [Parameter(Mandatory=$false)]
    [switch]$BypassTamper = $false,
    
    [Parameter(Mandatory=$false)]
    [switch]$BypassAll = $false,
    
    [Parameter(Mandatory=$false)]
    [string]$OutputDir = "builds",
    
    [Parameter(Mandatory=$false)]
    [switch]$Force = $false,
    
    [Parameter(Mandatory=$false)]
    [switch]$InstallAfterBuild = $false,
    
    [Parameter(Mandatory=$false)]
    [switch]$ForceDetectAll = $false
)

$MainActivityPath = "app\src\main\java\com\android\calculator\activities\MainActivity.kt"
$BackupPath = "MainActivity_backup_$(Get-Date -Format 'yyyyMMdd_HHmmss').kt"

Write-Host "Anti-Debug Testing APK Builder" -ForegroundColor Cyan
Write-Host "=================================" -ForegroundColor Cyan
Write-Host ""

# Validate parameters
if (-not (Test-Path $MainActivityPath)) {
    Write-Host "[ERROR] MainActivity.kt not found at: $MainActivityPath" -ForegroundColor Red
    exit 1
}

# Show configuration
Write-Host "Testing Configuration:" -ForegroundColor Green
Write-Host "   Build Type: $BuildType" -ForegroundColor Yellow
Write-Host "   Bypass Emulator: $(if($BypassEmulator -or $BypassAll){'[YES]'}else{'[NO]'})" -ForegroundColor $(if($BypassEmulator -or $BypassAll){'Green'}else{'Red'})
Write-Host "   Bypass Debugger: $(if($BypassDebugger -or $BypassAll){'[YES]'}else{'[NO]'})" -ForegroundColor $(if($BypassDebugger -or $BypassAll){'Green'}else{'Red'})
Write-Host "   Bypass Root: $(if($BypassRoot -or $BypassAll){'[YES]'}else{'[NO]'})" -ForegroundColor $(if($BypassRoot -or $BypassAll){'Green'}else{'Red'})
Write-Host "   Bypass Tamper: $(if($BypassTamper -or $BypassAll){'[YES]'}else{'[NO]'})" -ForegroundColor $(if($BypassTamper -or $BypassAll){'Green'}else{'Red'})
Write-Host "   Force All Detections: $(if($ForceDetectAll){'[YES]'}else{'[NO]'})" -ForegroundColor $(if($ForceDetectAll){'Yellow'}else{'Gray'})
Write-Host ""

if (-not $Force) {
    $confirmation = Read-Host "Continue with testing APK build? (y/N)"
    if ($confirmation -ne 'y' -and $confirmation -ne 'Y') {
        Write-Host "[CANCELLED] Build cancelled by user" -ForegroundColor Yellow
        exit 0
    }
}
try {
    # 1. Create backup
    Write-Host "Creating backup of MainActivity.kt..." -ForegroundColor Cyan
    Copy-Item $MainActivityPath $BackupPath -Force
    Write-Host "   Backup created: $BackupPath" -ForegroundColor Green
    
    # 2. Read original content
    $content = Get-Content $MainActivityPath -Raw
    $originalContent = $content
    
    # 3. Create modified content based on bypass flags
    Write-Host ""
    Write-Host "Applying testing bypasses..." -ForegroundColor Cyan
    
    $modifications = @()
    $conditionParts = @()
    
    # Build the condition string based on what to bypass
    if (-not ($BypassDebugger -or $BypassAll)) {
        $conditionParts += "securityReport.debuggerDetected"
    } else {
        $modifications += "Debugger detection"
    }
    
    if (-not ($BypassEmulator -or $BypassAll)) {
        $conditionParts += "securityReport.emulatorDetected"
    } else {
        $modifications += "Emulator detection"
    }
    
    if (-not ($BypassRoot -or $BypassAll)) {
        $conditionParts += "securityReport.rootDetected"
    } else {
        $modifications += "Root detection"
    }
    
    if (-not ($BypassTamper -or $BypassAll)) {
        $conditionParts += "securityReport.tamperingDetected"
    } else {
        $modifications += "Tamper detection"
    }
    
    # Create the new condition
    if ($conditionParts.Count -eq 0) {
        $newCondition = "false // ALL PROTECTIONS BYPASSED FOR TESTING"
    } else {
        $newCondition = $conditionParts -join " || "
    }
    
    # Find and replace the security check condition (handles multi-line format)
    $pattern = 'if \(securityReport\.debuggerDetected \|\| securityReport\.emulatorDetected \|\| [\s\S]*?securityReport\.rootDetected \|\| securityReport\.tamperingDetected\)'
    
    if ($content -match $pattern -or $ForceDetectAll) {
        if ($ForceDetectAll) {
            # Force all detections to be true but bypass termination - replace entire security block
            $fullSecurityBlockPattern = 'try \{[\s\S]*?// ===== ANTI-DEBUG PROTECTION END ====='
            $forceDetectionReplacement = @"
        // ===== FORCE DETECTION TESTING MODE START =====
        try {
            // Initialize AntiDebug SDK
            AntiDebug.init(this, enableContinuousMonitoring = true)
            
            // Perform comprehensive security check
            val securityReport = AntiDebug.performSecurityCheck()
            
            // TESTING MODE: Force all detections to TRUE but don't terminate
            Log.d("AntiDebug", "TESTING MODE - Forced Detection Report - Debugger: true, Emulator: true, Root: true, Tampered: true")
            Log.w("AntiDebug", "All security threats detected but continuing execution for testing purposes!")
            
            // Handle threat detection for logging but don't terminate
            if (true) { // Always true for testing
                Log.i("AntiDebug", "Security threats simulated - app continuing for testing")
                
                // Call threat handlers but don't terminate
                try {
                    AntiDebug.handleThreat(ThreatType.DEBUGGER)
                    AntiDebug.handleThreat(ThreatType.EMULATOR) 
                    AntiDebug.handleThreat(ThreatType.ROOT)
                    AntiDebug.handleThreat(ThreatType.TAMPERING)
                } catch (e: Exception) {
                    Log.w("AntiDebug", "Threat handler called in testing mode", e)
                }
                
                // DON'T call finishAffinity() - continue execution
                Log.i("AntiDebug", "Testing mode: Application continuing despite all threats detected")
            }
        } catch (e: Exception) {
            Log.e("AntiDebug", "Anti-debug testing mode failed", e)
            // Even on error, don't terminate in testing mode
            Log.w("AntiDebug", "Testing mode: Continuing despite anti-debug error")
        }
        // ===== FORCE DETECTION TESTING MODE END =====
"@
            $content = $content -replace $fullSecurityBlockPattern, $forceDetectionReplacement
            $modifications += "Forced all detections to TRUE"
        } elseif ($conditionParts.Count -eq 0) {
            # If bypassing all, replace the entire try-catch security block with a comment
            $fullSecurityBlock = 'try \{[\s\S]*?// ===== ANTI-DEBUG PROTECTION END ====='
            $blockReplacement = "        // ===== ALL ANTI-DEBUG PROTECTIONS BYPASSED FOR TESTING ====="
            $content = $content -replace $fullSecurityBlock, $blockReplacement
        } else {
            # Otherwise just modify the condition
            $replacement = "if ($newCondition)"
            $content = $content -replace $pattern, $replacement
        }
        
        if ($modifications.Count -gt 0) {
            Write-Host "   [BYPASSED] $($modifications -join ', ')" -ForegroundColor Yellow
        }
        if ($conditionParts.Count -gt 0) {
            Write-Host "   [ACTIVE] $(($conditionParts -replace 'securityReport\.','') -join ', ')" -ForegroundColor Red
        }
    } else {
        Write-Host "   [WARNING] Could not find security check pattern for modification" -ForegroundColor Yellow
    }
    
    # Add testing mode comment
    $testingComment = "        // ===== TESTING MODE ENABLED ====="
    if ($modifications.Count -gt 0) {
        $testingComment += "`n        // Bypassed: $($modifications -join ', ')"
    }
    $testingComment += "`n        // ===== TESTING MODE ENABLED ====="
    
    $content = $content -replace "(// ===== ANTI-DEBUG PROTECTION START =====)", "`$1`n$testingComment"
    
    # 4. Write modified content
    Set-Content $MainActivityPath $content -NoNewline
    Write-Host "   [SUCCESS] Testing modifications applied" -ForegroundColor Green
    
    # 5. Build APK
    Write-Host ""
    Write-Host "Building testing APK..." -ForegroundColor Cyan
    
    $buildCommand = if ($BuildType -eq "debug") { "assembleDebug" } else { "assembleRelease" }
    $buildResult = & "./gradlew" $buildCommand "-x" "lintDebug" "-x" "lintRelease" "--no-daemon"
    
    if ($LASTEXITCODE -ne 0) {
        throw "Gradle build failed with exit code $LASTEXITCODE"
    }
    
    # 6. Copy APK to builds directory
    Write-Host ""
    Write-Host "Copying APK..." -ForegroundColor Cyan
    
    $apkSource = "app\build\outputs\apk\$BuildType\app-$BuildType.apk"
    $testingFlags = @()
    if ($ForceDetectAll) { 
        $testingFlags = @("force-detect-all") 
    } else {
        if ($BypassEmulator -or $BypassAll) { $testingFlags += "noemu" }
        if ($BypassDebugger -or $BypassAll) { $testingFlags += "nodbg" }
        if ($BypassRoot -or $BypassAll) { $testingFlags += "noroot" }
        if ($BypassTamper -or $BypassAll) { $testingFlags += "notamp" }
        if ($BypassAll) { $testingFlags = @("bypass-all") }
    }
    
    $flagSuffix = if ($testingFlags.Count -gt 0) { "-$($testingFlags -join '-')" } else { "-full-protection" }
    $timestamp = Get-Date -Format "yyyy-MM-dd_HH-mm-ss"
    $apkDestination = "$OutputDir\calculator-testing$flagSuffix-$BuildType-$timestamp.apk"
    
    if (-not (Test-Path $OutputDir)) {
        New-Item -ItemType Directory -Path $OutputDir -Force | Out-Null
    }
    
    Copy-Item $apkSource $apkDestination -Force
    $apkSize = [math]::Round((Get-Item $apkDestination).Length / 1MB, 2)
    
    Write-Host "   [SUCCESS] APK created: $apkDestination" -ForegroundColor Green
    Write-Host "   [INFO] APK Size: $apkSize MB" -ForegroundColor Green
    
    # 7. Install if requested
    if ($InstallAfterBuild) {
        Write-Host ""
        Write-Host "Installing APK..." -ForegroundColor Cyan
        $installResult = & "adb" "install" "-r" $apkDestination 2>&1
        if ($LASTEXITCODE -eq 0) {
            Write-Host "   [SUCCESS] APK installed successfully" -ForegroundColor Green
        } else {
            Write-Host "   [WARNING] APK installation failed (is device connected?)" -ForegroundColor Yellow
        }
    }
    
    Write-Host ""
    Write-Host "Testing APK Build Complete!" -ForegroundColor Green
    Write-Host "============================" -ForegroundColor Green
    Write-Host ""
    Write-Host "Testing APK: $apkDestination" -ForegroundColor Cyan
    Write-Host "Bypassed Protections: $(if($modifications.Count -gt 0){$modifications -join ', '}else{'None - Full Protection'})" -ForegroundColor Yellow
    Write-Host "Active Protections: $(if($conditionParts.Count -gt 0){($conditionParts -replace 'securityReport\.','') -join ', '}else{'None - All Bypassed'})" -ForegroundColor Red
    Write-Host ""
    Write-Host "Usage Examples:" -ForegroundColor White
    Write-Host "   - Test in emulator: Install and run (should work if emulator bypassed)" -ForegroundColor Gray
    Write-Host "   - Test calculator: Try calculations, history, themes" -ForegroundColor Gray
    Write-Host "   - Test remaining protections: Check logcat for active detections" -ForegroundColor Gray
    
} catch {
    Write-Host ""
    Write-Host "[ERROR] Build failed: $($_.Exception.Message)" -ForegroundColor Red
} finally {
    # 8. Always restore original file
    Write-Host ""
    Write-Host "Restoring original MainActivity.kt..." -ForegroundColor Cyan
    
    if (Test-Path $BackupPath) {
        Copy-Item $BackupPath $MainActivityPath -Force
        Remove-Item $BackupPath -Force
        Write-Host "   [SUCCESS] Original file restored" -ForegroundColor Green
    } else {
        # Fallback: restore from variable if backup file is missing
        Set-Content $MainActivityPath $originalContent -NoNewline
        Write-Host "   [SUCCESS] Original content restored from memory" -ForegroundColor Green
    }
    
    Write-Host ""
    Write-Host "Source code is clean and unchanged" -ForegroundColor Green
}
