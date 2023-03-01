#!/bin/bash

WASMOPT=/Volumes/sebahome/projects/external/binaryen/bin/wasm-opt
TIMEOUT_SECONDS=120

for f in filtered/*.wasm;
do
  echo $WASMOPT -Oz $f -o $f.by
  { gtimeout $TIMEOUT_SECONDS time $WASMOPT -Oz $f -o $f.by; } 2> $f.bytime
done
