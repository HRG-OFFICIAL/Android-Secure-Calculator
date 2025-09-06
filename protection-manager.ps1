# AntiDebugSDK Protection Manager
# Helper script to manage protection application and removal

param(
    [string]$Action,            # apply, remove, status, help
    [string]$Level,             # minimal, full, debug, production
    [string]$ResponseType,      # Response type for threats
    [switch]$Force,             # Force actions without confirmation  
    [switch]$KeepBackups        # Keep backup files
)

Write-Host "🛡️ AntiDebugSDK Protection Manager" -ForegroundColor Cyan
Write-Host "===================================" -ForegroundColor Cyan

$ProjectRoot = Get-Location
$MetadataFile = "$ProjectRoot\.protection-metadata.json"
$BackupDir = "$ProjectRoot\.protection-backups"

# Function to show current status
function Show-Status {
    Write-Host "`n📊 Current Protection Status:" -ForegroundColor Cyan
    
    if (Test-Path $MetadataFile) {
        $metadata = Get-Content $MetadataFile -Raw | ConvertFrom-Json
        
        Write-Host "  🟢 Protection: APPLIED" -ForegroundColor Green
        Write-Host "  📅 Applied At: $($metadata.AppliedAt)" -ForegroundColor White
        Write-Host "  🔧 Level: $($metadata.Level)" -ForegroundColor White
        Write-Host "  📋 Response Type: $($metadata.Configuration.ResponseType)" -ForegroundColor White
        Write-Host "  📦 Backup: $($metadata.BackupTimestamp)" -ForegroundColor White
        
        Write-Host "`n  🛡️ Active Protections:" -ForegroundColor Yellow
        $config = $metadata.Configuration
        
        $protections = @(
            @{ Name = "Debugger Detection"; Enabled = $config.EnableDebuggerDetection; Icon = "🔍" }
            @{ Name = "Root Detection"; Enabled = $config.EnableRootDetection; Icon = "🔓" }
            @{ Name = "Emulator Detection"; Enabled = $config.EnableEmulatorDetection; Icon = "📱" }
            @{ Name = "Tamper Detection"; Enabled = $config.EnableTamperDetection; Icon = "🔒" }
            @{ Name = "Hook Detection"; Enabled = $config.EnableHookDetection; Icon = "🎣" }
            @{ Name = "Behavioral Detection"; Enabled = $config.EnableBehavioralDetection; Icon = "📊" }
            @{ Name = "Data Protection"; Enabled = $config.EnableDataProtection; Icon = "🔐" }
            @{ Name = "Continuous Monitoring"; Enabled = $config.EnableMonitoring; Icon = "👁️" }
        )
        
        $protections | ForEach-Object {
            $status = if ($_.Enabled) { "✅" } else { "❌" }
            Write-Host "     $status $($_.Icon) $($_.Name)" -ForegroundColor White
        }
        
    } else {
        Write-Host "  🔴 Protection: NOT APPLIED" -ForegroundColor Red
        Write-Host "  ℹ️  No protection metadata found" -ForegroundColor Blue
    }
    
    # Show backup status
    if (Test-Path $BackupDir) {
        $backupCount = (Get-ChildItem $BackupDir -File).Count
        Write-Host "`n  📁 Backups: $backupCount files available" -ForegroundColor Blue
    } else {
        Write-Host "`n  📁 Backups: None available" -ForegroundColor Gray
    }
}

# Function to show usage help
function Show-Help {
    Write-Host "`n📖 Usage Instructions:" -ForegroundColor Cyan
    Write-Host "======================" -ForegroundColor Cyan
    
    Write-Host "`n🎯 Basic Commands:" -ForegroundColor Yellow
    Write-Host "  .\protection-manager.ps1 status                    # Show current protection status"
    Write-Host "  .\protection-manager.ps1 apply -Level minimal      # Apply minimal protection"
    Write-Host "  .\protection-manager.ps1 apply -Level full         # Apply full protection"
    Write-Host "  .\protection-manager.ps1 remove                    # Remove all protection"
    Write-Host "  .\protection-manager.ps1 help                      # Show this help"
    
    Write-Host "`n🔧 Protection Levels:" -ForegroundColor Yellow
    Write-Host "  minimal     - Basic debugger detection + data protection"
    Write-Host "  full        - All protection features enabled"
    Write-Host "  debug       - Debug-friendly settings (LOG_ONLY responses)"
    Write-Host "  production  - Production settings (DELAYED_EXIT responses)"
    
    Write-Host "`n⚡ Response Types:" -ForegroundColor Yellow
    Write-Host "  LOG_ONLY        - Only log threats (safe for testing)"
    Write-Host "  DELAYED_EXIT    - Exit app after delay when threats detected"
    Write-Host "  IMMEDIATE_EXIT  - Exit app immediately"
    Write-Host "  CRASH_APP       - Simulate crash when threats detected"
    Write-Host "  FAKE_SCREEN     - Show misleading content"
    Write-Host "  DISABLE_FEATURES - Disable app features"
    
    Write-Host "`n📝 Detailed Examples:" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "# Apply minimal protection for development testing:"
    Write-Host ".\protection-manager.ps1 apply -Level debug" -ForegroundColor Green
    Write-Host ""
    Write-Host "# Apply full protection with custom response:"
    Write-Host ".\protection-manager.ps1 apply -Level full -ResponseType DELAYED_EXIT" -ForegroundColor Green
    Write-Host ""
    Write-Host "# Apply production-ready protection:"
    Write-Host ".\protection-manager.ps1 apply -Level production" -ForegroundColor Green
    Write-Host ""
    Write-Host "# Remove protection and keep backups:"
    Write-Host ".\protection-manager.ps1 remove -KeepBackups" -ForegroundColor Green
    Write-Host ""
    Write-Host "# Force remove without confirmation:"
    Write-Host ".\protection-manager.ps1 remove -Force" -ForegroundColor Green
    
    Write-Host "`n🛠️ Direct Script Usage:" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "# Apply protection directly:"
    Write-Host ".\apply-protection.ps1 -Full -ResponseType LOG_ONLY" -ForegroundColor Green
    Write-Host ".\apply-protection.ps1 -Minimal" -ForegroundColor Green
    Write-Host ".\apply-protection.ps1 -Debug" -ForegroundColor Green
    Write-Host ".\apply-protection.ps1 -Production" -ForegroundColor Green
    Write-Host ""
    Write-Host "# Remove protection directly:"
    Write-Host ".\remove-protection.ps1" -ForegroundColor Green
    Write-Host ".\remove-protection.ps1 -Force -KeepBackups" -ForegroundColor Green
    Write-Host ".\remove-protection.ps1 -RestoreFromBackup -BackupTimestamp 2025-01-06_12-30-45" -ForegroundColor Green
    
    Write-Host "`n📋 Protection Features:" -ForegroundColor Yellow
    Write-Host "  🔍 Debugger Detection    - Detects attached debuggers and tracers"
    Write-Host "  🔓 Root Detection        - Detects rooted/jailbroken devices"
    Write-Host "  📱 Emulator Detection    - Detects if running on emulators"
    Write-Host "  🔒 Tamper Detection      - Detects app modifications and tampering"
    Write-Host "  🎣 Hook Detection        - Detects Frida, Xposed, and other hooks"
    Write-Host "  📊 Behavioral Detection  - Detects suspicious runtime behavior"
    Write-Host "  🔐 Data Protection       - Encrypts sensitive data storage"
    Write-Host "  👁️  Continuous Monitoring - Background security monitoring"
    
    Write-Host "`n⚠️  Important Notes:" -ForegroundColor Yellow
    Write-Host "  • Always test with -Level debug first"
    Write-Host "  • Backups are automatically created before applying protection"
    Write-Host "  • Use LOG_ONLY response type during development"
    Write-Host "  • Run 'gradle sync' after applying/removing protection"
    Write-Host "  • The calculator app is for testing only"
}

# Function to apply protection
function Apply-Protection {
    param([string]$Level, [string]$Response)
    
    Write-Host "`n🚀 Applying Protection..." -ForegroundColor Yellow
    
    $params = @()
    
    switch ($Level.ToLower()) {
        "minimal" { $params += "-Minimal" }
        "full" { $params += "-Full" }
        "debug" { $params += "-Debug" }
        "production" { $params += "-Production" }
        default {
            Write-Host "❌ Invalid protection level: $Level" -ForegroundColor Red
            Write-Host "Valid levels: minimal, full, debug, production" -ForegroundColor Yellow
            return
        }
    }
    
    if ($Response) {
        $params += "-ResponseType"
        $params += $Response
    }
    
    if ($Force) {
        $params += "-Force"
    }
    
    # Execute apply-protection script
    $scriptPath = "$ProjectRoot\apply-protection.ps1"
    if (Test-Path $scriptPath) {
        & $scriptPath @params
    } else {
        Write-Host "❌ apply-protection.ps1 not found!" -ForegroundColor Red
    }
}

# Function to remove protection
function Remove-Protection {
    Write-Host "`n🗑️ Removing Protection..." -ForegroundColor Yellow
    
    $params = @()
    
    if ($Force) {
        $params += "-Force"
    }
    
    if ($KeepBackups) {
        $params += "-KeepBackups"
    }
    
    # Execute remove-protection script
    $scriptPath = "$ProjectRoot\remove-protection.ps1"
    if (Test-Path $scriptPath) {
        & $scriptPath @params
    } else {
        Write-Host "❌ remove-protection.ps1 not found!" -ForegroundColor Red
    }
}

# Main execution
switch ($Action.ToLower()) {
    "status" {
        Show-Status
    }
    
    "apply" {
        if (-not $Level) {
            Write-Host "❌ Protection level required for apply action" -ForegroundColor Red
            Write-Host "Usage: .\protection-manager.ps1 apply -Level <minimal|full|debug|production>" -ForegroundColor Yellow
            exit 1
        }
        Apply-Protection -Level $Level -Response $ResponseType
    }
    
    "remove" {
        Remove-Protection
    }
    
    "help" {
        Show-Help
    }
    
    "" {
        # No action specified, show status and brief help
        Show-Status
        Write-Host "`n💡 Use '.\protection-manager.ps1 help' for detailed usage instructions" -ForegroundColor Blue
        Write-Host "💡 Use '.\protection-manager.ps1 apply -Level debug' for quick testing" -ForegroundColor Blue
    }
    
    default {
        Write-Host "❌ Unknown action: $Action" -ForegroundColor Red
        Write-Host "Valid actions: status, apply, remove, help" -ForegroundColor Yellow
        exit 1
    }
}

Write-Host ""
