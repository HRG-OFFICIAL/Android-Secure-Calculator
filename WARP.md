# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Project Overview

This is a sophisticated Android Calculator application with advanced security features, multi-variant builds, and comprehensive anti-debugging protection. The project uses Kotlin, Material Design 3, and includes a custom mathematical expression parser with precise BigDecimal calculations.

## Build System & Commands

### Gradle Commands
```bash
# Standard Android builds
.\gradlew assembleDebug
.\gradlew assembleRelease
.\gradlew installDebug

# Run tests
.\gradlew testDebugUnitTest
.\gradlew connectedAndroidTest
.\gradlew test

# Clean builds
.\gradlew clean
```

### Build Variants
The project supports multiple build flavors:
- **standard**: Default flavor for regular development
- **aggressive**: Ultra-obfuscated build with aggressive ProGuard rules

### PowerShell Build System
The project includes an advanced PowerShell-based build system for creating protected/unprotected APK variants:

```powershell
# Build clean APK (development)
.\build-clean-apk-fixed.ps1 -BuildType debug -Force

# Build protected APK (with AntiDebugSDK)  
.\build-protected-apk-fixed.ps1 -BuildType release -CleanBuild -Force

# Unified APK manager
.\apk-manager-fixed.ps1 -Mode both -BuildType debug -Force

# Ultra-obfuscated builds
.\build-ultra-obfuscated.ps1
```

**Key PowerShell Scripts:**
- `apk-manager-fixed.ps1`: Central build management
- `build-clean-apk-fixed.ps1`: Builds without security protection
- `build-protected-apk-fixed.ps1`: Builds with full AntiDebugSDK integration
- `build-ultra-obfuscated.ps1`: Maximum obfuscation builds

## Architecture Overview

### Core Components

**Main Application Structure:**
- `MainActivity`: Primary calculator interface with anti-debug integration
- `Calculator`: Core mathematical computation engine using BigDecimal for precision
- `Expression`: Custom mathematical expression parser supporting complex operations
- `HistoryAdapter`: Calculation history management with JSON persistence

**Security Layer (AntiDebugSDK):**
- Multi-layer security detection (debugger, root, emulator, tampering)
- Native C++ components for low-level protection
- Response system with configurable threat handling
- Data protection with AES encryption and secure storage

**Theme System:**
- Dynamic theme switching (Light, Dark, AMOLED)
- Material Design 3 implementation
- System theme integration with preference migration

### Module Structure
```
app/                           # Main calculator application
├── activities/               # UI Activities (MainActivity, SettingsActivity)
├── calculator/              # Core calculation logic and parser
├── history/                 # History management
├── services/                # Background services (QuickTile)
└── util/                    # Utilities (preferences, themes, crypto)

anti-debug-sdk/              # Security library module  
├── AntiDebug.kt            # Main SDK API
├── DebuggerDetection.kt    # Debugger detection
├── RootDetection.kt        # Root detection
├── EmulatorDetection.kt    # Emulator detection
├── TamperDetection.kt      # Integrity checks
└── cpp/                    # Native security layer
```

### Key Architecture Patterns

**Coroutine-Based Async Operations:**
All security checks and calculations run on background threads using Kotlin coroutines with proper Main/IO dispatcher switching.

**View Binding:**
The project uses Android View Binding throughout for type-safe view access.

**Preference Migration System:**
Implements automatic preference migration between app versions with `MyPreferenceMigrator`.

**Expression Evaluation Engine:**
Custom mathematical parser supporting scientific functions, precedence rules, and precision control.

## Security Features

### AntiDebugSDK Integration
The calculator includes comprehensive security protection:

- **Debugger Detection**: Multiple techniques including ptrace, TracerPid monitoring, signal handling
- **Root Detection**: SU binary detection, root app scanning, system property analysis  
- **Emulator Detection**: Build fingerprinting, QEMU detection, network analysis
- **Tampering Detection**: APK signature verification, DEX integrity checks
- **Hook Detection**: Frida, Xposed, and Substrate detection
- **Data Protection**: AES encryption with Android Keystore

### Build Security
- **R8/ProGuard Obfuscation**: Aggressive code obfuscation for protected builds
- **Obfuscation Dictionary**: 200+ misleading class/method names
- **Native Protection**: C++ JNI layer for low-level security checks
- **Certificate Pinning**: APK signature validation

## Development Workflow

### Standard Development Flow
1. Use clean builds for daily development: `.\build-clean-apk-fixed.ps1 -Force`
2. Test functionality with standard Android debugging tools
3. Use protected builds for security validation: `.\build-protected-apk-fixed.ps1 -BuildType release -Force`
4. Compare clean vs protected APK behavior

### Testing Security Features
The project includes specialized APK building for testing different security scenarios:
- Bypass specific security checks for development
- Force detection modes for UI testing
- Compare protected vs unprotected builds side-by-side

### Build Artifacts
Generated APKs are organized in the `builds/` directory:
- Clean builds: ~5.7-7.2 MB (no protection)
- Protected builds: ~2.4-9.4 MB (with security + obfuscation)
- Ultra-obfuscated: ~2.4 MB (maximum protection)

## Key Configuration Files

**Gradle Configuration:**
- `build.gradle.kts`: Main app build configuration with flavor variants
- `gradle/libs.versions.toml`: Version catalog for dependencies
- Java 21 target, Android API 21-34 support

**Security Configuration:**
- `proguard-rules.pro`: Standard obfuscation rules
- `proguard-ultra-aggressive-fixed.pro`: Maximum obfuscation
- `obfuscation-dictionary.txt`: Custom misleading names

**Native Build:**
- `anti-debug-sdk/src/main/cpp/CMakeLists.txt`: Native security layer build
- NDK with ARM64, ARMv7, x86, x86_64 support
- 16KB page size compatibility for Android 15+

## Testing

### Unit Tests
- `ExpressionUnitTest.kt`: Mathematical expression parser tests
- `NumberFormatterTest.kt`: Number formatting and precision tests
- `MainActivityTests.kt`: UI interaction tests

### Security Testing
Use the PowerShell build system to create different security test scenarios:
- Test individual security components
- Validate threat detection and response
- Performance testing with/without protection

## Important Notes for AI Development

- **Source Code Protection**: The build system automatically backs up and restores source files during protected builds
- **API Compatibility**: Project uses API level 21 minimum, targets API 34
- **Native Dependencies**: Requires NDK and CMake for anti-debug native components  
- **PowerShell Scripts**: Windows-specific build automation - requires PowerShell 5.1+
- **Security-First**: All security features should be tested in isolated APK builds
- **Precision Mathematics**: Uses BigDecimal for all calculations to avoid floating-point errors

