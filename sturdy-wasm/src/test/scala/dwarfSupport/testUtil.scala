package dwarfSupport

import sturdy.language.wasm.Parsing
import swam.binary.custom.dwarf.{DwarfSyntaxTree, DwarfTreeBuilder}

import java.net.URI
import java.io.File
import java.nio.file.Path

/**
 * @return a List of paths containing all .wasm-files in the benchmarksgame/src folder
 */
def getBenchmarksGameFiles: List[Path] = {
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

/**
 * enumeration representing all benchmarksgame test files.
 * @param fileName name of the file (without file ending like .wasm or .wast) this allows the encoding to be specified later
 */
enum BenchmarksgameFile(val fileName: String):
  case Binarytrees         extends BenchmarksgameFile("binarytrees")
  case Fankuchredux        extends BenchmarksgameFile("fankuchredux")
  case Mandelbrot          extends BenchmarksgameFile("mandelbrot")
  case Nbody               extends BenchmarksgameFile("nbody")
  case Pidigits            extends BenchmarksgameFile("pidigits")
  case ReverseComplement   extends BenchmarksgameFile("reverse-complement")
  case SpectralNorm        extends BenchmarksgameFile("spectral-norm")
  case TestArrayOfStructs  extends BenchmarksgameFile("test-array-of-structs")
  case TestArrays          extends BenchmarksgameFile("test-arrays")
  case TestCallByReference extends BenchmarksgameFile("test-call-by-reference")

/**
 * utility function returning the path to the test file specified through the parameter. this only succeeds if the files actually exist and are returned by getBenchmarkGameFiles
 * @param benchmarksgameFile enum value describing the path to be created
 * @return path to the file.
 */
def getTestFile(benchmarksgameFile: BenchmarksgameFile): Path = {
  getBenchmarksGameFiles.find(path => path.toString.contains(benchmarksgameFile.fileName)).getOrElse(sys.error(s"could not find ${benchmarksgameFile.fileName}"))
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
