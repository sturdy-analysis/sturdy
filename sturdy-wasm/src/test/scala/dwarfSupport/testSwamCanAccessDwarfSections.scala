package dwarfSupport

import org.scalatest.funsuite.AnyFunSuite
import sturdy.language.wasm.Parsing
import swam.syntax.Module
import java.nio.file.Path

class testSwamCanAccessDwarfSections extends AnyFunSuite {
  val wasmFiles: List[Path] = BenchmarkGameFiles()
  
  wasmFiles.foreach { file =>
    test(s"swam can read DWARF Sections for ${file.getName}.") {
      val mod: Module = Parsing.fromBinary(file)
      mod.dwarfContext match {
        case Some(ctx) => true
        case None => sys.error(s"could not read dwarf sections from ${file.getName}.")
      }
    }
  }
}
