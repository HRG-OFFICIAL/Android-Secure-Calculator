# OPTIMIZED R8 SHRINKING CONFIGURATION
# This configuration maximizes R8 shrinking while maintaining security

# ===== CORE PROTECTIONS =====

# Keep ONLY essential Android framework methods (minimal keep rules)
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

# ===== OPTIMIZED SHRINKING TECHNIQUES =====

# 1. AGGRESSIVE SHRINKING (Let R8 remove unused code)
-dontwarn **
-ignorewarnings
-optimizations !code/simplification/cast,!code/simplification/field

# 2. SINGLE OPTIMIZATION PASS (More reliable)
-optimizationpasses 1

# 3. REMOVE UNUSED CODE AGGRESSIVELY
-allowaccessmodification
-overloadaggressively
-mergeinterfacesaggressively

# 4. PACKAGE RESTRUCTURING (Minimal)
-repackageclasses 'a'

# 5. REMOVE DEBUG INFO
-keepattributes !SourceFile,!LineNumberTable,!LocalVariableTable

# 6. SHRINK RESOURCES AGGRESSIVELY
-adaptresourcefilenames **.properties,**.gif,**.jpg
-adaptresourcefilecontents **.properties,META-INF/MANIFEST.MF

# ===== SELECTIVE KEEP RULES (Only what's needed) =====

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

# ===== REMOVE UNNECESSARY KEEP RULES =====

# DON'T keep all Android classes (let R8 shrink them)
# -keep class android.** { *; }  # REMOVED

# DON'T keep all AndroidX classes (let R8 shrink them)  
# -keep class androidx.** { *; }  # REMOVED

# DON'T keep all AntiDebug classes (let R8 shrink unused ones)
# -keep class com.example.antidebug.** { *; }  # REMOVED

# ===== FINAL OPTIMIZATIONS =====

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
