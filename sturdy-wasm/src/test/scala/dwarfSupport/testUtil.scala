package dwarfSupport

import java.net.URI
import java.io.File
import java.nio.file.Path

def BenchmarkGameFiles(): List[Path] = {
  val uri: URI = this.getClass.getResource("/sturdy/language/wasm/benchmarksgame/src").toURI
  val extension = ".wasm"
  val srcFolder = File(uri)
  if (!srcFolder.exists || !srcFolder.isDirectory) {
    sys.error("test Files not found.")
  }
  val wasmFiles: List[Path] = srcFolder.listFiles()
    .filter(f => f.isFile && f.getName.endsWith(extension))
    .toList
    .map(file => file.toPath)
  wasmFiles
}
