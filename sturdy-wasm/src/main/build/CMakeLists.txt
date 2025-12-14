# THIS IS JUST FOR CLION INTELLISENSE
# JUST RUN THE compile.sh TO COMPILE THE shared library

cmake_minimum_required(VERSION 3.28)
project(LLVMDWARFAPI LANGUAGES CXX)

# Set include paths
include_directories(
        #Source files:
        src

        #jni.h:
        $ENV{JAVA_HOME}/include
        $ENV{JAVA_HOME}/include/linux

        #llvm header locations
        /usr/local/llvm-19/include
)

set(CMAKE_CXX_STANDARD 20)

set(SOURCES
        src/JNIHelpers.h
        src/JNIHelpers.cpp
        src/llvm_DWARFContext.h
        src/llvm_DWARFContext.cpp
        src/llvm_DWARFUnit.h
        src/llvm_DWARFUnit.cpp
        src/llvm_DWARFDie.h
        src/llvm_DWARFDie.cpp
)

add_library(LLVMDWARFAPI SHARED ${SOURCES})

target_link_libraries(LLVMDWARFAPI
        # llvm shared library
        /usr/lib/x86_64-linux-gnu/libLLVM.so.19.1
)