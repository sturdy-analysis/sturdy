package dwarfSupport

import sturdy.language.wasm.Parsing
import swam.binary.custom.dwarf.{DwarfSyntaxTree, DwarfTreeBuilder}

import java.net.URI
import java.io.File
import java.nio.file.Path

/**
 * @return a List of paths containing all .wasm-files in the benchmarksgame/src folder
 */
def getBenchmarkGameFiles: List[Path] = {
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

def getArrayTestFile: Path = {
  getBenchmarkGameFiles.find(path => path.toString.contains("test-arrays")).getOrElse(sys.error("could not find test-arrays"))
}

def getCallByReferenceTestFile: Path = {
  getBenchmarkGameFiles.find(path => path.toString.contains("test-call-by-reference")).getOrElse(sys.error("could not find test-call-by-reference"))
}

def getArrayOfStructsTestFile: Path = {
  getBenchmarkGameFiles.find(path => path.toString.contains("test-array-of-structs")).getOrElse(sys.error("could not find test-array-of-structs"))
}

def tryToMakeASTFromBinary(path: Path): Option[DwarfSyntaxTree] = {
  val mod = Parsing.fromBinary(path)
  mod.dwarfContext match {
    case Some(ctx) =>
      val unit = ctx.CompileUnits().get(0)
      Option(new DwarfTreeBuilder().makeAST(unit))
    case None => None
  }
}
