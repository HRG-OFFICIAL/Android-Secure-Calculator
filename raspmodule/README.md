# RASP SDK

**Version:** 2.1.0  
**Author:** Security Engineering Team  
**Date:** September 2025  
**Status:** Production Ready

## Overview

RASP SDK is a comprehensive modular Android security SDK designed to protect applications from debugging, reverse engineering, tampering, and various security threats. The SDK provides multiple layers of protection including debugger detection, root detection, emulator detection, anti-hooking mechanisms, behavioral analysis, and secure data storage.

## Features

### Core Security Features
- **Debugger Detection**: Detects debugging tools and processes
- **Root Detection**: Identifies rooted devices and privilege escalation
- **Emulator Detection**: Recognizes virtual environments and emulators
- **Tamper Detection**: Verifies APK integrity and signature
- **Hook Detection**: Identifies runtime hooking frameworks
- **Behavioral Analysis**: Monitors for suspicious application behavior
- **Data Protection**: Built-in encryption and secure storage

### Technical Features
- **Native Components**: JNI layer for low-level protection
- **Modular Design**: Easy integration into existing Android projects
- **Configurable Responses**: Flexible threat response mechanisms
- **R8/ProGuard Integration**: Aggressive obfuscation support
- **Production Ready**: Comprehensive error handling and logging

## Installation

### Gradle Integration

Add the SDK to your project's `build.gradle.kts`:

```kotlin
dependencies {
    implementation(project(":raspmodule"))
}
```

## Quick Start

### Basic Integration

```kotlin
import com.example.raspsdk.RASP
import com.example.raspsdk.ThreatType

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize RASP SDK
        RASP.init(this, enableContinuousMonitoring = true)
        
        // Perform security check
        val securityReport = RASP.performSecurityCheck()
        
        // Handle threats
        if (securityReport.hasThreats()) {
            when {
                securityReport.debuggerDetected -> RASP.handleThreat(ThreatType.DEBUGGER)
                securityReport.rootDetected -> RASP.handleThreat(ThreatType.ROOT)
                securityReport.emulatorDetected -> RASP.handleThreat(ThreatType.EMULATOR)
                securityReport.tamperingDetected -> RASP.handleThreat(ThreatType.TAMPERING)
            }
            finishAffinity()
            return
        }
        
        // Continue with normal app initialization
        setContentView(R.layout.activity_main)
    }
}
```

## API Reference

### Main Interface

```kotlin
object RASP {
    // Initialize the SDK
    fun init(context: Context, enableContinuousMonitoring: Boolean = false)
    
    // Perform comprehensive security check
    fun performSecurityCheck(): SecurityReport
    
    // Individual detection methods
    fun isDebuggerAttached(): Boolean
    fun isDeviceRooted(): Boolean
    fun isRunningOnEmulator(): Boolean
    fun isApplicationTampered(): Boolean
    fun areHooksDetected(): Boolean
    fun isSuspiciousBehavior(): Boolean
    
    // Threat handling
    fun handleThreat(threatType: ThreatType)
    fun configureResponse(responseType: ResponseHandler.ResponseType)
    
    // Data protection
    fun getDataProtection(): DataProtection
    
    // Monitoring control
    fun stopMonitoring()
    fun cleanup()
}
```

### Security Report

```kotlin
data class SecurityReport(
    val debuggerDetected: Boolean,
    val rootDetected: Boolean,
    val emulatorDetected: Boolean,
    val tamperingDetected: Boolean,
    val hooksDetected: Boolean,
    val suspiciousBehavior: Boolean,
    val timestamp: Long
) {
    fun hasThreats(): Boolean
}
```

### Threat Types

```kotlin
enum class ThreatType {
    DEBUGGER,
    ROOT,
    EMULATOR,
    TAMPERING,
    HOOKS,
    SUSPICIOUS_BEHAVIOR
}
```

## Configuration

### ProGuard/R8 Configuration

Add to your `proguard-rules.pro`:

```proguard
# Keep essential RASP API surface
-keep class com.example.raspsdk.RASP {
    public static void init(android.content.Context, boolean);
    public static com.example.raspsdk.SecurityReport performSecurityCheck();
    public static void handleThreat(com.example.raspsdk.ThreatType);
}

# Keep data classes
-keep class com.example.raspsdk.SecurityReport { *; }
-keep class com.example.raspsdk.ThreatType { *; }

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}
```

## Best Practices

### Security Implementation
1. **Initialize Early**: Call `RASP.init()` in `onCreate()` before any other initialization
2. **Handle Failures**: Always check for initialization failures and handle them securely
3. **Continuous Monitoring**: Enable continuous monitoring for production builds
4. **Response Configuration**: Configure appropriate response types for your use case

### Testing
1. **Selective Testing**: Use individual feature testing for demonstrations
2. **Log Analysis**: Monitor logcat for security detection messages
3. **APK Verification**: Verify obfuscation using mapping files

## Troubleshooting

### Common Issues

1. **Native Library Loading**
   - Ensure NDK is properly installed
   - Check that native libraries are included in APK

2. **Build Issues**
   - Clean build: `.\gradlew clean assembleRelease`
   - Check ProGuard configuration
   - Verify dependency versions

3. **Runtime Issues**
   - Check logcat for error messages
   - Verify initialization sequence
   - Test on different devices

## License

This project is licensed under the MIT License - see the LICENSE file for details.

