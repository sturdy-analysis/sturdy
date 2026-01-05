package dwarfSupport

import org.scalatest.funsuite.AnyFunSuite
import sturdy.language.wasm.Parsing
import swam.binary.custom.dwarf.DwarfTreeBuilder
import swam.syntax.Module

import java.nio.file.Path

class CanBuildAST extends AnyFunSuite {

  val wasmFiles: List[Path] = BenchmarkGameFiles()

  for (file <- wasmFiles) {
    test(s"can build ast for ${file.getFileName}") {
      val mod: Module = Parsing.fromBinary(file)
      mod.dwarfContext match {
        case Some(ctx) =>
          val unit = ctx.CompileUnits().get(0)
          DwarfTreeBuilder.makeAST(unit)
        case None =>
          fail(s"could not read dwarf sections from ${file.getFileName}")
      }
    }
  }
}