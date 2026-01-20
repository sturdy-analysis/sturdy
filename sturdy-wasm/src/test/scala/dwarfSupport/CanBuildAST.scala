package dwarfSupport

import dwarfSupport.BenchmarksgameFile._
import org.scalatest.funsuite.AnyFunSuite
import sturdy.language.wasm.Parsing
import swam.binary.custom.dwarf.{DwarfLogging, DwarfTreeBuilder}
import swam.syntax.Module

import java.nio.file.Path

class CanBuildAST extends AnyFunSuite {
  val DEBUG = false
  val wasmFiles: List[Path] = getBenchmarksGameFiles

  for (file <- wasmFiles) {
    test(s"can build ast for ${file.getFileName}") {
      val mod: Module = Parsing.fromBinary(file)
      mod.dwarfContext match {
        case Some(ctx) =>
          val unit = ctx.CompileUnits().get(0)
          val astBuilder = new DwarfTreeBuilder()
          astBuilder.makeAST(unit)
        case None =>
          fail(s"could not read dwarf sections from ${file.getFileName}")
      }
    }
  }
  test(s"can build ast for test-arrays fromBinary") {
    tryToMakeASTFromBinary(getTestFile(TestArrays)) match {
      case Some(ast) =>
        if (DEBUG) println(DwarfLogging.formatAST(ast))
      case None => fail(s"could not make AST fromBinary")
    }
  }
  test(s"can build ast for test-array-of-structs fromBinary") {
    tryToMakeASTFromBinary(getTestFile(TestArrayOfStructs)) match {
      case Some(ast) =>
        if (DEBUG) println(DwarfLogging.formatAST(ast))
      case None => fail(s"could not make AST fromBinary")
    }
  }
  test(s"can build ast for test-call-by-reference fromBinary") {
    tryToMakeASTFromBinary(getTestFile(TestCallByReference)) match {
      case Some(ast) =>
        if (DEBUG) println(DwarfLogging.formatAST(ast))
      case None => fail(s"could not make AST fromBinary")
    }
  }
  test(s"can build ast for pidigits fromBinary") {
    tryToMakeASTFromBinary(getTestFile(Pidigits)) match {
      case Some(ast) =>
        if (DEBUG) println(DwarfLogging.formatAST(ast))
        for (global <- ast.globals) {
          println(s"${global.name} has size: ${ast.getTypeSize(global.varType)}")
        }
      case None => fail(s"could not make AST fromBinary")
    }
  }
}