#!/bin/bash

mkdir -p out

echo "====Compiling to .wasm===="
for file in src/*.c; do
    base="$(basename "${file%.c}")"
    outdir="out/$base"
    mkdir -p "$outdir"

    echo "Compiling $file -> $outdir"

    clang --target=wasm32 -nostdlib -O3 -g \
          -Wl,--allow-undefined,--export-all,--no-gc-sections,--no-entry \
          -o "$outdir/$base.O3.wasm" \
          "$file"

    clang --target=wasm32 -nostdlib -O0 -g \
          -Wl,--allow-undefined,--export-all,--no-gc-sections,--no-entry \
          -o "$outdir/$base.O0.wasm" \
          "$file"

    clang --target=wasm32 -nostdlib -O3 \
              -Wl,--allow-undefined,--export-all,--no-gc-sections,--no-entry \
              -o "$outdir/$base.O3_nodebug.wasm" \
              "$file"

    clang --target=wasm32 -nostdlib -O0 \
              -Wl,--allow-undefined,--export-all,--no-gc-sections,--no-entry \
              -o "$outdir/$base.O0_nodebug.wasm" \
              "$file"
done

echo "====Converting from .wasm to .wast===="
for dir in out/*/; do
    for wasm in "$dir"/*.wasm; do
        name="$(basename "${wasm%.wasm}")"
        echo "Converting $wasm -> $name.wast"

        wasm2wat "$wasm" --generate-names --enable-all \
            -o "$dir/$name.wast"

        wasm2wat "$wasm" --generate-names --enable-all --fold-exprs \
            -o "$dir/$name.folded.wast"
    done
done

echo "====Extracting DWARF Information to .log files===="
for dir in out/*/; do
    for wasm in "$dir"/*.wasm; do
        name="$(basename "${wasm%.wasm}")"
        echo "Dumping DWARF debug info $wasm -> $name.log"

        llvm-dwarfdump "$wasm" > "$dir/$name.log"
    done
done