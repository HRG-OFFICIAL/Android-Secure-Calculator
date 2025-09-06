# R8/ProGuard Mapping Files Guide

## 📍 **Location**
```
app/build/outputs/mapping/release/
```

## 📋 **Files Overview**

| File | Size | Purpose |
|------|------|---------|
| **mapping.txt** | 16.7 MB | **Main obfuscation mapping** (original → obfuscated names) |
| **usage.txt** | 2.8 MB | Code that was kept during shrinking |
| **resources.txt** | 1.8 MB | Resource shrinking report |
| **configuration.txt** | 40 KB | ProGuard configuration used |
| **seeds.txt** | 319 KB | Entry points that were kept |

## 🎯 **Key File: mapping.txt**

**Purpose:** Maps original class/method names to obfuscated versions

**Example Entries:**
```
com.example.antidebug.AntiDebug -> o.a:
    boolean isDebuggerAttached() -> c
    boolean isDeviceRooted() -> d
    boolean isRunningOnEmulator() -> e

com.example.antidebug.DebuggerDetection -> o.b:
    boolean isDebuggerConnected() -> a
    boolean checkTracerPid() -> b
```

## 📂 **Full File Paths**
```
C:\Users\harsh\AndroidStudioProjects\Android_Calculator\app\build\outputs\mapping\release\mapping.txt
C:\Users\harsh\AndroidStudioProjects\Android_Calculator\app\build\outputs\mapping\release\usage.txt
C:\Users\harsh\AndroidStudioProjects\Android_Calculator\app\build\outputs\mapping\release\resources.txt
C:\Users\harsh\AndroidStudioProjects\Android_Calculator\app\build\outputs\mapping\release\configuration.txt
C:\Users\harsh\AndroidStudioProjects\Android_Calculator\app\build\outputs\mapping\release\seeds.txt
```

## 🔍 **How to Use Mapping Files**

### **1. Crash Deobfuscation**
Use mapping.txt to convert obfuscated stack traces back to readable format:
```bash
# Using retrace tool
retrace.bat mapping.txt obfuscated_stacktrace.txt
```

### **2. Verify Obfuscation**
Check mapping.txt to confirm security-sensitive classes were obfuscated:
```bash
# Search for AntiDebug classes
findstr "antidebug" mapping.txt
```

### **3. Google Play Console**
Upload mapping.txt to Google Play Console for crash reporting deobfuscation

## 🚨 **Security Warning**
**NEVER** distribute mapping.txt files publicly - they reveal the complete obfuscation mapping and can be used to reverse engineer your protected APK!

## 📊 **Statistics**
- **Total mapping entries:** ~50,000+ class/method mappings
- **AntiDebug SDK classes:** Fully obfuscated
- **Calculator classes:** Preserved (as per ProGuard rules)
- **Android framework:** Not obfuscated (system classes)
