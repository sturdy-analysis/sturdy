package dwarfSupport

import org.scalatest.funsuite.AnyFunSuite
import sturdy.language.wasm.Parsing
import swam.binary.custom.dwarf.{DwarfLogging, DwarfTreeBuilder}
import swam.syntax.Module

import java.nio.file.Path

class dwarfTest extends AnyFunSuite{
  val DEBUG = true
  val wasmFiles: List[Path] = getDwarfTestFiles

  for (file <- wasmFiles) {
    test(s"can build ast for ${file.getFileName}") {
      val mod: Module = Parsing.fromBinary(file)
      mod.dwarfContext match {
        case Some(ctx) =>
          val unit = ctx.CompileUnits().get(0)
          val astBuilder = new DwarfTreeBuilder()
          val syntaxTree = astBuilder.makeAST(unit)
          if (DEBUG) {
            println("======================")
            println(file.getFileName)
            println("======================")
            println(DwarfLogging.formatAST(syntaxTree))
          }
        case None =>
          fail(s"could not read dwarf sections from ${file.getFileName}")
      }
    }
  }
}
