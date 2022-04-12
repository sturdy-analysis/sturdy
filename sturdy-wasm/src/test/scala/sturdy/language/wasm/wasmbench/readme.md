## Wasmbench

* Invoke `fetch_files.sh` with the following switches to download wasmbench files to *test/resources/wasmbench* (requires `7za` in PATH):
  * **-m**: metadata only
  * **-u**: unfiltered binaries or metadata
  * defaults to filtered binaries

### Cause memory leakage and performance problems:

```
  {
    "hash":"b022a54c3b5546fd09f00e6cb6ed12d04530298cef64182db9e12b8d9b4e4737",
    "sizeBytes":9132,
    "processors":{
      
    },
    "languages":{
      
    },
    "instructionCount":3922,
    "inferredSourceLanguages":["AssemblyScript"]
  }
``` 

### Run longer than 5 minutes:

- c1cfe409e18435f0371876cf25ca47621e0e59f73beb0284dbf1b61b7696f7ef

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