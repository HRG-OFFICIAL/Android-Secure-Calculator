# COMPREHENSIVE OBFUSCATION CONFIGURATION
# This configuration implements all major obfuscation techniques

# ===== CORE PROTECTIONS =====

# Keep only essential Android framework methods (minimal keep rules)
-keepclassmembers class com.android.calculator.activities.MainActivity {
    public void onCreate(android.os.Bundle);
    public void onDestroy();
    public void onResume();
    public void onPause();
    public void onBackPressed();
    public void onConfigurationChanged(android.content.res.Configuration);
}

# Keep MINIMAL AntiDebug API surface (only what's actually used)
-keep class com.example.antidebug.AntiDebug {
    public static void init(android.content.Context, boolean);
    public static com.example.antidebug.SecurityReport performSecurityCheck();
    public static void handleThreat(com.example.antidebug.ThreatType);
}

# Keep only essential data classes
-keep class com.example.antidebug.SecurityReport { 
    public boolean debuggerDetected;
    public boolean rootDetected;
    public boolean emulatorDetected;
    public boolean tamperingDetected;
    public long timestamp;
}

-keep class com.example.antidebug.ThreatType { 
    public static com.example.antidebug.ThreatType DEBUGGER;
    public static com.example.antidebug.ThreatType ROOT;
    public static com.example.antidebug.ThreatType EMULATOR;
    public static com.example.antidebug.ThreatType TAMPERING;
}

# ===== 1. STATIC CODE OBFUSCATION =====

# 1.1 Identifier Renaming - Maximum obfuscation
-repackageclasses 'a'
-allowaccessmodification
-overloadaggressively
-mergeinterfacesaggressively

# 1.2 Control Flow Obfuscation
-optimizations !code/simplification/cast,!code/simplification/field,!code/simplification/arithmetic
-optimizations !code/simplification/variable,!code/simplification/arithmetic,!field/*,!class/merging/*

# 1.3 String Encryption
-adaptclassstrings
-adaptresourcefilecontents **.properties,**.xml,**.txt

# 1.4 Junk Code / Dead Code Insertion
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

# 1.5 Method Inlining / Outlining
-optimizationpasses 5
-mergeinterfacesaggressively
-allowaccessmodification

# 1.6 Metadata Stripping / Debug Removal
-keepattributes !SourceFile,!LineNumberTable,!LocalVariableTable,!LocalVariableTypeTable
-renamesourcefileattribute SourceFile
-dontwarn **

# ===== 2. RESOURCE & MANIFEST OBFUSCATION =====

# 2.1 Resource Name Mangling / Shrinking
-adaptresourcefilenames **.properties,**.gif,**.jpg,**.png,**.xml
-adaptresourcefilecontents **.properties,META-INF/MANIFEST.MF

# 2.2 Encrypted Resources / Assets
-keep class com.android.calculator.util.ResourceObfuscator { *; }
-keep class com.android.calculator.util.AssetEncryption { *; }

# 2.3 AndroidManifest Obfuscation
-keep class com.android.calculator.util.ManifestObfuscator { *; }

# ===== 3. RUNTIME / DYNAMIC OBFUSCATION =====

# 3.1 Runtime Class Decryption / Dynamic Loading
-keep class com.android.calculator.util.DynamicClassLoader { *; }
-keep class com.android.calculator.util.AdvancedObfuscator { *; }

# 3.2 Native Loaders / Bootstrap Stubs
-keep class com.android.calculator.util.NativeObfuscator { *; }
-keep class com.android.calculator.util.NativeLoader { *; }

# 3.3 Code Virtualization / VM-based Obfuscation
-keep class com.android.calculator.util.CodeVirtualizer { *; }

# 3.4 Dynamic Code Generation
-keep class com.android.calculator.util.DynamicCodeGenerator { *; }

# ===== 4. DATA MASKING =====

# 4.1 Static Data Masking (SDM)
-keep class com.android.calculator.util.DataMasking { *; }
-keep class com.android.calculator.util.SubstitutionMasking { *; }
-keep class com.android.calculator.util.ShufflingMasking { *; }
-keep class com.android.calculator.util.RedactionMasking { *; }
-keep class com.android.calculator.util.EncryptionMasking { *; }

# 4.2 Dynamic Data Masking (DDM)
-keep class com.android.calculator.util.DynamicMasking { *; }

# 4.3 Format-Preserving Encryption (FPE)
-keep class com.android.calculator.util.FormatPreservingEncryption { *; }

# 4.4 Data Redaction
-keep class com.android.calculator.util.DataRedaction { *; }

# ===== 5. ENHANCED OBFUSCATION TECHNIQUES =====

# 5.1 String Obfuscation
-keep class com.android.calculator.util.StringObfuscator { *; }
-keep class com.android.calculator.util.StringCrypto { *; }
-keep class com.android.calculator.util.EncryptedStrings { *; }
-keep class com.android.calculator.util.ObfuscatedStrings { *; }

# 5.2 Control Flow Obfuscation
-keep class com.android.calculator.util.ControlFlowObfuscator { *; }
-keep class com.android.calculator.util.FlowObfuscator { *; }

# 5.3 Reflection Indirection
-keep class com.android.calculator.util.ReflectionIndirection { *; }

# 5.4 Integrity Verification
-keep class com.android.calculator.util.IntegrityVerifier { *; }

# ===== 6. ADVANCED OPTIMIZATIONS =====

# 6.1 Aggressive Shrinking
-dontwarn **
-ignorewarnings
-optimizations !code/simplification/cast,!code/simplification/field

# 6.2 Single Optimization Pass (More reliable)
-optimizationpasses 1

# 6.3 Remove Unused Code Aggressively
-allowaccessmodification
-overloadaggressively
-mergeinterfacesaggressively

# 6.4 Package Restructuring (Minimal)
-repackageclasses 'a'

# 6.5 Remove Debug Info
-keepattributes !SourceFile,!LineNumberTable,!LocalVariableTable

# 6.6 Shrink Resources Aggressively
-adaptresourcefilenames **.properties,**.gif,**.jpg
-adaptresourcefilecontents **.properties,META-INF/MANIFEST.MF

# ===== 7. SELECTIVE KEEP RULES (Only what's needed) =====

# Keep only essential Android classes (not all)
-keep class android.app.Activity { *; }
-keep class android.app.Application { *; }
-keep class android.content.Context { *; }
-keep class android.os.Bundle { *; }
-keep class android.view.View { *; }
-keep class android.widget.** { *; }

# Keep only used AndroidX classes
-keep class androidx.appcompat.app.AppCompatActivity { *; }
-keep class androidx.constraintlayout.widget.ConstraintLayout { *; }
-keep class androidx.recyclerview.widget.** { *; }
-keep class androidx.preference.** { *; }

# Keep only essential Kotlin classes
-keep class kotlin.Metadata { *; }
-keep class kotlin.jvm.internal.** { *; }

# ===== 8. REMOVE UNNECESSARY KEEP RULES =====

# DON'T keep all Android classes (let R8 shrink them)
# -keep class android.** { *; }  # REMOVED

# DON'T keep all AndroidX classes (let R8 shrink them)  
# -keep class androidx.** { *; }  # REMOVED

# DON'T keep all AntiDebug classes (let R8 shrink unused ones)
# -keep class com.example.antidebug.** { *; }  # REMOVED

# ===== 9. FINAL OPTIMIZATIONS =====

# Remove unused methods and fields
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
}

# Remove unused string constants
-adaptclassstrings

# Final shrinking pass
-printmapping mapping.txt
-printseeds seeds.txt
-printusage usage.txt

# ===== 10. SECURITY MEASURES =====

# Additional security measures
-dontskipnonpubliclibraryclassmembers
-forceprocessing

# Aggressive string obfuscation
-adaptclassstrings
-adaptresourcefilecontents **.properties,**.xml,**.txt

# Remove all debug information
-keepattributes !SourceFile,!LineNumberTable,!LocalVariableTable,!LocalVariableTypeTable
-renamesourcefileattribute SourceFile

# Missing classes warnings suppression (generated by R8)
-dontwarn javax.annotation.Nullable
-dontwarn javax.annotation.concurrent.GuardedBy

# ===== 11. NATIVE CODE OBFUSCATION =====

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep JNI methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# ===== 12. REFLECTION PROTECTION =====

# Keep classes that might be accessed via reflection
-keep class com.android.calculator.activities.** { *; }
-keep class com.android.calculator.calculator.** { *; }
-keep class com.android.calculator.history.** { *; }

# Keep obfuscation package structure
-keep class com.android.calculator.obfuscation.** { *; }

# ===== 13. SERIALIZATION PROTECTION =====

# Keep serializable classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ===== 14. ANNOTATION PROTECTION =====

# Keep annotations
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# ===== 15. FINAL SECURITY MEASURES =====

# Remove unused code
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

# Remove unused string constants
-adaptclassstrings

# Final shrinking pass
-printmapping mapping.txt
-printseeds seeds.txt
-printusage usage.txt

# ===== 16. CUSTOM OBFUSCATION RULES =====

# Custom obfuscation for specific classes
-keep class com.android.calculator.util.** { *; }

# Obfuscate all utility classes
-keep class com.android.calculator.util.StringObfuscator { *; }
-keep class com.android.calculator.util.ControlFlowObfuscator { *; }
-keep class com.android.calculator.util.StringCrypto { *; }
-keep class com.android.calculator.util.AdvancedObfuscator { *; }
-keep class com.android.calculator.util.DataMasking { *; }
-keep class com.android.calculator.util.ResourceObfuscator { *; }
-keep class com.android.calculator.util.NativeObfuscator { *; }

# ===== 17. PERFORMANCE OPTIMIZATIONS =====

# Optimize for performance
-optimizations !code/simplification/cast,!code/simplification/field
-optimizationpasses 1

# Remove unused code
-allowaccessmodification
-overloadaggressively
-mergeinterfacesaggressively

# ===== 18. FINAL CONFIGURATION =====

# Final obfuscation settings
-repackageclasses 'a'
-allowaccessmodification
-overloadaggressively
-mergeinterfacesaggressively
-optimizations !code/simplification/variable,!code/simplification/arithmetic,!field/*,!class/merging/*

# Additional security measures
-dontskipnonpubliclibraryclassmembers
-forceprocessing

# Aggressive string obfuscation
-adaptclassstrings
-adaptresourcefilecontents **.properties,**.xml,**.txt

# Remove all debug information
-keepattributes !SourceFile,!LineNumberTable,!LocalVariableTable,!LocalVariableTypeTable
-renamesourcefileattribute SourceFile

# Missing classes warnings suppression (generated by R8)
-dontwarn javax.annotation.Nullable
-dontwarn javax.annotation.concurrent.GuardedBy

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
