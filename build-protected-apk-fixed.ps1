# Build Protected APK Script - Test Version
# This script temporarily applies AntiDebugSDK protections and builds protected APK files

param(
    [ValidateSet("debug", "release")]
    [string]$BuildType = "debug",        # Build configuration (debug or release)
    [string]$OutputDir = "builds",       # Output directory for APK files
    [switch]$CleanBuild,         # Clean before building
    [switch]$Force               # Force build without confirmation
)

Write-Host "Protected Calculator APK Builder" -ForegroundColor Red
Write-Host "================================" -ForegroundColor Red

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
    
    Write-Host ""
    Write-Host "Warning: $Message" -ForegroundColor Yellow
    $response = Read-Host "Continue? (y/N)"
    return $response -eq 'y' -or $response -eq 'Y'
}

# Function to check AntiDebugSDK availability
function Test-AntiDebugSDKAvailability {
    Write-Host "Checking AntiDebugSDK availability..." -ForegroundColor Yellow
    
    $antiDebugPath = "$ProjectRoot\anti-debug-sdk"
    $antiDebugBuild = "$antiDebugPath\build.gradle"
    $antiDebugSrc = "$antiDebugPath\src\main\java\com\example\antidebug"
    
    $allValid = $true
    
    if (Test-Path $antiDebugPath) {
        Write-Host "  anti-debug-sdk directory found" -ForegroundColor Green
    } else {
        Write-Host "  anti-debug-sdk directory missing" -ForegroundColor Red
        $allValid = $false
    }
    
    if (Test-Path $antiDebugBuild) {
        Write-Host "  anti-debug-sdk/build.gradle found" -ForegroundColor Green
    } else {
        Write-Host "  anti-debug-sdk build configuration missing" -ForegroundColor Red
        $allValid = $false
    }
    
    if (Test-Path $antiDebugSrc) {
        Write-Host "  AntiDebugSDK source code found" -ForegroundColor Green
    } else {
        Write-Host "  AntiDebugSDK source code missing" -ForegroundColor Red
        $allValid = $false
    }
    
    return $allValid
}

# Function to build protected APK (simplified for testing)
function Build-ProtectedAPK {
    param([string]$BuildType)
    
    Write-Host "Building protected APK..." -ForegroundColor Red
    
    # For testing, just show that we would apply protections
    Write-Host "Would apply protection code temporarily..." -ForegroundColor Yellow
    Write-Host "Would enable anti-debug-sdk in settings.gradle.kts" -ForegroundColor Yellow
    Write-Host "Would add anti-debug-sdk dependency to app/build.gradle.kts" -ForegroundColor Yellow
    Write-Host "Would add protection code to MainActivity.kt" -ForegroundColor Yellow
    
    # Simulate build process
    Write-Host "Would run gradle $BuildType build..." -ForegroundColor Blue
    Write-Host "Note: Actual build skipped due to Java version compatibility issue" -ForegroundColor Yellow
    
    # Show what would happen
    Write-Host "Would restore original source files after build..." -ForegroundColor Blue
    
    return $true
}

# Main execution
try {
    Write-Host ""
    Write-Host "Build Configuration:" -ForegroundColor Cyan
    Write-Host "Type: Protected (AntiDebugSDK)" -ForegroundColor Red
    Write-Host "Build Type: $BuildType" -ForegroundColor White
    Write-Host "Output Directory: $OutputPath" -ForegroundColor White
    Write-Host "Clean Build: $($CleanBuild -eq $true)" -ForegroundColor White
    
    # Check AntiDebugSDK availability
    if (-not (Test-AntiDebugSDKAvailability)) {
        Write-Host ""
        Write-Host "AntiDebugSDK verification failed!" -ForegroundColor Red
        exit 1
    }
    
    # Get confirmation
    $confirmMessage = "Build PROTECTED calculator APK with AntiDebugSDK? This will temporarily modify source files."
    if (-not (Get-UserConfirmation $confirmMessage)) {
        Write-Host ""
        Write-Host "Build cancelled by user" -ForegroundColor Yellow
        exit 0
    }
    
    Write-Host ""
    Write-Host "Starting protected APK build process..." -ForegroundColor Red
    
    # Build the protected APK
    $buildSuccess = Build-ProtectedAPK -BuildType $BuildType
    
    if ($buildSuccess) {
        Write-Host ""
        Write-Host "Protected APK build simulation completed!" -ForegroundColor Green
    } else {
        Write-Host ""
        Write-Host "Protected APK build failed!" -ForegroundColor Red
        exit 1
    }
    
} catch {
    Write-Host ""
    Write-Host "Error during build process: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Stack trace: $($_.ScriptStackTrace)" -ForegroundColor Gray
    exit 1
}

Write-Host ""
Write-Host "Protected Calculator APK Build Test Complete!" -ForegroundColor Red
