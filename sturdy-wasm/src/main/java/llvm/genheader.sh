#!/bin/bash
set -e

# Check if javac exists
if ! command -v javac >/dev/null 2>&1; then
    echo "Error: javac not found! Make sure javac is available in this terminal."
    exit 1
fi

echo "generating JNI .h files into /src/main/build/"
javac -h ../../build/src NativeCleaner.java DWARFContext.java DWARFUnit.java DWARFDie.java DwarfTag.java DwarfAttr.java
echo "generated JNI .h files into /src/main/build/"