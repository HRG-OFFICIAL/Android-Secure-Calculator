# Remove AntiDebugSDK Protection Script
# This script intelligently removes security protections from the calculator app

param(
    [switch]$Force,              # Force removal without confirmation
    [switch]$KeepBackups,        # Keep backup files after removal
    [switch]$RestoreFromBackup,  # Restore from specific backup
    [string]$BackupTimestamp,    # Specific backup timestamp to restore from
    [switch]$CleanAll            # Remove all protection-related files
)

Write-Host "🗑️ AntiDebugSDK Protection Removal Script" -ForegroundColor Red
Write-Host "==========================================" -ForegroundColor Red

# Define paths
$ProjectRoot = Get-Location
$AppBuildGradle = "$ProjectRoot\app\build.gradle.kts"
$AppManifest = "$ProjectRoot\app\src\main\AndroidManifest.xml"
$MainActivity = "$ProjectRoot\app\src\main\java\com\android\calculator\activities\MainActivity.kt"
$SettingsGradle = "$ProjectRoot\settings.gradle.kts"
$MetadataFile = "$ProjectRoot\.protection-metadata.json"
$BackupDir = "$ProjectRoot\.protection-backups"

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

# Function to show current protection status
function Show-CurrentStatus {
    Write-Host "`n📊 Current Protection Status:" -ForegroundColor Cyan
    
    if (Test-Path $MetadataFile) {
        $metadata = Get-Content $MetadataFile -Raw | ConvertFrom-Json
        Write-Host "  🟢 Protection: APPLIED" -ForegroundColor Green
        Write-Host "  📅 Applied At: $($metadata.AppliedAt)" -ForegroundColor White
        Write-Host "  🔧 Level: $($metadata.Level)" -ForegroundColor White
        Write-Host "  📦 Backup Available: $($metadata.BackupTimestamp)" -ForegroundColor White
    } else {
        Write-Host "  🔴 Protection: NOT APPLIED" -ForegroundColor Red
        Write-Host "  ℹ️  No protection metadata found" -ForegroundColor Blue
        return $false
    }
    
    return $true
}

# Function to list available backups
function Show-AvailableBackups {
    if (Test-Path $BackupDir) {
        $backupFiles = Get-ChildItem $BackupDir -File | Sort-Object LastWriteTime -Descending
        
        if ($backupFiles.Count -gt 0) {
            Write-Host "`n📁 Available Backups:" -ForegroundColor Cyan
            
            # Group by timestamp
            $timestamps = $backupFiles | ForEach-Object { 
                if ($_.Name -match '\.(\d{4}-\d{2}-\d{2}_\d{2}-\d{2}-\d{2})$') {
                    $matches[1]
                }
            } | Sort-Object -Unique -Descending
            
            $timestamps | ForEach-Object {
                Write-Host "  📦 $_ ($(Get-Date $_.Replace('_', ' ').Replace('-', '/') -Format 'MMM dd, yyyy HH:mm')))" -ForegroundColor White
            }
            
            return $timestamps
        }
    }
    
    Write-Host "`n📁 No backup files found" -ForegroundColor Gray
    return @()
}

# Function to restore from backup
function Restore-FromBackup {
    param([string]$Timestamp)
    
    if (-not (Test-Path $BackupDir)) {
        Write-Host "❌ Backup directory not found!" -ForegroundColor Red
        return $false
    }
    
    $backupFiles = @{
        "build.gradle.kts" = "$BackupDir\build.gradle.kts.$Timestamp"
        "AndroidManifest.xml" = "$BackupDir\AndroidManifest.xml.$Timestamp"
        "MainActivity.kt" = "$BackupDir\MainActivity.kt.$Timestamp"
        "settings.gradle.kts" = "$BackupDir\settings.gradle.kts.$Timestamp"
    }
    
    $targetFiles = @{
        "build.gradle.kts" = $AppBuildGradle
        "AndroidManifest.xml" = $AppManifest
        "MainActivity.kt" = $MainActivity
        "settings.gradle.kts" = $SettingsGradle
    }
    
    Write-Host "🔄 Restoring from backup timestamp: $Timestamp" -ForegroundColor Yellow
    
    foreach ($fileType in $backupFiles.Keys) {
        $backupFile = $backupFiles[$fileType]
        $targetFile = $targetFiles[$fileType]
        
        if (Test-Path $backupFile) {
            Copy-Item $backupFile $targetFile -Force
            Write-Host "  ✅ Restored $fileType" -ForegroundColor Green
        } else {
            Write-Host "  ⚠️  Backup not found for $fileType" -ForegroundColor Yellow
        }
    }
    
    return $true
}

# Function to remove AntiDebugSDK references from settings.gradle.kts
function Clean-SettingsGradle {
    Write-Host "🔧 Cleaning settings.gradle.kts..." -ForegroundColor Yellow
    
    if (Test-Path $SettingsGradle) {
        $content = Get-Content $SettingsGradle -Raw
        $originalContent = $content
        
        # Remove anti-debug-sdk module reference
        $content = $content -replace '\n\s*include\s*\(\s*"?:anti-debug-sdk"?\s*\)', ''
        $content = $content -replace 'include\s*\(\s*"?:anti-debug-sdk"?\s*\)\s*\n?', ''
        
        if ($content -ne $originalContent) {
            Set-Content $SettingsGradle $content.TrimEnd() -NoNewline
            Write-Host "  ✅ Removed anti-debug-sdk module reference" -ForegroundColor Green
        } else {
            Write-Host "  ℹ️  No changes needed in settings.gradle.kts" -ForegroundColor Blue
        }
    }
}

# Function to remove AntiDebugSDK references from app build.gradle.kts
function Clean-AppBuildGradle {
    Write-Host "🔧 Cleaning app build.gradle.kts..." -ForegroundColor Yellow
    
    if (Test-Path $AppBuildGradle) {
        $content = Get-Content $AppBuildGradle -Raw
        $originalContent = $content
        
        # Remove SDK dependency
        $content = $content -replace '\s*implementation\(project\(":anti-debug-sdk"\)\)[^\n]*\n?', ''
        
        if ($content -ne $originalContent) {
            Set-Content $AppBuildGradle $content.TrimEnd() -NoNewline
            Write-Host "  ✅ Removed AntiDebugSDK dependency" -ForegroundColor Green
        } else {
            Write-Host "  ℹ️  No changes needed in build.gradle.kts" -ForegroundColor Blue
        }
    }
}

# Function to remove INTERNET permission from AndroidManifest.xml (if added by protection)
function Clean-AndroidManifest {
    Write-Host "🔧 Cleaning AndroidManifest.xml..." -ForegroundColor Yellow
    
    if (Test-Path $AppManifest) {
        $content = Get-Content $AppManifest -Raw
        $originalContent = $content
        
        # Only remove INTERNET permission if it was added after USE_FULL_SCREEN_INTENT
        # This preserves any existing INTERNET permission while removing the one we added
        $content = $content -replace '(<uses-permission android:name="android\.permission\.USE_FULL_SCREEN_INTENT" />)\s*\n\s*<uses-permission android:name="android\.permission\.INTERNET" />', '$1'
        
        if ($content -ne $originalContent) {
            Set-Content $AppManifest $content.TrimEnd() -NoNewline
            Write-Host "  ✅ Removed added INTERNET permission" -ForegroundColor Green
        } else {
            Write-Host "  ℹ️  No changes needed in AndroidManifest.xml" -ForegroundColor Blue
        }
    }
}

# Function to remove protection code from MainActivity.kt
function Clean-MainActivity {
    Write-Host "🔧 Cleaning MainActivity.kt..." -ForegroundColor Yellow
    
    if (Test-Path $MainActivity) {
        $content = Get-Content $MainActivity -Raw
        $originalContent = $content
        
        # Remove imports
        $content = $content -replace 'import com\.example\.antidebug\.[^\n]*\n', ''
        
        # Remove initialization call
        $content = $content -replace '\s*// Initialize AntiDebugSDK Protection \(Generated\)[^\n]*\n\s*initializeAntiDebugProtection\(\)\s*\n', ''
        
        # Remove protection methods (find and remove the entire block)
        $content = $content -replace '(\n\s*\/\*\*\s*\n\s*\*\s*Initialize and configure AntiDebugSDK protections[\s\S]*?\*\/\s*\n\s*private fun initializeAntiDebugProtection\(\)[\s\S]*?\n\s*\}\s*)', ''
        
        # Remove helper methods
        $helperMethods = @(
            'performDebuggerCheck', 'performRootCheck', 'performEmulatorCheck', 
            'performTamperCheck', 'performHookCheck', 'performBehavioralCheck', 
            'setupDataProtection', 'cleanupAntiDebugProtection'
        )
        
        foreach ($method in $helperMethods) {
            $content = $content -replace "(\n\s*private fun $method\(\)[\s\S]*?\n\s*\}\s*)", ''
        }
        
        # Remove cleanup method override if it exists
        $content = $content -replace '(\n\s*\/\*\*\s*\n\s*\*\s*Cleanup AntiDebugSDK resources[\s\S]*?\*\/\s*\n\s*override fun cleanupAntiDebugProtection\(\)[\s\S]*?\n\s*\}\s*)', ''
        
        # Clean up any double blank lines
        $content = $content -replace '\n\s*\n\s*\n', "`n`n"
        
        if ($content -ne $originalContent) {
            Set-Content $MainActivity $content.TrimEnd() -NoNewline
            Write-Host "  ✅ Removed protection code from MainActivity.kt" -ForegroundColor Green
        } else {
            Write-Host "  ℹ️  No changes needed in MainActivity.kt" -ForegroundColor Blue
        }
    }
}

# Function to clean all protection-related files
function Clean-AllProtectionFiles {
    Write-Host "🧹 Cleaning all protection-related files..." -ForegroundColor Yellow
    
    $filesToRemove = @(
        $MetadataFile,
        "$ProjectRoot\.protection-state.json"
    )
    
    foreach ($file in $filesToRemove) {
        if (Test-Path $file) {
            Remove-Item $file -Force
            Write-Host "  ✅ Removed $(Split-Path $file -Leaf)" -ForegroundColor Green
        }
    }
    
    # Remove backup directory if not keeping backups
    if (-not $KeepBackups -and (Test-Path $BackupDir)) {
        $backupCount = (Get-ChildItem $BackupDir -File).Count
        if (Get-UserConfirmation "Remove $backupCount backup files?") {
            Remove-Item $BackupDir -Recurse -Force
            Write-Host "  ✅ Removed backup directory" -ForegroundColor Green
        }
    }
}

# Main execution
try {
    # Check if protection is currently applied
    $protectionApplied = Show-CurrentStatus
    
    if ($RestoreFromBackup) {
        # Handle specific backup restoration
        if (-not $BackupTimestamp) {
            $availableBackups = Show-AvailableBackups
            if ($availableBackups.Count -eq 0) {
                Write-Host "`n❌ No backups available for restoration!" -ForegroundColor Red
                exit 1
            }
            
            Write-Host "`n🔍 Please specify a backup timestamp:" -ForegroundColor Yellow
            Write-Host "Usage: .\remove-protection.ps1 -RestoreFromBackup -BackupTimestamp YYYY-MM-DD_HH-MM-SS" -ForegroundColor White
            exit 1
        }
        
        if (Get-UserConfirmation "Restore from backup timestamp: $BackupTimestamp") {
            $success = Restore-FromBackup -Timestamp $BackupTimestamp
            if ($success) {
                Write-Host "`n✅ Successfully restored from backup!" -ForegroundColor Green
                if (Test-Path $MetadataFile) {
                    Remove-Item $MetadataFile -Force
                }
                Write-Host "📋 Don't forget to run 'gradle sync' in Android Studio" -ForegroundColor Blue
            } else {
                Write-Host "`n❌ Failed to restore from backup!" -ForegroundColor Red
                exit 1
            }
        }
        return
    }
    
    if (-not $protectionApplied) {
        Write-Host "`n💡 No protection currently applied. Nothing to remove." -ForegroundColor Blue
        
        if ((Test-Path $BackupDir) -and -not $KeepBackups) {
            Show-AvailableBackups
            if (Get-UserConfirmation "Remove leftover backup files?") {
                Remove-Item $BackupDir -Recurse -Force
                Write-Host "✅ Removed backup files" -ForegroundColor Green
            }
        }
        return
    }
    
    # Show available backups
    Show-AvailableBackups
    
    # Get confirmation
    if (-not (Get-UserConfirmation "Remove AntiDebugSDK protection from calculator app?")) {
        Write-Host "`n❌ Protection removal cancelled" -ForegroundColor Yellow
        return
    }
    
    Write-Host "`n🚀 Removing protection..." -ForegroundColor Yellow
    
    # Check if we have backup to restore from
    $metadata = Get-Content $MetadataFile -Raw | ConvertFrom-Json
    $backupTimestamp = $metadata.BackupTimestamp
    
    if ($backupTimestamp -and (Test-Path "$BackupDir\MainActivity.kt.$backupTimestamp")) {
        Write-Host "🔄 Restoring from backup (recommended method)..." -ForegroundColor Yellow
        $success = Restore-FromBackup -Timestamp $backupTimestamp
        if (-not $success) {
            Write-Host "⚠️  Backup restoration failed, using manual cleanup..." -ForegroundColor Yellow
        }
    } else {
        Write-Host "⚠️  No backup found, using manual cleanup..." -ForegroundColor Yellow
    }
    
    # Manual cleanup (as fallback or if no backup)
    Clean-SettingsGradle
    Clean-AppBuildGradle
    Clean-AndroidManifest
    Clean-MainActivity
    
    # Clean protection files
    if ($CleanAll) {
        Clean-AllProtectionFiles
    } else {
        if (Test-Path $MetadataFile) {
            Remove-Item $MetadataFile -Force
            Write-Host "✅ Removed protection metadata" -ForegroundColor Green
        }
    }
    
    # Handle backup cleanup
    if ((Test-Path $BackupDir) -and -not $KeepBackups) {
        if (Get-UserConfirmation "Remove backup files?") {
            Remove-Item $BackupDir -Recurse -Force
            Write-Host "✅ Removed backup files" -ForegroundColor Green
        } else {
            Write-Host "📁 Backup files preserved in .protection-backups/" -ForegroundColor Blue
        }
    } elseif (Test-Path $BackupDir) {
        Write-Host "📁 Backup files preserved in .protection-backups/" -ForegroundColor Blue
    }
    
    Write-Host "`n🎉 Protection removal completed!" -ForegroundColor Green
    
    Write-Host "`n📖 Next Steps:" -ForegroundColor Cyan
    Write-Host "  1. Run 'gradle sync' in Android Studio" -ForegroundColor White
    Write-Host "  2. Clean and rebuild your project" -ForegroundColor White
    Write-Host "  3. Verify the app runs normally" -ForegroundColor White
    Write-Host "  4. Use 'apply-protection.ps1' to reapply protection when needed" -ForegroundColor White
    
} catch {
    Write-Host "`n❌ Error removing protection: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "🔄 You can try restoring from backups in .protection-backups/" -ForegroundColor Yellow
    exit 1
}

Write-Host "`n🧹 AntiDebugSDK Protection Removed Successfully! 🧹" -ForegroundColor Green
