## Wasmbench Usage

* Invoke `fetch_files.sh` with the following switches to download wasmbench files to *test/resources/wasmbench* (requires `7za` in PATH):
  * **-m**: metadata only
  * **-u**: unfiltered binaries or metadata
  * defaults to filtered binaries
* The benchmarks are run in the `WASMBenchRunner` class. Adjust config in the `WASMBenchRunner` object.

## Documentation / Log

### Excluded binaries

Cause memory leakage and performance problems but satisfy other requirements

```
{
  "hash":"681460c7ceeb6c96f37934f7b2d216dff3e7aa3e02f3325d5417113b607b1c03",
  "files":[{
    "collectionMethod":"github",
    "absolutePath":"wasm-study/github/clone/repos/torch2424/as-echo/build/untouched.wasm"
  }],
  "sizeBytes":13623,
  "processors":{},
  "languages":{},
  "instructionCount":5069,
  "inferredSourceLanguages":["AssemblyScript"]
},
{
  "hash": b022a54c3b5546fd09f00e6cb6ed12d04530298cef64182db9e12b8d9b4e4737",
  "files":[{
    "collectionMethod":"github",
    "absolutePath":"wasm-study/github/clone/repos/torch2424/as-playground/map-keys-holey-array/index.wasm"
  }],
  "sizeBytes":9132,
  "processors":{},
  "languages":{},
  "instructionCount":3922,
  "inferredSourceLanguages":["AssemblyScript"]
}
```

### TypeAnalysisTest Run#1

Analysis was set up to run on 1039 binaries, ordered from smallest to 
largest in filesize.

First runs produced many timed out futures, but after removing `b022(...)` from 
the dataset benchmark ran smoothly until he next timed out future was encountered. 
Memory allocated by the timed out futures does not seem to be freed - memory leak?

Binaries analysed: 0 - 730, whereby `c1cfe409e`, `2acb3a786`, `f79f5d53d` 
timed out.

A second run from 728 until 1038 produced no more timed out futures, indicating 
that timing out futures really do cause a memory leak. 

### TaintTest and ConstantTest results

with `b022a54c(..)` removed and heapsize set to 4096m with Xmx4096m, both tests ran
successfully without memory leaks. Analysis of the results revealed high percentages 
of dead code in the binaries. The initial hypothesis for this outcome was:

"Most binaries are intended to be used as library. The _start function is used for
initialization only."

An analysis of the metadata of successfully tested binaries showed that 358 out of 380 binaries that 
passed originate from one github repository, Jacarte/CROW_tmp/rosetta_unique.
All binaries in this folder include:
- a set of functions to conduct benchmarks ("polybench")
- C memory management functions "malloc", "free", "realloc" etc
- C's "printf"

A handful of binaries was explored manually with these results:

```
------------------------------------
Jacarte/CROW_tmp

b317100cccfad408f3bfe20b07d4fe5c8e76543897dd9a49c70bb9e36f09d5ff, 99.46%
    multiplies 10 with 2, does not call printf or malloc or any other of the large functions defined in this binary
    
bff2a448694e54fb45b41f1ae638678cdb0fac7ca3872c9be68d3efbeb4e035e, 33.73%
    calls malloc, free, printf
    
3ead8e1f053b2faf761e54681a87cf431d77548f46a3ae54470474cda0b703a8, 60.17%
    calls malloc and free, but not printf
    
b8a884b2df0cd8850d088fa6bdf71e8e837f001ec5a556150ffab00afa257077, 63,51%
    calls printf but not malloc, free etc
    
--------------------------------------------------
Non-Jacarte/CROW_tmp

bc7cb89c1cb09cd1098755e9459d8f44ebd5f7fa723f5a42a79130512653c641, 48.92%
  TODO: Analyse manually

5e27bd616495f80bd81b86f70a515234f22199c0edfb2c840a77f03773487f17, 90,43%
  TODO: Analyse manually

6f70fe81ffff021c7fe87c5bf61ba78604520d7453067f2c0111f58abc07d278, 99.97%
    _start function is a one liner to empty fun __wasm_call_ctors,
    purpose probably in:
        multipleFloat64ArraysWithOutputArray
        multipleFloat64ArraysReturningPtr
    
2acb3a786d7aa11021da486bbdd5cf0ef7ed9b0d7c300ebb348be2884a13536a, 40.8%
    defines strtoull, strtoll but never calls them

7081270ad7c91ee6f0d18df593ae06391aef9182f38c60273bdfefe551a0fb94,  1.62%
    exports 3 functions, non semantic function names
    generates looping music

```

### Implementation of HostFunction, WASI

Analysis of the errors produced by failed tests revealed the following:

(error histogram here)

A host of errors, namely those mentioning `No host function with name x` would be fixable
by implementing for every error with name `x` an appropriate case in enum `HostFunction` of
package `sturdy.language.wasm.generic` 

The following host functions were implemented:
- args_sizes_get
- args_get
- environ_sizes_get
- environ_get
- fd_prestat_get
- random_get
- path_open
- fd_prestat_dir_name

The study of WASI documentation revealed another possible fix to increase number of 
successfully completed tests. It is my understanding that the module `wasi_unstable` is 
a the predecessor of `wasi_snapshot_preview1`, for which all the current host function
implementations or abstractions are implemented. If we figure out how and where the 2 version
of WASI differ, we might be able to just reuse the current implementations for `wasi_unstable`
as well.

### WASM Module 

From the beginning the nature of the `_start` function was unclear. It is my current
understanding that said function is a requirement when targeting the experimental 
`wasm32_wasi` target.

https://github.com/WebAssembly/WASI/blob/main/legacy/application-abi.md

https://doc.rust-lang.org/stable/nightly-rustc/rustc_target/spec/wasm32_wasi/index.html
https://github.com/bytecodealliance/wasmtime/blob/main/docs/WASI-documents.md

The WASM spec does not specify any requirement regarding a start function. A start function
is optional, and does follow a different syntax: `(start  $func)`

https://webassembly.github.io/spec/core/syntax/modules.html#syntax-start