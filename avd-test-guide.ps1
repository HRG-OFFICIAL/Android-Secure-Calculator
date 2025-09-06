# AVD Testing Guide for Protected Calculator APK

Write-Host "Protected Calculator APK Testing Guide" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# Show available APKs
Write-Host "Available APK Files:" -ForegroundColor Green
Get-ChildItem "builds\*.apk" | Format-Table Name, @{Label="Size(MB)";Expression={[math]::Round($_.Length/1MB,2)}}, LastWriteTime

Write-Host ""
Write-Host "TESTING INSTRUCTIONS:" -ForegroundColor Yellow
Write-Host "===================" -ForegroundColor Yellow
Write-Host ""

Write-Host "1. START AVD:" -ForegroundColor Cyan
Write-Host "   - Open Android Studio" -ForegroundColor White
Write-Host "   - Tools -> AVD Manager" -ForegroundColor White  
Write-Host "   - Start or create an AVD" -ForegroundColor White
Write-Host "   - Wait for boot to complete" -ForegroundColor White
Write-Host ""

Write-Host "2. INSTALL PROTECTED APK:" -ForegroundColor Cyan
$protectedApk = Get-ChildItem "builds\*protected*final*.apk" | Select-Object -First 1
if (-not $protectedApk) {
    $protectedApk = Get-ChildItem "builds\*protected*16kb*.apk" | Select-Object -First 1
    if (-not $protectedApk) {
        $protectedApk = Get-ChildItem "builds\*protected*obfuscated*.apk" | Select-Object -First 1
    }
}
if ($protectedApk) {
    Write-Host "   File: $($protectedApk.Name)" -ForegroundColor Green
    Write-Host "   Size: $([math]::Round($protectedApk.Length/1MB,2)) MB" -ForegroundColor Green
    Write-Host "   Drag this APK file to the running emulator" -ForegroundColor White
} else {
    Write-Host "   Protected APK not found!" -ForegroundColor Red
}
Write-Host ""

Write-Host "3. EXPECTED BEHAVIOR:" -ForegroundColor Cyan
Write-Host "   - APK installs successfully" -ForegroundColor Green
Write-Host "   - App icon appears in launcher" -ForegroundColor Green
Write-Host "   - When launched: APP SHOULD TERMINATE IMMEDIATELY" -ForegroundColor Red
Write-Host "   - This proves emulator detection is working" -ForegroundColor Yellow
Write-Host ""

Write-Host "4. CHECK LOGCAT:" -ForegroundColor Cyan  
Write-Host "   - In Android Studio: View -> Tool Windows -> Logcat" -ForegroundColor White
Write-Host "   - Filter by tag: AntiDebug" -ForegroundColor White
Write-Host "   - Look for: 'Security Report - Emulator: true'" -ForegroundColor Green
Write-Host "   - Look for: 'Security threat detected! Terminating'" -ForegroundColor Green
Write-Host ""

Write-Host "5. COMPARE WITH CLEAN APK:" -ForegroundColor Cyan
$cleanApk = Get-ChildItem "builds\*clean*.apk" | Select-Object -First 1
if ($cleanApk) {
    Write-Host "   Clean APK: $($cleanApk.Name)" -ForegroundColor Green  
    Write-Host "   - Should work normally in emulator" -ForegroundColor Green
    Write-Host "   - Install and test for comparison" -ForegroundColor White
}
Write-Host ""

Write-Host "TESTING PROVES:" -ForegroundColor Yellow
Write-Host "- Anti-debug protection is active" -ForegroundColor Green
Write-Host "- Emulator detection is working" -ForegroundColor Green  
Write-Host "- APK is properly secured" -ForegroundColor Green
Write-Host ""

Write-Host "Ready to test! Launch the AVD and install the protected APK." -ForegroundColor Cyan
