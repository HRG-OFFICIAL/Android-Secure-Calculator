# Android Calculator

A modern, feature-rich calculator application for Android devices with a clean interface and powerful computation capabilities.

## ✨ Features

- **Basic Arithmetic**: Addition, subtraction, multiplication, and division
- **Scientific Functions**: Trigonometric functions, logarithms, exponentials, and more
- **Multiple Themes**: Light, dark, and AMOLED themes with Material Design 3 support
- **Calculation History**: Keep track of previous calculations with persistent storage
- **Expression Parsing**: Custom-built mathematical expression parser for accurate results
- **Responsive Design**: Optimized for both portrait and landscape orientations
- **Precision Control**: Configurable decimal precision and scientific notation
- **Intuitive UI**: Clean, modern interface with haptic feedback support

## 🔧 Technical Details

### Architecture
- **Language**: Kotlin
- **UI Framework**: Android Views with View Binding
- **Minimum SDK**: API 21 (Android 5.0)
- **Target SDK**: API 34 (Android 14)
- **Build System**: Gradle with Kotlin DSL

### Key Components
- **Custom Calculator Engine**: Built-in mathematical expression evaluator using BigDecimal for precision
- **Theme System**: Dynamic theming with support for system themes and custom color schemes
- **History Management**: JSON-based calculation history with configurable storage limits
- **Preference System**: Comprehensive settings with automatic migration support

## 📸 Screenshots

### Light Theme
<p align="center">
  <img src="app/src/main/res/drawable/screenshots/Screenshot_20250905_235435.png" alt="Light Mode Portrait" width="250">
</p>

### Dark Theme
<p align="center">
  <img src="app/src/main/res/drawable/screenshots/Screenshot_20250906_000106.png" alt="Dark Mode Portrait" width="250">
  <img src="app/src/main/res/drawable/screenshots/Screenshot_20250906_000121.png" alt="Dark Mode Settings" width="250">
</p>

### Scientific Mode & Features
<p align="center">
  <img src="app/src/main/res/drawable/screenshots/Screenshot_20250906_000718.png" alt="Scientific Mode" width="250">
  <img src="app/src/main/res/drawable/screenshots/Screenshot_20250906_000742.png" alt="History View" width="250">
  <img src="app/src/main/res/drawable/screenshots/Screenshot_20250906_001533.png" alt="Advanced Calculations" width="250">
</p>

## 🚀 Getting Started

### Prerequisites
- Android Studio Arctic Fox (2020.3.1) or later
- JDK 8 or higher
- Android SDK with API 34
- PowerShell 5.1+ (for testing APK builder script)
- Windows OS (for PowerShell script compatibility)

### Building the Project

1. Clone the repository:
   ```bash
   git clone <your-repository-url>
   cd Android_Calculator
   ```

2. Open in Android Studio or build from command line:
   ```bash
   ./gradlew assembleDebug
   ```

3. Install on device:
   ```bash
   ./gradlew installDebug
   ```

## 🧪 Testing

### Automated Tests
Run the test suite:
```bash
# Unit tests
./gradlew testDebugUnitTest

# Instrumented tests
./gradlew connectedAndroidTest

# All tests
./gradlew test
```

### Security Testing APK Builder

The project includes a PowerShell script that can generate testing APKs with various security configurations to help test anti-debugging protections. This is particularly useful for testing the app's behavior in secured environments.

#### Features

- **Selective Bypass Options**: Disable specific security checks while keeping others active
- **Force Detection Mode**: Forces all security checks to return `true` but allows the app to continue running
- **Build Type Selection**: Generate debug or release APKs
- **Source Protection**: Automatically restores source code after building
- **Install Option**: Automatically install generated APKs on connected devices

#### Usage Examples

```powershell
# Bypass emulator detection only (debug build)
.\build-test-apk.ps1 -BypassEmulator

# Bypass debugger detection only (release build)
.\build-test-apk.ps1 -BypassDebugger -BuildType release

# Bypass all security checks
.\build-test-apk.ps1 -BypassAll

# Force all detections to return true but continue running
.\build-test-apk.ps1 -ForceDetectAll

# Auto-install after building
.\build-test-apk.ps1 -BypassEmulator -InstallAfterBuild

# Skip confirmation prompt
.\build-test-apk.ps1 -BypassEmulator -Force
```

#### Generated APK Types

| APK Type | Purpose | Naming Convention |
|----------|---------|-------------------|
| Bypass Specific | Disable one or more security checks | `calculator-testing-noemu-nodbg-[build]-[timestamp].apk` |
| Bypass All | Disable all security checks | `calculator-testing-bypass-all-[build]-[timestamp].apk` |
| Force Detection | All checks return true but app continues | `calculator-testing-force-detect-all-[build]-[timestamp].apk` |

#### Script Parameters

| Parameter | Type | Description | Default |
|-----------|------|-------------|----------|
| `-BuildType` | String | Build type: `debug` or `release` | `debug` |
| `-BypassEmulator` | Switch | Disable emulator termination | `false` |
| `-BypassDebugger` | Switch | Disable debugger termination | `false` |
| `-BypassRoot` | Switch | Disable root termination | `false` |
| `-BypassTamper` | Switch | Disable tamper termination | `false` |
| `-BypassAll` | Switch | Disable all security checks | `false` |
| `-ForceDetectAll` | Switch | Force all detections true, no termination | `false` |
| `-OutputDir` | String | APK output directory | `builds` |
| `-Force` | Switch | Skip confirmation prompts | `false` |
| `-InstallAfterBuild` | Switch | Auto-install APK after building | `false` |

#### Security Testing Workflow

1. **Development Phase**: Use bypass modes to test app functionality
2. **Security Validation**: Use force detection mode to verify logging and UI responses
3. **Production Testing**: Test with full security enabled on target devices
4. **Threat Simulation**: Use specific bypass combinations to test edge cases

#### Important Notes

- ⚠️ **Source Code Safety**: The script automatically backs up and restores original source code
- 🔒 **Testing Only**: Generated APKs are for testing purposes and should not be distributed
- 📱 **Device Compatibility**: Release APKs are smaller and better for performance testing
- 🔍 **Debugging**: Debug APKs include additional logging and debugging symbols

## 🎨 Customization

The calculator supports extensive theming through:
- **Color schemes**: Defined in `values/colors.xml` and `values-night/colors.xml`
- **Typography**: Configurable font families and sizes
- **Button styles**: Customizable button appearances and behaviors

## 📱 Supported Features

### Mathematical Operations
- Basic arithmetic (+, -, ×, ÷)
- Parentheses for operation precedence
- Percentage calculations
- Square root and power operations
- Factorial calculations
- Trigonometric functions (sin, cos, tan)
- Logarithmic functions (ln, log, log₂)
- Constants (π, e)

### User Interface
- Material Design 3 components
- Adaptive layouts for different screen sizes
- Smooth animations and transitions
- Accessibility support

## 🛑 Security Features

This calculator app implements comprehensive anti-debugging and tamper detection measures:

### Anti-Debug Protections
- **Debugger Detection**: Detects various debugging tools and environments
- **Emulator Detection**: Identifies if the app is running in an emulated environment
- **Root Detection**: Checks for rooted devices and superuser access
- **Tampering Detection**: Validates app integrity and prevents modification

### Security Implementation
- **Native Library Integration**: Utilizes native code for enhanced security checks
- **Continuous Monitoring**: Real-time threat detection during app execution
- **Graceful Termination**: Secure app shutdown when threats are detected
- **Comprehensive Logging**: Detailed security event logging for analysis

### Testing and Development
The included testing framework allows developers to:
- Test individual security components
- Simulate threat environments for validation
- Verify security behavior without compromising production builds

## 🔒 Privacy

This calculator app:
- **No network permissions**: All calculations are performed locally
- **No data collection**: No user data is transmitted or stored externally
- **Minimal permissions**: Only requests essential permissions for functionality

## 🤝 Contributing

Contributions are welcome! Please feel free to submit pull requests or open issues for bugs and feature requests.

### Development Guidelines
- Follow Kotlin coding conventions
- Write unit tests for new functionality
- Update documentation for significant changes
- Test on multiple screen sizes and orientations

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🏢 Project Structure

```
├── app/
│   ├── src/main/
│   │   ├── cpp/               # Native library (security)
│   │   ├── java/com/android/calculator/
│   │   │   ├── activities/    # UI activities
│   │   │   ├── calculator/   # Core calculation logic
│   │   │   ├── history/      # History management
│   │   │   ├── services/     # Background services
│   │   │   └── util/        # Utility classes
│   │   └── res/           # Resources (layouts, strings, themes)
│   └── src/test/          # Unit and integration tests
├── antidebug/             # Anti-debug library module
├── build-test-apk.ps1     # Testing APK builder script
├── builds/                # Generated testing APKs
└── gradle/                # Gradle wrapper and configuration
```
