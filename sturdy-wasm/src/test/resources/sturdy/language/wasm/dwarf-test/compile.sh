#!/bin/bash

mkdir -p wasm wast log

echo "====Compiling to .wasm===="
for file in src/*.c; do
    name="$(basename "${file%.c}")"
    echo "Compiling $file -> $name.wasm"

    clang --target=wasm32 -nostdlib -O3 -g \
          -Wl,--allow-undefined,--export-all,--no-gc-sections,--no-entry \
          -o "wasm/$name.O3.wasm" \
          "$file"

    clang --target=wasm32 -nostdlib -O0 -g \
          -Wl,--allow-undefined,--export-all,--no-gc-sections,--no-entry \
          -o "wasm/$name.O0.wasm" \
          "$file"
done

echo "====Converting from .wasm to .wast===="
for file in wasm/*.wasm; do
    name="$(basename "${file%.wasm}")"
    echo "Converting $file -> $name.wast"
    wasm2wat "$file" --generate-names --enable-all -o "wast/$name.wast"
    wasm2wat "$file" --generate-names --enable-all --fold-exprs -o "folded_wast/$name.folded.wast"
done

echo "====Extracting DWARF Information to .log files===="
for file in wasm/*.wasm; do
    name="$(basename "${file%.wasm}")"
    echo "Dumping DWARF debug info $file -> $name.log"
    llvm-dwarfdump "$file" > "log/$name.log"
done