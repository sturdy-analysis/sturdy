#!/bin/bash

WASMOPT=/Volumes/sebahome/projects/external/binaryen/bin/wasm-opt

INDIR=filtered

TIMEOUT_SECONDS=60
FILES=considered-files-${TIMEOUT_SECONDS}s.txt
OUTDIR=binaryen-out-${TIMEOUT_SECONDS}s

mkdir -p $OUTDIR

# for f in filtered/*.wasm;
while read f
do
  if [ ! -f $OUTDIR/$f.bytime ]
  then
    echo gtimeout $TIMEOUT_SECONDS time $WASMOPT -Oz $INDIR/$f -o $OUTDIR/$f.by
    { gtimeout $TIMEOUT_SECONDS time $WASMOPT -Oz $INDIR/$f -o $OUTDIR/$f.by; } 2> $OUTDIR/$f.bytime
  fi
done <$FILES

