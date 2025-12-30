package dwarfSupport

import org.scalatest.funsuite.AnyFunSuite
import swam.binary.custom.dwarf.llvm.DWARFContext

import java.io.File
import java.nio.file.Path

class testDWARFContext extends AnyFunSuite {
  val wasmFiles: List[Path] = BenchmarkGameFiles()
  
  wasmFiles.foreach { file =>
    test(s"DWARF custom sections are present in ${file.getName}") {
      val contextOpt = attemptCreateContext(file.toString)
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
