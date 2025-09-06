# Sets the minimum version of CMake required to build the native library.
cmake_minimum_required(VERSION 3.22.1)

# Declares and names the project.
project("anti-debug-native")

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
add_library(
    # Sets the name of the library.
    anti-debug-native

    # Sets the library as a shared library.
    SHARED

    # Provides a relative path to your source file(s).
    native-lib.cpp
)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library(
    # Sets the name of the path variable.
    log-lib

    # Specifies the name of the NDK library that
    # you want CMake to locate.
    log
)

# Add Android log library
find_library(android-lib android)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries(
    # Specifies the target library.
    anti-debug-native

    # Links the target library to the log library
    # included in the NDK.
    ${log-lib}
    ${android-lib}
)

# Add compiler flags for security and optimization
target_compile_options(anti-debug-native PRIVATE
    -Wall
    -Wextra
    -Werror
    -Wno-unused-parameter
    -Wno-unused-variable
    -O2
    -fstack-protector-strong
    -fPIC
    -D_FORTIFY_SOURCE=2
)

# Add linker flags for security and 16KB page size compatibility
target_link_options(anti-debug-native PRIVATE
    -Wl,-z,relro
    -Wl,-z,now
    -Wl,-z,noexecstack
    # 16KB page size alignment for Android 15+ compatibility
    -Wl,-z,max-page-size=16384
    -Wl,-z,common-page-size=16384
    # Ensure LOAD segments are properly aligned
    -Wl,--section-start=.text=0x10000
)
