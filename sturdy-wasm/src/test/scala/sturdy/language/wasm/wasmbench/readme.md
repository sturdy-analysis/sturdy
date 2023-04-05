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
