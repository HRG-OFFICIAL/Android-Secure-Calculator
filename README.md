# Android Calculator with Security Features

A modern, feature-rich calculator application for Android devices with comprehensive security protection and anti-debugging capabilities.

## Features

### Calculator Features
- **Basic Arithmetic**: Addition, subtraction, multiplication, and division
- **Scientific Functions**: Trigonometric functions, logarithms, exponentials, and more
- **Multiple Themes**: Light, dark, and AMOLED themes with Material Design 3 support
- **Calculation History**: Keep track of previous calculations with persistent storage
- **Expression Parsing**: Custom-built mathematical expression parser for accurate results
- **Responsive Design**: Optimized for both portrait and landscape orientations
- **Precision Control**: Configurable decimal precision and scientific notation
- **Intuitive UI**: Clean, modern interface with haptic feedback support

### Security Features
- **AntiDebug Protection**: Detects and prevents debugging attempts
- **Root Detection**: Identifies and blocks rooted devices
- **Emulator Detection**: Prevents execution on emulated environments
- **Tamper Detection**: Verifies APK integrity and detects modifications
- **Code Obfuscation**: Maximum obfuscation with single-letter meaningless names
- **Selective Testing**: Individual security feature testing capabilities
- **Continuous Monitoring**: Background security monitoring

## Technical Details

### Architecture
- **Language**: Kotlin
- **UI Framework**: Android Views with View Binding
- **Minimum SDK**: API 21 (Android 5.0)
- **Target SDK**: API 34 (Android 14)
- **Build System**: Gradle with Kotlin DSL

### Security Implementation
- **AntiDebug SDK**: Custom security library with native components
- **R8/ProGuard**: Aggressive code shrinking and obfuscation
- **Native Protection**: JNI layer for low-level security checks
- **Reflection Obfuscation**: Advanced obfuscation techniques
- **String Encryption**: Critical strings encrypted at build time

### Key Components
- **Custom Calculator Engine**: Built-in mathematical expression evaluator using BigDecimal for precision
- **Theme System**: Dynamic theming with support for system themes and custom color schemes
- **History Management**: JSON-based calculation history with configurable storage limits
- **Preference System**: Comprehensive settings with automatic migration support
- **Security Framework**: Modular security detection and response system

## Building the Application

### Prerequisites
- Android Studio Arctic Fox or later
- JDK 21 or later
- Android SDK API 21-34
- NDK (for native security components)

### Build Commands

#### Quick Build
```bash
.\gradlew assembleaggressiveRelease
```

#### Clean Build
```bash
.\gradlew clean assembleaggressiveRelease
```

#### Using Build Script
```powershell
# Build with clean
.\build-release.ps1 -Clean

# Build without clean
.\build-release.ps1
```

### Build Variants

#### Standard Build
- Basic obfuscation
- Standard security features
- Debuggable

#### Aggressive Build (Recommended)
- Maximum obfuscation
- All security features enabled
- Optimized for production
- R8 shrinking enabled

## Security Configuration

### Selective Testing Mode

The application supports selective testing of individual security features for demonstration purposes.

#### Configuration
Edit `app/src/main/java/com/android/calculator/util/SelectiveTestingConfig.kt`:

```kotlin
// Default: All features enabled (production mode)
val currentConfig: TestingConfig = TestScenarios.allFeatures()

// Individual testing scenarios:
val currentConfig: TestingConfig = TestScenarios.debuggerOnly()    // Test debugger only
val currentConfig: TestingConfig = TestScenarios.emulatorOnly()    // Test emulator only
val currentConfig: TestingConfig = TestScenarios.rootOnly()        // Test root only
val currentConfig: TestingConfig = TestScenarios.tamperingOnly()   // Test tampering only
```

#### Testing Scenarios
- **allFeatures()**: All security features enabled (production)
- **debuggerOnly()**: Only debugger detection enabled
- **emulatorOnly()**: Only emulator detection enabled
- **rootOnly()**: Only root detection enabled
- **tamperingOnly()**: Only tampering detection enabled
- **noFeatures()**: All security features disabled (development)

### Obfuscation Configuration

The application uses aggressive obfuscation with the following features:
- **Class Renaming**: All classes renamed to single letters
- **Method Renaming**: All methods renamed to single letters
- **Field Renaming**: All fields renamed to single letters
- **Package Flattening**: Package hierarchy flattened to single level
- **String Encryption**: Critical strings encrypted
- **Control Flow Obfuscation**: Complex control flow patterns

## AntiDebug SDK

### Overview
The AntiDebug SDK provides comprehensive security protection through multiple detection mechanisms:

- **Debugger Detection**: Detects debugging tools and processes
- **Root Detection**: Identifies rooted devices and privilege escalation
- **Emulator Detection**: Recognizes virtual environments and emulators
- **Tamper Detection**: Verifies APK integrity and signature
- **Hook Detection**: Identifies runtime hooking frameworks
- **Behavioral Analysis**: Monitors for suspicious application behavior

### Integration
The SDK is automatically integrated into the aggressive build variant:

```kotlin
// In MainActivity.kt
AntiDebug.init(this, enableContinuousMonitoring = true)
val securityReport = AntiDebug.performSecurityCheck()

if (securityReport.hasThreats()) {
    // Handle security threats
    finishAffinity()
}
```

### Native Components
The SDK includes native C++ components for low-level security checks:
- **Process monitoring**
- **System call interception**
- **Memory protection**
- **Hardware fingerprinting**

## Project Structure

```
app/
├── src/main/java/com/android/calculator/
│   ├── activities/
│   │   └── MainActivity.kt              # Main activity with security integration
│   ├── util/
│   │   ├── SelectiveTestingConfig.kt    # Testing configuration
│   │   ├── StringCrypto.kt             # String encryption utilities
│   │   └── StringObfuscator.kt         # Obfuscation utilities
│   └── ...
├── proguard-optimized-shrinking.pro    # R8/ProGuard configuration
└── build.gradle.kts                    # Build configuration

anti-debug-sdk/
├── src/main/java/com/example/antidebug/
│   ├── AntiDebug.kt                    # Main SDK interface
│   ├── DebuggerDetection.kt            # Debugger detection
│   ├── RootDetection.kt                # Root detection
│   ├── EmulatorDetection.kt            # Emulator detection
│   ├── TamperDetection.kt              # Tamper detection
│   └── ...
└── src/main/cpp/
    └── anti-debug-native.cpp           # Native security components
```

## Security Analysis

### Obfuscation Effectiveness
- **Class Names**: 100% obfuscated to single letters
- **Method Names**: 95% obfuscated (framework methods preserved)
- **Field Names**: 100% obfuscated to single letters
- **Package Structure**: Flattened to single level
- **String Constants**: Encrypted and obfuscated

### R8 Shrinking
- **Code Reduction**: ~60% of unused code removed
- **Resource Shrinking**: Unused resources eliminated
- **Dead Code Elimination**: Unreachable code removed
- **Optimization**: Multiple optimization passes applied

### APK Analysis
- **Final Size**: ~2.8 MB (optimized)
- **Security Features**: All enabled by default
- **Obfuscation Level**: Maximum
- **Shrinking**: R8 working optimally

## Troubleshooting

### Build Issues
1. **Clean Build**: Use `.\gradlew clean assembleaggressiveRelease`
2. **NDK Issues**: Ensure NDK is properly installed
3. **Memory Issues**: Increase Gradle heap size in `gradle.properties`

### Security Testing
1. **Selective Testing**: Use individual feature testing for demonstrations
2. **Log Analysis**: Check logcat for security detection messages
3. **APK Verification**: Verify obfuscation using mapping files

### Performance
1. **Build Time**: Aggressive builds take longer due to obfuscation
2. **APK Size**: Optimized shrinking reduces final size
3. **Runtime**: Security checks have minimal performance impact

## License

This project is licensed under the MIT License - see the LICENSE file for details.