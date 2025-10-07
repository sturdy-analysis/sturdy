SOURCE_FILES := $(wildcard *.c)
WASM_FILES := $(patsubst %.c,%.wasm,$(SOURCE_FILES))
WAST_FILES := $(patsubst %.wasm,%.wast,$(WASM_FILES))

.PHONY: all

all: $(WASM_FILES) $(WAST_FILES)

clean:
	rm -f $(WASM_FILES) $(WAST_FILES)

%.wasm: %.c
	clang-19 --target=wasm32 --no-standard-libraries -O1 -g -Wl,--allow-undefined,--export-all $< -o $@
#	emcc -sSTANDALONE_WASM -sEXPORTED_FUNCTIONS=_main -sMALLOC=none -O3 -g \
#		-Wl,--allow-undefined \
#		 -Wno-limited-postlink-optimizations \
#		 $< -o $@

%.wast: %.wasm
	wasm2wat $< -o $@