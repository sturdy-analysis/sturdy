package sturdy.language.wasm.benchmarksgame

import apron.*
import com.github.tototoshi.csv.{CSVReader, CSVWriter, DefaultCSVFormat}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.{BeforeAndAfterAll, Suites}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import sturdy.control.PrintingControlObserver
import sturdy.fix.StackConfig
import sturdy.language.wasm
import sturdy.language.wasm.Parsing
import sturdy.language.wasm.analyses.{FixpointConfig, RelationalAnalysis, WasmConfig}
import sturdy.language.wasm.analyses.RelationalAnalysis.ByteMemoryCtx
import sturdy.language.wasm.generic.{*, given}
import sturdy.util.Profiler
import sturdy.values.{*, given}
import swam.syntax
import swam.syntax.{LoadInst, LoadNInst, StoreInst, StoreNInst}

import java.io.File
import java.net.URI
import java.nio.file.{Files, Path, Paths}
import scala.collection.immutable.SortedMap
import scala.jdk.StreamConverters.*

val writer: CSVWriter = CSVWriter.open(File("benchmarks-game-precision-test.csv"))

class BenchmarksgameRelationalPrecisionTests extends Suites(
  BenchmarksgameRelationalPrecisionTest(manager = Polka(true), relational = true),
  BenchmarksgameRelationalPrecisionTest(manager = Octagon(), relational = true),
  BenchmarksgameRelationalPrecisionTest(manager = Box(), relational = true),
  BenchmarksgameRelationalPrecisionTest(manager = Box(), relational = false)
), BeforeAndAfterAll:

  override def beforeAll(): Unit =
    writer.writeRow(List("program", "analysis", "precise_loads", "imprecise_loads", "precise_stores", "imprecise_stores"))

  override def afterAll(): Unit =
    writer.close

class BenchmarksgameRelationalPrecisionTest(manager: Manager, relational: Boolean) extends AnyFunSpec, Matchers:

  val funcName = "_start"
  val uri: URI = this.getClass.getResource("/sturdy/language/wasm/benchmarksgame/src").toURI;

  val fixpointConfig: FixpointConfig = FixpointConfig(
    stack = StackConfig.StackedStates(),
    iter = sturdy.fix.iter.Config.Innermost
  )

  // These programs contain structs, which our analysis does not yet support.
  val excluded: Set[Path] = Set("k-nucleotide.wasm", "pidigits.wasm", "test-arrays.wasm", "test-call-by-reference.wasm").map(prog =>
    Paths.get(uri).resolve(prog)
  )

  val analysisName: String = if (relational) manager.getClass.getSimpleName else "non-relational"

  describe(analysisName) {
    Files.list(Paths.get(uri)).toScala(List).filter(p => p.toString.endsWith("spectral-norm.wasm") && !excluded.contains(p)).sorted.foreach { p =>
      it(s"${p.getFileName}") {
        val module = Parsing.fromBinary(p)

        val analysis = RelationalAnalysis.Instance(manager, FrameData.empty, Iterable.empty, WasmConfig(fix = fixpointConfig, relational = relational))
        val relationalMemoryLogger = analysis.memoryLogger
        analysis.addControlObserver(new PrintingControlObserver("  ", "\n")(println))
        var moduleInst = analysis.instantiateModule(module, moduleId = Some(p.getFileName))
        analysis.failure.fallible(
          analysis.invokeExported(moduleInst, funcName, List.empty)
        )
        Profiler.printLastMeasured()
        Profiler.reset()

        val expected = parseMemOpsCSV(p, moduleInst)

        writePrecision(p, analysis, relationalMemoryLogger, expected)
      }
    }
  }

  def writePrecision(programPath: Path, analysis: RelationalAnalysis.Instance, memoryLogger: analysis.MemoryLogger, expected: SortedMap[InstLoc, Set[ByteMemoryCtx]]): Unit =
    val program = programPath.getFileName
    val precision: memoryLogger.Precision = memoryLogger.computePrecision(expected)
    val preciseLoads = filterLoads(precision.precise)
    val impreciseLoads = filterLoads(precision.imprecise)
    val preciseStores = filterStores(precision.precise)
    val impreciseStores= filterStores(precision.imprecise)

    writer.writeRow(List(program, analysisName, s"${preciseLoads.size}", s"${impreciseLoads.size}", s"${preciseStores.size}", s"${impreciseStores.size}"))

  inline def filterLoads(map: SortedMap[InstLoc, (LoadInst | LoadNInst | StoreInst | StoreNInst, Set[ByteMemoryCtx])]): SortedMap[InstLoc, (LoadInst | LoadNInst | StoreInst | StoreNInst, Set[ByteMemoryCtx])] =
    map.filter { case (key, (_: (LoadInst | LoadNInst), _)) => true; case _ => false }

  inline def filterStores(map: SortedMap[InstLoc, (LoadInst | LoadNInst | StoreInst | StoreNInst, Set[ByteMemoryCtx])]): SortedMap[InstLoc, (LoadInst | LoadNInst | StoreInst | StoreNInst, Set[ByteMemoryCtx])] =
    map.filter { case (key, (_: (StoreInst | StoreNInst), _)) => true; case _ => false }


  def parseMemOpsCSV(p: java.nio.file.Path, moduleInstance: ModuleInstance): SortedMap[InstLoc, Set[ByteMemoryCtx]] =
    val reader = CSVReader.open(p.toString + ".memops.csv")
    SortedMap.from(reader.iterator.drop(1).map(parseLoadsCSVLine(using moduleInstance)))

  def parseLoadsCSVLine(using moduleInstance: ModuleInstance)(line: Seq[String]): (InstLoc, Set[ByteMemoryCtx]) =
    val Seq(instLocStr, _memOp, heapCtxStr) = line
    val Array(func, pc) = instLocStr.split(':')
    val instLoc = InstLoc.InFunction(func, pc.toInt)
    val heapCtxs = heapCtxStr.split(';').map(_.trim).map(parseHeapCtx).toSet
    (instLoc, heapCtxs)

  def parseHeapCtx(using moduleInstance: ModuleInstance)(heapCtxStr: String): ByteMemoryCtx =
    heapCtxStr.take(1) match {
      case "F" => ByteMemoryCtx.Fill(FixIn.MostGeneralClientLoop(moduleInstance))
      case "G" => ByteMemoryCtx.Global(heapCtxStr.drop(1))
      case "S" =>
        val Array(funcName, offset) = heapCtxStr.drop(1).split('+')
        ByteMemoryCtx.Stack(FuncId(funcName), offset.toInt)
      case "H" =>
        val Array(func, pc, offset) = heapCtxStr.drop(1).split(Array(':', '+'))
        val instLoc = InstLoc.InFunction(func, pc.toInt)
        ByteMemoryCtx.Heap(instLoc, offset.toInt)
    }