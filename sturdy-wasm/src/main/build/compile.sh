#!/bin/bash
set -e

# Path to Java JDK:
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
echo "Compiling with JAVA_HOME: $JAVA_HOME (jni.h location)"

# LLVM paths
LLVM_INCLUDE=/usr/local/llvm-19/include
LLVM_LIB=/usr/local/llvm-19/lib
echo "LLVM directories: $LLVM_INCLUDE, $LLVM_LIB"

# Source files under /src/
SRC="src/*.cpp"
echo "Source files: $SRC"

# Name of shared library
OUT_LIB=libLLVMDWARFAPI.so


# Compiler
CXX=/usr/local/llvm-19/bin/clang++
$CXX --version

# Compilation flags
CXXFLAGS="-std=c++20 -fPIC -I$LLVM_INCLUDE -I$JAVA_HOME/include -I$JAVA_HOME/include/linux -Wall -O2"

# Linker flags: shared library, link LLVM, embed rpath
LDFLAGS="-shared -L$LLVM_LIB -lLLVM -Wl,-rpath,$LLVM_LIB"

echo "Compiling $SRC into $OUT_LIB..."
$CXX $CXXFLAGS $SRC $LDFLAGS -o $OUT_LIB

echo "$OUT_LIB created."
