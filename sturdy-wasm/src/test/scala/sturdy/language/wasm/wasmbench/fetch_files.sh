#!/bin/bash

TY=binaries
EXT=7z
F=filtered

DIR="$( cd -- "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 ; pwd -P )"
DIR="${DIR/\/scala//resources}"
echo $DIR

if ! test -d $DIR ; then
  mkdir -p $DIR
fi

while getopts muh opt; do
  case $opt in
    m)
      echo "downloading metadata only"
      TY=metadata
      EXT=json
      ;;
    u)
      echo "downloading unfiltered dataset files"
      F=unfiltered
      ;;
    h)
      echo "Download wasmbench binaries and metadata" >&2
      echo "  [-m metadata only]" >&2
      echo "  [-u unfiltered dataset]" >&2
      exit 0
      ;;
  esac
done

FILE="$TY.$F.$EXT"

UF_BIN_URL=https://github.com/sola-st/WasmBench/releases/download/v1.0/all-binaries-metadata.7z
F_BIN_URL=https://github.com/sola-st/WasmBench/releases/download/v1.0/filtered-binaries-metadata.7z
UF_MD_URL=https://github.com/sola-st/WasmBench/blob/main/dataset-metadata/all.pretty.json?raw=true
F_MD_URL=https://github.com/sola-st/WasmBench/blob/main/dataset-metadata/filtered.pretty.json?raw=true

UF_BIN=binaries.unfiltered.7z
F_BIN=binaries.filtered.7z
UF_MD=metadata.unfiltered.json
F_MD=metadata.filtered.json

case $FILE in
  $F_BIN)
    if ! test -f "$DIR/$F_BIN" ; then
      curl -L -H 'Accept: application/json' $F_BIN_URL -o "$DIR/$F_BIN"
    fi
    7za x "$DIR/$F_BIN" -aos -o"$DIR" -x!filtered.pretty.json

#    metadata in binaries archive does not contain 'possible_source_languages' field, download separately
    curl -L -H 'Accept: application/json' $F_MD_URL -o "$DIR/$F_MD"
    ;;
  $UF_BIN)
    if ! test -f "$DIR/$UF_BIN" ; then
      curl -L -H 'Accept: application/json' $F_BIN_URL -o "$DIR/$UF_BIN"
    fi
    7za x "$DIR/$UF_BIN" -aos -o"$DIR"
    mv "$DIR/all.pretty.json" "$DIR/metadata.unfiltered.json"
    ;;
  $F_MD)
    if ! test -f "$DIR/$F_MD" ; then
      curl -L -H 'Accept: application/json' $F_MD_URL -o "$DIR/$F_MD"
    fi
    ;;
  $UF_MD)
    if ! test -f "$DIR/$UF_MD" ; then
      curl -L -H 'Accept: application/json' $UF_MD_URL -o "$DIR/$UF_MD"
    fi
esac