# APK Manager Script - Test Version
# This script provides a unified interface for building both clean and protected APK files

param(
    [ValidateSet("clean", "protected", "both")]
    [string]$Mode = "both",              # Build mode: clean, protected, or both
    [ValidateSet("debug", "release")]
    [string]$BuildType = "debug",        # Build configuration (debug or release)
    [string]$OutputDir = "builds",       # Output directory for APK files
    [switch]$CleanBuild,         # Clean before building
    [switch]$Force,              # Force build without confirmation
    [switch]$SkipTests,          # Skip post-build verification tests
    [switch]$InstallAfterBuild   # Install APK after successful build
)

Write-Host "Calculator APK Manager" -ForegroundColor Cyan
Write-Host "=====================" -ForegroundColor Cyan

# Define paths
$ProjectRoot = Get-Location
$OutputPath = "$ProjectRoot\$OutputDir"
$Timestamp = Get-Date -Format "yyyy-MM-dd_HH-mm-ss"

# Build script paths
$CleanBuildScript = "$ProjectRoot\build-clean-apk-fixed.ps1"
$ProtectedBuildScript = "$ProjectRoot\build-protected-apk-fixed.ps1"

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
    
    Write-Host ""
    Write-Host "Warning: $Message" -ForegroundColor Yellow
    $response = Read-Host "Continue? (y/N)"
    return $response -eq 'y' -or $response -eq 'Y'
}

# Function to display menu
function Show-BuildMenu {
    Write-Host ""
    Write-Host "Build Options:" -ForegroundColor Cyan
    Write-Host "=============" -ForegroundColor Cyan
    Write-Host "  Clean APK    - No security protections, suitable for development" -ForegroundColor Green
    Write-Host "  Protected APK - AntiDebugSDK enabled, suitable for production" -ForegroundColor Red
    Write-Host "  Both APKs    - Build both versions for comparison" -ForegroundColor Blue
    Write-Host ""
    
    Write-Host "Current Configuration:" -ForegroundColor Cyan
    Write-Host "  Build Type: $BuildType" -ForegroundColor White
    Write-Host "  Output Directory: $OutputPath" -ForegroundColor White
    Write-Host "  Clean Build: $($CleanBuild -eq $true)" -ForegroundColor White
    Write-Host "  Auto Install: $($InstallAfterBuild -eq $true)" -ForegroundColor White
    Write-Host "  Skip Tests: $($SkipTests -eq $true)" -ForegroundColor White
}

# Function to check script availability
function Test-BuildScripts {
    Write-Host "Checking build script availability..." -ForegroundColor Yellow
    
    $allScriptsFound = $true
    
    # Check for clean build script
    if (Test-Path $CleanBuildScript) {
        Write-Host "  Clean build script found: build-clean-apk-fixed.ps1" -ForegroundColor Green
    } else {
        Write-Host "  Clean build script not found!" -ForegroundColor Red
        Write-Host "     Expected: build-clean-apk-fixed.ps1" -ForegroundColor Yellow
        $allScriptsFound = $false
    }
    
    # Check for protected build script
    if (Test-Path $ProtectedBuildScript) {
        Write-Host "  Protected build script found: build-protected-apk-fixed.ps1" -ForegroundColor Green
    } else {
        Write-Host "  Protected build script not found!" -ForegroundColor Red
        Write-Host "     Expected: build-protected-apk-fixed.ps1" -ForegroundColor Yellow
        $allScriptsFound = $false
    }
    
    return $allScriptsFound
}

# Function to build clean APK
function Build-CleanAPK {
    Write-Host ""
    Write-Host "Building Clean Calculator APK..." -ForegroundColor Green
    Write-Host "================================" -ForegroundColor Green
    
    # Prepare script parameters as hashtable for splatting
    $scriptParams = @{
        "BuildType" = $BuildType
        "OutputDir" = $OutputDir
    }
    
    if ($CleanBuild) {
        $scriptParams["CleanBuild"] = $true
    }
    
    if ($Force) {
        $scriptParams["Force"] = $true
    }
    
    # Execute the clean build script
    try {
        Write-Host "Executing: $CleanBuildScript with parameters: $($scriptParams.Keys -join ', ')" -ForegroundColor Blue
        $result = & $CleanBuildScript @scriptParams
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "Clean APK build completed successfully!" -ForegroundColor Green
            return $true
        } else {
            Write-Host "Clean APK build failed with exit code: $LASTEXITCODE" -ForegroundColor Red
            return $false
        }
    } catch {
        Write-Host "Error executing clean build script: $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

# Function to build protected APK
function Build-ProtectedAPK {
    Write-Host ""
    Write-Host "Building Protected Calculator APK..." -ForegroundColor Red
    Write-Host "===================================" -ForegroundColor Red
    
    # Prepare script parameters as hashtable for splatting
    $scriptParams = @{
        "BuildType" = $BuildType
        "OutputDir" = $OutputDir
    }
    
    if ($CleanBuild) {
        $scriptParams["CleanBuild"] = $true
    }
    
    if ($Force) {
        $scriptParams["Force"] = $true
    }
    
    # Execute the protected build script
    try {
        Write-Host "Executing: $ProtectedBuildScript with parameters: $($scriptParams.Keys -join ', ')" -ForegroundColor Blue
        $result = & $ProtectedBuildScript @scriptParams
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "Protected APK build completed successfully!" -ForegroundColor Green
            return $true
        } else {
            Write-Host "Protected APK build failed with exit code: $LASTEXITCODE" -ForegroundColor Red
            return $false
        }
    } catch {
        Write-Host "Error executing protected build script: $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

# Function to generate build summary
function Show-BuildSummary {
    param([hashtable]$BuildResults)
    
    Write-Host ""
    Write-Host "Build Summary" -ForegroundColor Cyan
    Write-Host "=============" -ForegroundColor Cyan
    
    # Display build results
    foreach ($buildType in $BuildResults.Keys) {
        $result = $BuildResults[$buildType]
        $status = if ($result) { "SUCCESS" } else { "FAILED" }
        $color = if ($result) { "Green" } else { "Red" }
        
        Write-Host "  $buildType APK: $status" -ForegroundColor $color
    }
    
    Write-Host ""
    Write-Host "Next Steps:" -ForegroundColor Cyan
    Write-Host "  1. Test APK functionality on device/emulator" -ForegroundColor White
    Write-Host "  2. Compare clean vs protected APK behavior" -ForegroundColor White
    Write-Host "  3. Use clean APK for development and debugging" -ForegroundColor White
    Write-Host "  4. Use protected APK for security testing and production" -ForegroundColor White
}

# Main execution
try {
    # Display configuration
    Show-BuildMenu
    
    # Check script availability
    if (-not (Test-BuildScripts)) {
        Write-Host ""
        Write-Host "Build scripts not available! Please fix the issues above." -ForegroundColor Red
        exit 1
    }
    
    # Get confirmation
    $confirmMessage = "Proceed with $Mode APK build(s) in $BuildType mode?"
    if (-not (Get-UserConfirmation $confirmMessage)) {
        Write-Host ""
        Write-Host "Build cancelled by user" -ForegroundColor Yellow
        exit 0
    }
    
    Write-Host ""
    Write-Host "Starting APK build process..." -ForegroundColor Cyan
    
    # Track build results
    $buildResults = @{}
    $builtApkTypes = @()
    
    # Execute builds based on mode
    switch ($Mode) {
        "clean" {
            Write-Host ""
            Write-Host "Building Clean APK Only..." -ForegroundColor Green
            $buildResults["Clean"] = Build-CleanAPK
            if ($buildResults["Clean"]) { $builtApkTypes += "clean" }
        }
        "protected" {
            Write-Host ""
            Write-Host "Building Protected APK Only..." -ForegroundColor Red
            $buildResults["Protected"] = Build-ProtectedAPK
            if ($buildResults["Protected"]) { $builtApkTypes += "protected" }
        }
        "both" {
            Write-Host ""
            Write-Host "Building Both APK Types..." -ForegroundColor Blue
            
            # Build clean first (faster, more likely to succeed)
            $buildResults["Clean"] = Build-CleanAPK
            if ($buildResults["Clean"]) { $builtApkTypes += "clean" }
            
            # Build protected second
            $buildResults["Protected"] = Build-ProtectedAPK
            if ($buildResults["Protected"]) { $builtApkTypes += "protected" }
        }
    }
    
    # Show build summary
    Show-BuildSummary -BuildResults $buildResults
    
    # Determine overall success
    $overallSuccess = ($buildResults.Values | Where-Object { $_ -eq $false }).Count -eq 0
    
    if ($overallSuccess) {
        Write-Host ""
        Write-Host "All builds completed successfully!" -ForegroundColor Green
        exit 0
    } else {
        Write-Host ""
        Write-Host "Some builds failed or had issues!" -ForegroundColor Yellow
        Write-Host "Check the detailed output above for troubleshooting information." -ForegroundColor Gray
        exit 1
    }
    
} catch {
    Write-Host ""
    Write-Host "Error in APK Manager: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Stack trace: $($_.ScriptStackTrace)" -ForegroundColor Gray
    exit 1
}

Write-Host ""
Write-Host "Calculator APK Manager Complete!" -ForegroundColor Cyan
