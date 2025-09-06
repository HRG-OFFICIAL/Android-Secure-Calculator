# Build Clean APK Script - Test Version
# This script builds APK files without AntiDebugSDK protections (clean calculator app)

param(
    [ValidateSet("debug", "release")]
    [string]$BuildType = "debug",        # Build configuration (debug or release)
    [string]$OutputDir = "builds",       # Output directory for APK files
    [switch]$CleanBuild,         # Clean before building
    [switch]$Force               # Force build without confirmation
)

Write-Host "Clean Calculator APK Builder" -ForegroundColor Blue
Write-Host "=============================" -ForegroundColor Blue

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

# Function to build clean APK
function Build-CleanAPK {
    param([string]$BuildType)
    
    Write-Host "Building clean APK..." -ForegroundColor Blue
    
    # Clean build if requested
    if ($CleanBuild) {
        Write-Host "Cleaning project..." -ForegroundColor Blue
        $cleanResult = & "$ProjectRoot\gradlew.bat" clean 2>&1
        if ($LASTEXITCODE -ne 0) {
            Write-Host "Clean failed: $cleanResult" -ForegroundColor Red
            return $false
        }
        Write-Host "Project cleaned" -ForegroundColor Green
    }
    
    # Build the APK
    $buildTask = if ($BuildType -eq "release") { "assembleRelease" } else { "assembleDebug" }
    Write-Host "Running gradle $buildTask..." -ForegroundColor Blue
    
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
            
            Write-Host "Clean APK built successfully!" -ForegroundColor Green
            Write-Host "Output: $outputApk" -ForegroundColor White
            Write-Host "Build Type: $BuildType" -ForegroundColor White
            
            # Display APK info
            $apkSize = [math]::Round((Get-Item $outputApk).Length / 1MB, 2)
            Write-Host "APK Size: $apkSize MB (unobfuscated)" -ForegroundColor Blue
            
            return $true
        } else {
            Write-Host "APK file not found after build!" -ForegroundColor Red
            Write-Host "Expected location: app\build\outputs\apk\$BuildType\" -ForegroundColor Yellow
            return $false
        }
    } else {
        Write-Host "Build failed!" -ForegroundColor Red
        Write-Host "Build output:" -ForegroundColor Yellow
        Write-Host $buildResult -ForegroundColor Gray
        return $false
    }
}

# Main execution
try {
    Write-Host ""
    Write-Host "Build Configuration:" -ForegroundColor Cyan
    Write-Host "Type: Clean (No Protection)" -ForegroundColor White
    Write-Host "Build Type: $BuildType" -ForegroundColor White
    Write-Host "Output Directory: $OutputPath" -ForegroundColor White
    Write-Host "Clean Build: $($CleanBuild -eq $true)" -ForegroundColor White
    
    # Get confirmation
    if (-not (Get-UserConfirmation "Build clean calculator APK with these settings?")) {
        Write-Host ""
        Write-Host "Build cancelled by user" -ForegroundColor Yellow
        exit 0
    }
    
    Write-Host ""
    Write-Host "Starting clean APK build process..." -ForegroundColor Blue
    
    # Build the clean APK
    $buildSuccess = Build-CleanAPK -BuildType $BuildType
    
    if ($buildSuccess) {
        Write-Host ""
        Write-Host "Clean APK build completed successfully!" -ForegroundColor Green
    } else {
        Write-Host ""
        Write-Host "Clean APK build failed!" -ForegroundColor Red
        exit 1
    }
    
} catch {
    Write-Host ""
    Write-Host "Error during build process: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Stack trace: $($_.ScriptStackTrace)" -ForegroundColor Gray
    exit 1
}

Write-Host ""
Write-Host "Clean Calculator APK Build Complete!" -ForegroundColor Blue
