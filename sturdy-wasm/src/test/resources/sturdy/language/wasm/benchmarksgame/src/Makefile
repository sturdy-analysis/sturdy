SOURCE_FILES := $(wildcard *.c)
WASM_FILES := $(patsubst %.c,%.wasm,$(SOURCE_FILES))
WAST_FILES := $(patsubst %.wasm,%.wast,$(WASM_FILES))

.PHONY: all

all: $(WASM_FILES) $(WAST_FILES)

clean:
	rm -f $(WASM_FILES) $(WAST_FILES)

%.wasm: %.c
	clang-19 --target=wasm32 --no-standard-libraries -O0 -g -Wl,--allow-undefined,--export-all,--no-gc-sections $< -o $@
	wasm-opt $@ -O1 -g -o $@

%.wast: %.wasm
	wasm2wat $< --generate-names -o $@