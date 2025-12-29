package DWARFContextValidation

import org.scalatest.funsuite.AnyFunSuite
import swam.binary.custom.dwarf.llvm.DWARFContext

import java.io.File
import java.net.URI

class testDWARFContext extends AnyFunSuite {
  val uri: URI = this.getClass.getResource("/sturdy/language/wasm/benchmarksgame/src").toURI;
  val extension = ".wasm"
  val srcFolder = File(uri)
  if (!srcFolder.exists || !srcFolder.isDirectory) {
    sys.error("test Files not found.")
  }
  val wasmFiles: List[File] = srcFolder.listFiles
    .filter(f => f.isFile && f.getName.endsWith(extension))
    .toList
  
  wasmFiles.foreach { file =>
    test(s"DWARF custom sections are present in ${file.getName}") {
      val contextOpt = attemptCreateContext(file.getAbsolutePath)
      assert(contextOpt.isDefined, s"Failed to create DWARFContext for file: ${file.getName}")
    }
  }

  def attemptCreateContext(absoluteFilepath: String): Option[DWARFContext] = {
    try {
      Some(DWARFContext(absoluteFilepath))
    } catch {
      case _: RuntimeException => None
    }
  }

}
