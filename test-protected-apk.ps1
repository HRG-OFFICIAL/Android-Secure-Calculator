# Test Protected Calculator APK in Android Studio AVD
# This script helps you test the anti-debug functionality

param(
    [Parameter(Mandatory=$false)]
    [string]$ApkPath = "builds\calculator-protected-obfuscated-release-2025-09-06_14-53.apk",
    
    [Parameter(Mandatory=$false)]
    [switch]$CleanInstall
)

Write-Host "🧪 Protected Calculator APK Testing Script" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""

# Check if APK exists
if (-not (Test-Path $ApkPath)) {
    Write-Host "❌ APK not found: $ApkPath" -ForegroundColor Red
    Write-Host "Available APK files:" -ForegroundColor Yellow
    Get-ChildItem "builds\*.apk" | Format-Table Name, Length, LastWriteTime
    exit 1
}

$ApkInfo = Get-Item $ApkPath
Write-Host "📱 APK Information:" -ForegroundColor Green
Write-Host "   File: $($ApkInfo.Name)" -ForegroundColor Yellow
Write-Host "   Size: $([math]::Round($ApkInfo.Length / 1MB, 2)) MB" -ForegroundColor Yellow
Write-Host "   Modified: $($ApkInfo.LastWriteTime)" -ForegroundColor Yellow
Write-Host ""

Write-Host "🚀 Testing Instructions:" -ForegroundColor Green
Write-Host "========================" -ForegroundColor Green
Write-Host ""

Write-Host "1. START AVD:" -ForegroundColor Cyan
Write-Host "   • Open Android Studio" -ForegroundColor White
Write-Host "   • Go to Tools → AVD Manager" -ForegroundColor White
Write-Host "   • Start an existing AVD or create new one" -ForegroundColor White
Write-Host "   • Wait for emulator to fully boot" -ForegroundColor White
Write-Host ""

Write-Host "2. INSTALL APK:" -ForegroundColor Cyan
Write-Host "   Method A (Drag & Drop):" -ForegroundColor Yellow
Write-Host "   • Drag this file to the running emulator:" -ForegroundColor White
Write-Host "   • $ApkPath" -ForegroundColor Green
Write-Host ""
Write-Host "   Method B (Command Line):" -ForegroundColor Yellow
Write-Host "   • Open terminal in Android SDK platform-tools directory" -ForegroundColor White
Write-Host "   • Run: adb install `"$((Get-Item $ApkPath).FullName)`"" -ForegroundColor Green
Write-Host ""

Write-Host "3. EXPECTED BEHAVIOR (Anti-Debug Protection):" -ForegroundColor Cyan
Write-Host "   ✅ APK installs successfully" -ForegroundColor Green
Write-Host "   ✅ App icon appears in launcher" -ForegroundColor Green
Write-Host "   ⚠️  App SHOULD DETECT EMULATOR and terminate immediately" -ForegroundColor Yellow
Write-Host "   📱 Check logcat for security messages" -ForegroundColor Yellow
Write-Host ""

Write-Host "4. LOGCAT MONITORING:" -ForegroundColor Cyan
Write-Host "   • In Android Studio: View → Tool Windows → Logcat" -ForegroundColor White
Write-Host "   • Filter by 'AntiDebug' tag" -ForegroundColor White
Write-Host "   • Look for messages like:" -ForegroundColor White
Write-Host "     - 'Security Report - Emulator: true'" -ForegroundColor Green
Write-Host "     - 'Security threat detected! Terminating application.'" -ForegroundColor Green
Write-Host ""

Write-Host "5. TESTING DIFFERENT APK VERSIONS:" -ForegroundColor Cyan
Write-Host "   Compare with clean APK for different behavior:" -ForegroundColor White
if (Test-Path "builds\calculator-clean-release-*.apk") {
    $CleanApk = Get-ChildItem "builds\calculator-clean-release-*.apk" | Select-Object -First 1
    Write-Host "   Clean APK: $($CleanApk.Name)" -ForegroundColor Green
    Write-Host "   → Should work normally in emulator" -ForegroundColor Green
} else {
    Write-Host "   No clean APK found for comparison" -ForegroundColor Yellow
}
Write-Host "   Protected APK: $($ApkInfo.Name)" -ForegroundColor Red
Write-Host "   → Should terminate due to emulator detection" -ForegroundColor Red
Write-Host ""

Write-Host "6. TROUBLESHOOTING:" -ForegroundColor Cyan
Write-Host "   If app works normally (does not terminate):" -ForegroundColor Yellow
Write-Host "   • Check if this is the correct protected APK" -ForegroundColor White
Write-Host "   • Verify anti-debug code is included" -ForegroundColor White
Write-Host "   • Check logcat for any error messages" -ForegroundColor White
Write-Host ""

Write-Host "📊 APK COMPARISON:" -ForegroundColor Cyan
Write-Host "==================" -ForegroundColor Cyan
Get-ChildItem "builds\*.apk" | Sort-Object Name | Format-Table @{
    Label="APK Type"; Expression={
        if ($_.Name -like "*clean*") { "Clean" }
        elseif ($_.Name -like "*protected*" -and $_.Name -like "*obfuscated*") { "Protected+R8" }
        elseif ($_.Name -like "*protected*") { "Protected" }
        else { "Other" }
    }
}, @{
    Label="Size (MB)"; Expression={[math]::Round($_.Length / 1MB, 2)}
}, @{
    Label="File Name"; Expression={$_.Name}
}

Write-Host ""
Write-Host "READY TO TEST!" -ForegroundColor Green
Write-Host "The protected APK should demonstrate emulator detection by terminating immediately." -ForegroundColor Yellow
Write-Host "This proves the anti-debug protection is working correctly!" -ForegroundColor Yellow
