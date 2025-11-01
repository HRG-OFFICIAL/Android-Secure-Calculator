#include <jni.h>
#include <string>
#include <unistd.h>
#include <sys/ptrace.h>
#include <sys/prctl.h>
#include <signal.h>
#include <errno.h>
#include <android/log.h>
#include <sys/mman.h>
#include <fcntl.h>
#include <sys/stat.h>
#include <time.h>
#include <stdlib.h>

#define LOG_TAG "RASPNative"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Global variables for signal handling
static volatile sig_atomic_t debugger_detected = 0;
static volatile sig_atomic_t timing_anomaly = 0;  // Reserved for future timing checks

// Signal handler for SIGTRAP (debugger breakpoints)
void sigtrap_handler(int signum) {
    if (signum == SIGTRAP) {
        debugger_detected = 1;
        LOGW("SIGTRAP signal received - debugger detected");
    }
}

// Signal handler for SIGSTOP/SIGCONT (process manipulation)
void sigstop_handler(int signum) {
    if (signum == SIGSTOP || signum == SIGCONT) {
        debugger_detected = 1;
        LOGW("Process manipulation signal received - debugger detected");
    }
}

// Timing check function
long long get_time_ns() {
    struct timespec ts;
    clock_gettime(CLOCK_MONOTONIC, &ts);
    return ts.tv_sec * 1000000000LL + ts.tv_nsec;
}

extern "C" {

// DebuggerDetection native methods
JNIEXPORT jboolean JNICALL
Java_com.example.raspsdk_DebuggerDetection_nativePtraceCheck(JNIEnv *env, jclass clazz) {
    (void)env;    // Suppress unused parameter warning
    (void)clazz;  // Suppress unused parameter warning
    
    // Try to attach to ourselves with ptrace
    // If a debugger is already attached, this will fail
    if (ptrace(PTRACE_TRACEME, 0, 1, 0) == -1) {
        if (errno == EPERM) {
            LOGW("ptrace self-attach failed - debugger already attached");
            return JNI_TRUE;
        }
    }
    
    // If successful, detach immediately
    ptrace(PTRACE_DETACH, 0, 1, 0);
    return JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_com.example.raspsdk_DebuggerDetection_nativeSignalCheck(JNIEnv *env, jclass clazz) {
    (void)env;    // Suppress unused parameter warning
    (void)clazz;  // Suppress unused parameter warning
    
    // Set up signal handlers for debugger detection
    signal(SIGTRAP, sigtrap_handler);
    signal(SIGSTOP, sigstop_handler);
    signal(SIGCONT, sigstop_handler);
    
    // Check if any signals were received
    if (debugger_detected) {
        LOGW("Debugger signal detected");
        return JNI_TRUE;
    }
    
    return JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_com.example.raspsdk_DebuggerDetection_nativeTimingCheck(JNIEnv *env, jclass clazz) {
    (void)env;    // Suppress unused parameter warning
    (void)clazz;  // Suppress unused parameter warning
    
    long long start_time = get_time_ns();
    
    // Perform simple computation
    volatile int result = 0;
    for (int i = 0; i < 1000; i++) {
        result += i * i;
    }
    
    long long end_time = get_time_ns();
    long long duration = end_time - start_time;
    
    // If execution took too long (> 1ms), might be debugged
    if (duration > 1000000) {  // 1ms in nanoseconds
        LOGW("Timing check failed - execution too slow: %lld ns", duration);
        return JNI_TRUE;
    }
    
    return JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_com.example.raspsdk_DebuggerDetection_nativeDebuggerCheck(JNIEnv *env, jclass clazz) {
    (void)env;    // Suppress unused parameter warning
    (void)clazz;  // Suppress unused parameter warning
    
    // Check /proc/self/status for TracerPid
    FILE *fp = fopen("/proc/self/status", "r");
    if (fp == NULL) {
        return JNI_FALSE;
    }
    
    char line[256];
    while (fgets(line, sizeof(line), fp)) {
        if (strncmp(line, "TracerPid:", 10) == 0) {
            int tracer_pid = atoi(line + 10);
            fclose(fp);
            if (tracer_pid != 0) {
                LOGW("TracerPid is non-zero: %d", tracer_pid);
                return JNI_TRUE;
            }
            return JNI_FALSE;
        }
    }
    
    fclose(fp);
    return JNI_FALSE;
}

// RootDetection native methods
JNIEXPORT jboolean JNICALL
Java_com.example.raspsdk_RootDetection_nativeRootCheck(JNIEnv *env, jclass clazz) {
    (void)env;    // Suppress unused parameter warning
    (void)clazz;  // Suppress unused parameter warning
    
    // Check for SU binary using access()
    const char* su_paths[] = {
        "/system/bin/su",
        "/system/xbin/su",
        "/system/sbin/su",
        "/vendor/bin/su",
        "/sbin/su",
        NULL
    };
    
    for (int i = 0; su_paths[i] != NULL; i++) {
        if (access(su_paths[i], F_OK) == 0) {
            LOGW("SU binary found at: %s", su_paths[i]);
            return JNI_TRUE;
        }
    }
    
    // Check if /system is mounted as writable
    FILE *fp = fopen("/proc/mounts", "r");
    if (fp != NULL) {
        char line[1024];
        while (fgets(line, sizeof(line), fp)) {
            if (strstr(line, "/system") && strstr(line, "rw")) {
                LOGW("System partition mounted as read-write");
                fclose(fp);
                return JNI_TRUE;
            }
        }
        fclose(fp);
    }
    
    return JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_com.example.raspsdk_RootDetection_nativePropertyCheck(JNIEnv *env, jclass clazz) {
    (void)env;    // Suppress unused parameter warning
    (void)clazz;  // Suppress unused parameter warning
    
    // This is a placeholder - in real implementation, you'd check system properties
    // through Android's property system or by reading /system/build.prop
    return JNI_FALSE;
}

// HookDetection native methods
JNIEXPORT jboolean JNICALL
Java_com.example.raspsdk_HookDetection_nativeHookCheck(JNIEnv *env, jclass clazz) {
    (void)env;    // Suppress unused parameter warning
    (void)clazz;  // Suppress unused parameter warning
    
    // Check /proc/self/maps for suspicious libraries
    FILE *fp = fopen("/proc/self/maps", "r");
    if (fp == NULL) {
        return JNI_FALSE;
    }
    
    char line[1024];
    const char* suspicious_libs[] = {
        "frida", "xposed", "substrate", "cydia", "libhook", NULL
    };
    
    while (fgets(line, sizeof(line), fp)) {
        for (int i = 0; suspicious_libs[i] != NULL; i++) {
            if (strstr(line, suspicious_libs[i])) {
                LOGW("Suspicious library detected in memory: %s", suspicious_libs[i]);
                fclose(fp);
                return JNI_TRUE;
            }
        }
    }
    
    fclose(fp);
    return JNI_FALSE;
}

// HookDetection Companion object methods (Kotlin companion object generates different JNI signatures)
JNIEXPORT jboolean JNICALL
Java_com.example.raspsdk_HookDetection_00024Companion_nativeFridaCheck(JNIEnv *env, jclass clazz) {
    (void)env;    // Suppress unused parameter warning
    (void)clazz;  // Suppress unused parameter warning
    
    // Check for Frida-specific indicators
    FILE *fp = fopen("/proc/self/maps", "r");
    if (fp == NULL) {
        return JNI_FALSE;
    }
    
    char line[1024];
    const char* frida_indicators[] = {
        "frida-gadget", "frida-agent", "frida-core", "libfrida", NULL
    };
    
    while (fgets(line, sizeof(line), fp)) {
        for (int i = 0; frida_indicators[i] != NULL; i++) {
            if (strstr(line, frida_indicators[i])) {
                LOGW("Frida indicator detected: %s", frida_indicators[i]);
                fclose(fp);
                return JNI_TRUE;
            }
        }
    }
    
    fclose(fp);
    return JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_com.example.raspsdk_HookDetection_00024Companion_nativeHookCheck(JNIEnv *env, jclass clazz) {
    return Java_com.example.raspsdk_HookDetection_nativeHookCheck(env, clazz);
}

// Forward declaration for the inline hook check method (defined later)
JNIEXPORT jboolean JNICALL Java_com.example.raspsdk_HookDetection_nativeInlineHookCheck(JNIEnv *env, jclass clazz);

JNIEXPORT jboolean JNICALL
Java_com.example.raspsdk_HookDetection_00024Companion_nativeInlineHookCheck(JNIEnv *env, jclass clazz) {
    return Java_com.example.raspsdk_HookDetection_nativeInlineHookCheck(env, clazz);
}

// Keep the old methods for backward compatibility
JNIEXPORT jboolean JNICALL
Java_com.example.raspsdk_HookDetection_nativeFridaCheck(JNIEnv *env, jclass clazz) {
    return Java_com.example.raspsdk_HookDetection_00024Companion_nativeFridaCheck(env, clazz);
}

JNIEXPORT jboolean JNICALL
Java_com.example.raspsdk_HookDetection_nativeInlineHookCheck(JNIEnv *env, jclass clazz) {
    (void)env;    // Suppress unused parameter warning
    (void)clazz;  // Suppress unused parameter warning
    
    // Check function prologue for common hook patterns
    // This is a simplified check - real implementation would be more sophisticated
    
    // Get address of this function
    void *func_addr = (void*)Java_com.example.raspsdk_HookDetection_nativeInlineHookCheck;
    
    // Check first few bytes for common hook patterns
    unsigned char *bytes = (unsigned char*)func_addr;
    
    // Check for common x86/ARM hook patterns
    // x86: 0xE9 (JMP), 0x68 (PUSH)
    // ARM: 0xE51FF004 (LDR PC, [PC, #-4])
    if (bytes[0] == 0xE9 || bytes[0] == 0x68) {
        LOGW("Possible inline hook detected (x86)");
        return JNI_TRUE;
    }
    
    // ARM check (simplified)
    uint32_t *arm_bytes = (uint32_t*)func_addr;
    if (arm_bytes[0] == 0xE51FF004) {
        LOGW("Possible inline hook detected (ARM)");
        return JNI_TRUE;
    }
    
    return JNI_FALSE;
}

// TamperDetection native methods
JNIEXPORT jboolean JNICALL
Java_com.example.raspsdk_TamperDetection_nativeMemoryCheck(JNIEnv *env, jclass clazz) {
    (void)env;    // Suppress unused parameter warning
    (void)clazz;  // Suppress unused parameter warning
    
    // Check memory mappings for suspicious modifications
    FILE *fp = fopen("/proc/self/maps", "r");
    if (fp == NULL) {
        return JNI_FALSE;
    }
    
    char line[1024];
    int executable_count = 0;
    int writable_executable_count = 0;
    
    while (fgets(line, sizeof(line), fp)) {
        // Check for executable regions
        if (strstr(line, "r-xp") || strstr(line, "rwxp")) {
            executable_count++;
            (void)executable_count;  // Use the variable to suppress warning
            
            // Check for writable+executable (dangerous)
            if (strstr(line, "rwxp")) {
                writable_executable_count++;
                LOGW("Writable+Executable memory region detected: %s", line);
            }
        }
    }
    
    fclose(fp);
    
    // If too many writable+executable regions, might be tampered
    if (writable_executable_count > 5) {
        LOGW("Too many writable+executable regions: %d", writable_executable_count);
        return JNI_TRUE;
    }
    
    return JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_com.example.raspsdk_TamperDetection_nativeIntegrityCheck(JNIEnv *env, jclass clazz) {
    (void)env;    // Suppress unused parameter warning
    (void)clazz;  // Suppress unused parameter warning
    
    // Simple integrity check using prctl
    if (prctl(PR_SET_DUMPABLE, 0) == -1) {
        LOGW("Failed to disable core dumps");
        return JNI_TRUE;
    }
    
    return JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_com.example.raspsdk_TamperDetection_nativeBreakpointScan(JNIEnv *env, jclass clazz) {
    (void)env;    // Suppress unused parameter warning
    (void)clazz;  // Suppress unused parameter warning
    
    // Scan memory for common breakpoint instructions
    // This is a simplified implementation
    
    // Get current function address range
    void *start_addr = (void*)Java_com.example.raspsdk_TamperDetection_nativeBreakpointScan;
    size_t scan_size = 4096; // Scan first 4KB
    
    unsigned char *memory = (unsigned char*)start_addr;
    
    for (size_t i = 0; i < scan_size; i++) {
        // Check for common breakpoint instructions
        if (memory[i] == 0xCC) { // x86 INT3
            LOGW("x86 breakpoint instruction (INT3) detected at offset %zu", i);
            return JNI_TRUE;
        }
        
        // ARM breakpoint patterns (simplified)
        if (i < scan_size - 3) {
            uint32_t *arm_instr = (uint32_t*)(memory + i);
            if ((*arm_instr & 0xFFF000F0) == 0xE1200070) { // ARM breakpoint
                LOGW("ARM breakpoint instruction detected at offset %zu", i);
                return JNI_TRUE;
            }
        }
    }
    
    return JNI_FALSE;
}

// System hardening functions
JNIEXPORT jboolean JNICALL
Java_com.example.raspsdk_RASP_nativeHardenSystem(JNIEnv *env, jclass clazz) {
    (void)env;    // Suppress unused parameter warning
    (void)clazz;  // Suppress unused parameter warning
    
    // Disable core dumps
    if (prctl(PR_SET_DUMPABLE, 0) == -1) {
        LOGW("Failed to disable core dumps: %s", strerror(errno));
        return JNI_FALSE;
    }
    
    // Set up anti-debugging measures
    if (ptrace(PTRACE_TRACEME, 0, 1, 0) == -1) {
        if (errno == EPERM) {
            LOGW("Already being traced - debugger detected");
            return JNI_FALSE;
        }
    }
    
    LOGI("System hardening applied successfully");
    return JNI_TRUE;
}

// Memory protection utilities
JNIEXPORT jboolean JNICALL
Java_com.example.raspsdk_RASP_nativeProtectMemory(JNIEnv *env, jclass clazz, jlong addr, jint size) {
    (void)env;    // Suppress unused parameter warning
    (void)clazz;  // Suppress unused parameter warning
    
    // Make memory region read-only
    void *memory_addr = (void*)addr;
    
    if (mprotect(memory_addr, size, PROT_READ) == -1) {
        LOGE("Failed to protect memory region: %s", strerror(errno));
        return JNI_FALSE;
    }
    
    return JNI_TRUE;
}

// Random delay for timing obfuscation
JNIEXPORT void JNICALL
Java_com.example.raspsdk_RASP_nativeRandomDelay(JNIEnv *env, jclass clazz) {
    (void)env;    // Suppress unused parameter warning
    (void)clazz;  // Suppress unused parameter warning
    
    // Random delay between 1-100ms
    int delay_ms = (rand() % 100) + 1;
    usleep(delay_ms * 1000);
}

// Anti-debugging fork bomb (use with caution!)
JNIEXPORT jboolean JNICALL
Java_com.example.raspsdk_RASP_nativeAntiFork(JNIEnv *env, jclass clazz) {
    (void)env;    // Suppress unused parameter warning
    (void)clazz;  // Suppress unused parameter warning
    
    pid_t pid = fork();
    
    if (pid == 0) {
        // Child process - exit immediately
        _exit(0);
    } else if (pid > 0) {
        // Parent process - check if fork was successful
        return JNI_TRUE;
    } else {
        // Fork failed
        LOGE("Fork failed: %s", strerror(errno));
        return JNI_FALSE;
    }
}

} // extern "C"

