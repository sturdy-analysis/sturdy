SOURCE_FILES := $(wildcard ../src/*.c)
WASM_FILES := $(patsubst ../src/%.c,./%.wasm,$(SOURCE_FILES))
WAST_FILES := $(patsubst %.wasm,%.wast,$(WASM_FILES))

.PHONY: all

all: $(WASM_FILES) $(WAST_FILES)

clean:
	rm -f $(WASM_FILES) $(WAST_FILES)

%.wasm: ../src/%.c
	clang-21 --target=wasm32 --no-standard-libraries -O0 -g -Wl,--allow-undefined,--export-all,--no-gc-sections $< -o $@
#	wasm-opt $@ -O1 -g -o $@

%.wast: %.wasm
	wasm2wat $< --generate-names --enable-all -o $@
	wasm2wat $< --generate-names --fold-exprs --enable-all -o $(patsubst %.wasm,%.folded.wast,$<)